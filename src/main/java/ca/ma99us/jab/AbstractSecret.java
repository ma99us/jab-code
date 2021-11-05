package ca.ma99us.jab;

import lombok.Setter;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;

/**
 * Base class for all cryptography implementations
 */
public abstract class AbstractSecret<T extends AbstractSecret<?>> {
    protected final String keyAlgorithm;
    protected final int keyLen;
    protected final String providerName;

    protected Key privateKey;

    @Setter
    protected static CryptoConfig cryptoConfig;

    // register default global instance. Different implementation could be injected later.
    static {
        cryptoConfig = new CryptoConfig();
    }

    /**
     * @param keyAlgorithm algorithm name
     * @param keyLen       key length in bytes
     */
    protected AbstractSecret(String keyAlgorithm, int keyLen) {
        this.keyAlgorithm = keyAlgorithm;
        this.keyLen = keyLen;

        Provider provider = cryptoConfig.getSecurityProvider();
        if (provider != null) {
//            Security.addProvider(provider);
            Security.insertProviderAt(provider, 1);
            providerName = provider.getName();
        } else {
            providerName = null;
        }
    }

    public T setSecretKey(String key, String salt) {
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] saltBytes = (salt != null ? salt.getBytes(StandardCharsets.UTF_8) : key.getBytes(StandardCharsets.UTF_8));    // TODO: no salt? just use key again
            PBEKeySpec keySpec = new PBEKeySpec(key.toCharArray(), saltBytes, 65536, keyLen);
            byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
//            byte[] keyBytes = wrapBytes(key, keyLen);
            this.privateKey = new SecretKeySpec(wrapBytes(keyBytes, keyLen), keyAlgorithm);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error generating key from secrets", ex);
        }

        return (T) this;
    }

    public T setRandomKey() {
        try {
            KeyGenerator keyGen = providerName != null ? KeyGenerator.getInstance(keyAlgorithm, providerName) : KeyGenerator.getInstance(keyAlgorithm);
            keyGen.init(keyLen * 8, cryptoConfig.getSecureRandom());
            privateKey = keyGen.generateKey();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error generating key", ex);
        }

        return (T) this;
    }

    /**
     * Unique id of the crypto and it's key
     *
     * @return long integer id
     */
    public Long getKeyId() {
        if (privateKey == null) {
            throw new IllegalStateException("Private key has to be set first");
        }

        return JabHasher.getGlobalHasher().hash(this.keyAlgorithm.getBytes(StandardCharsets.UTF_8), privateKey.getEncoded());
    }

    public byte[] getPrivateKeyBytes() {
        return privateKey != null ? privateKey.getEncoded() : null;
    }

    public T setPrivateKeyBytes(byte[] keyBytes) {
        try {
            privateKey = new SecretKeySpec(keyBytes, keyAlgorithm);
        } catch (Exception ex) {
            throw new IllegalStateException("Error setting key from bytes", ex);
        }

        return (T) this;
    }

    protected byte[] wrapBytes(String string, int length) {
        return wrapBytes(string.getBytes(StandardCharsets.UTF_8), length);
    }

    protected byte[] wrapBytes(byte[] bytes, int length) {
        return JabHasher.getGlobalHasher().wrapBytes(bytes, length);
    }

    public static class CryptoConfig {

        public Provider getSecurityProvider() {
            //TODO: for  @see <a href="https://www.bouncycastle.org/java.html">Bouncy Castle</a> third-party lib support:
            // return new org.bouncycastle.jce.provider.BouncyCastleProvider();
            return null;    // default JVM provider
        }

        public SecureRandom getSecureRandom() throws NoSuchAlgorithmException {
            //TODO: for older Java use something like SecureRandom.getInstance("SHA1PRNG")
            return SecureRandom.getInstanceStrong();    // default for Java 1.8+
        }
    }
}
