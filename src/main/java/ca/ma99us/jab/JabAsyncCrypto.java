package ca.ma99us.jab;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Encrypt/decrypt byte arrays with asymmetrical keys.
 */
public class JabAsyncCrypto extends JabCrypto {
    protected Key publicKey;

    /**
     * Default algorithm.
     */
    public JabAsyncCrypto() {
        this("RSA", "RSA/ECB/PKCS1Padding", 512, 0);   // default
    }

    /**
     * @param keyAlgorithm algorithm name
     * @param mode      algorithm mode
     * @param keyLen    key length in bytes
     * @param ivLen     iV length in bytes
     */
    public JabAsyncCrypto(String keyAlgorithm, String mode, int keyLen, int ivLen) {
        super(keyAlgorithm, mode, keyLen, ivLen);
    }

    @Override
    public JabAsyncCrypto setSecretKey(String key, String salt) {
        throw new IllegalArgumentException("Do not use passwords for asymmetric encryption! Generate random key pair by calling 'setRandomKey()' instead.");
    }

    @Override
    public JabAsyncCrypto setRandomKey() {
        try {
            KeyPairGenerator keyGen = providerName != null ? KeyPairGenerator.getInstance(keyAlgorithm, providerName) : KeyPairGenerator.getInstance(keyAlgorithm);
            keyGen.initialize(keyLen * 8, cryptoConfig.getSecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error generating key", ex);
        }

        return this;
    }

    public byte[] getPublicKeyBytes() {
        return publicKey != null ? publicKey.getEncoded() : null;
    }

    public JabAsyncCrypto setPublicKeyBytes(byte[] keyBytes) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = providerName != null ? KeyFactory.getInstance(keyAlgorithm, providerName) : KeyFactory.getInstance(keyAlgorithm);
            publicKey = keyFactory.generatePublic(keySpec);
        } catch (Exception ex) {
            throw new IllegalStateException("Error setting public key from bytes", ex);
        }

        return this;
    }

    @Override
    public JabAsyncCrypto setPrivateKeyBytes(byte[] keyBytes) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = providerName != null ? KeyFactory.getInstance(keyAlgorithm, providerName) : KeyFactory.getInstance(keyAlgorithm);
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (Exception ex) {
            throw new IllegalStateException("Error setting private key from bytes", ex);
        }

        return this;
    }

    /**
     * Encrypt a byte array with the previously set Key.
     *
     * @param value bytes to encrypt
     * @return encrypted bytes
     */
    public byte[] encrypt(byte[] value) {
        if (publicKey == null) {
            throw new NullPointerException("Public key has to be set first");
        }

        return encrypt(value, publicKey);
    }

    /**
     * Decrypts a byte array with the previously set key.
     *
     * @param encrypted bytes to decrypt
     * @return decrypted bytes
     */
    public byte[] decrypt(byte[] encrypted) {
        if (privateKey == null) {
            throw new NullPointerException("Private key has to be set first");
        }

        return decrypt(encrypted, privateKey);
    }
}
