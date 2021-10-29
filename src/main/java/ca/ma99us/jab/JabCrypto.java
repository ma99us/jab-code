package ca.ma99us.jab;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Simple platform-independent way to encrypt/decrypt strings.
 */
public class JabCrypto {
    private final String algorithm;
    private final String mode;
    private final int keyLen;
    private final int ivLen;

    private CryptoKey cryptoKey;

    /**
     * Default Blowfish algorithm.
     */
    public JabCrypto() {
        this("Blowfish", "Blowfish/CBC/PKCS5Padding", 16, 8);   // default
    }

    public JabCrypto setKeySecrets(String key, String salt) {
        this.cryptoKey = new CryptoKey(key, salt);
        return this;
    }

    /**
     * Unique id of the crypto and it's key
     * @return long integer id
     */
    public Long getCryptoId() {
        return JabHasher.getGlobalHasher().hashString(this.algorithm + this.mode + (cryptoKey != null ? cryptoKey.getKeyWithSalt() : ""));
    }

    /**
     * @param algorithm algorithm name
     * @param mode      algorithm mode
     * @param keyLen    key length in bytes
     * @param ivLen     iV length in bytes
     */
    public JabCrypto(String algorithm, String mode, int keyLen, int ivLen) {
        this.algorithm = algorithm;
        this.mode = mode;
        this.keyLen = keyLen;
        this.ivLen = ivLen;
    }

    /**
     * Encrypt a string to human-readable string
     *
     * @param value string to encrypt
     * @param key   crypto key seed
     * @return encrypted string
     */
    public String encryptString(String value, String key) {
        return JabToString.getGlobalToString().bytesToString(encrypt(value.getBytes(StandardCharsets.UTF_8), key));
    }

    /**
     * Encrypt a byte array with the previously set Key.
     *
     * @param value bytes to encrypt
     * @return encrypted bytes
     */
    public byte[] encrypt(byte[] value) {
        if (cryptoKey == null) {
            throw new IllegalArgumentException("Crypto key has to be set first");
        }

        return encrypt(value, cryptoKey.getKey());
    }

    /**
     * Encrypt a byte array.
     *
     * @param value bytes to encrypt
     * @param key   crypto key seed
     * @return encrypted bytes
     */
    public byte[] encrypt(byte[] value, String key) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes(key), algorithm);
            Cipher cipher = Cipher.getInstance(mode);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(ivBytes(key)));
            return cipher.doFinal(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Encryption Error", ex);
        }
    }

    /**
     * Decrypts a string
     *
     * @param value strng to decrypt
     * @param key   crypto key seed
     * @return decrypted string
     */
    public String decryptString(String value, String key) {
        return new String(decrypt(JabToString.getGlobalToString().stringToBytes(value), key));
    }

    /**
     * Decrypts a byte array with the previously set key.
     *
     * @param encrypted bytes to decrypt
     * @return decrypted bytes
     */
    public byte[] decrypt(byte[] encrypted) {
        if (cryptoKey == null) {
            throw new IllegalArgumentException("Crypto key has to be set first");
        }

        return decrypt(encrypted, cryptoKey.getKey());
    }

    /**
     * Decrypts a byte array.
     *
     * @param encrypted bytes to decrypt
     * @param key       crypto key seed
     * @return decrypted bytes
     */
    public byte[] decrypt(byte[] encrypted, String key) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes(key), algorithm);
            Cipher cipher = Cipher.getInstance(mode);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(ivBytes(key)));
            return cipher.doFinal(encrypted);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Encryption Error", ex);
        }
    }

    protected byte[] keyBytes(String seed) {
        return JabHasher.getGlobalHasher().wrapBytes(seed.getBytes(StandardCharsets.UTF_8), keyLen);
    }

    protected byte[] ivBytes(String seed) {
        return JabHasher.getGlobalHasher().wrapBytes(seed.getBytes(StandardCharsets.UTF_8), ivLen);
    }

    @Data
    @AllArgsConstructor
    protected class CryptoKey {
        private final String key;
        private final String salt;

        String getKeyWithSalt() {
            return key + (salt != null ? salt : "");
        }
    }
}
