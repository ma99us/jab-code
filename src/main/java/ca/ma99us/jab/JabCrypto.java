package ca.ma99us.jab;

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
    private final JabHasher hasher = new JabHasher();

    /**
     * Default Blowfish algorithm.
     */
    public JabCrypto() {
        this("Blowfish", "Blowfish/CBC/PKCS5Padding", 16, 8);   // default
    }

    /**
     * @param algorithm algorithm name
     * @param mode algorithm mode
     * @param keyLen key length in bytes
     * @param ivLen iV length in bytes
     */
    public JabCrypto(String algorithm, String mode, int keyLen, int ivLen) {
        this.algorithm = algorithm;
        this.mode = mode;
        this.keyLen = keyLen;
        this.ivLen = ivLen;
    }

    /**
     * Encrypt a string to human-readable string
     * @param value string to encrypt
     * @param key crypto key seed
     * @return encrypted string
     */
    public String encryptString(String value, String key) {
        return bytesToString(encrypt(value.getBytes(StandardCharsets.UTF_8), key));
    }

    /**
     * Encrypt a byte array.
     * @param value bytes to encrypt
     * @param key crypto key seed
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
     * @param value strng to decrypt
     * @param key crypto key seed
     * @return decrypted string
     */
    public String decryptString(String value, String key) {
        return new String(decrypt(stringToBytes(value), key));
    }

    /**
     * Decrypts a byte array.
     * @param encrypted bytes to decrypt
     * @param key crypto key seed
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

    protected byte[] keyBytes(String seed){
        return hasher.wrapBytes(seed.getBytes(StandardCharsets.UTF_8), keyLen);
    }

    protected byte[] ivBytes(String seed){
        return hasher.wrapBytes(seed.getBytes(StandardCharsets.UTF_8), ivLen);
    }

    /**
     * Convert arbitrary bytes to human-readable string. Base64 be default.
     * @param bytes byte array
     * @return string
     */
    public String bytesToString(byte[] bytes){
        // use URL_SAFE, NO_WRAP standards
        return Base64.getEncoder().encodeToString(bytes);   //TODO: this might not work on Android or older Java
    }

    /**
     * Convert a string representation back to byte array. Base64 be default.
     * @param string bytes string representation
     * @return bytes
     */
    public byte[] stringToBytes(String string) {
        // use URL_SAFE, NO_WRAP standards
        return Base64.getDecoder().decode(string);        //TODO: this might not work on Android or older Java
    }
}
