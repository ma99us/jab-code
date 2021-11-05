package ca.ma99us.jab;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;

/**
 * Simple platform-independent way to encrypt/decrypt byte arrays.
 */
public class JabCrypto extends AbstractSecret<JabCrypto> {
    protected final String mode;
    protected final int ivLen;

    /**
     * Default Blowfish algorithm.
     */
    public JabCrypto() {
        this("Blowfish", "Blowfish/CBC/PKCS5Padding", 16, 8);   // default
    }

    /**
     * @param algorithm algorithm name
     * @param mode      algorithm mode
     * @param keyLen    key length in bytes
     * @param ivLen     iV length in bytes
     */
    public JabCrypto(String algorithm, String mode, int keyLen, int ivLen) {
        super(algorithm, keyLen);
        this.mode = mode;
        this.ivLen = ivLen;
    }

    /**
     * Encrypt a byte array with the previously set Key.
     *
     * @param value bytes to encrypt
     * @return encrypted bytes
     */
    public byte[] encrypt(byte[] value) {
        if (privateKey == null) {
            throw new NullPointerException("Crypto key has to be set first");
        }

        return encrypt(value, privateKey);
    }

    protected byte[] encrypt(byte[] value, Key key) {
        try {
            Cipher cipher = providerName != null ? Cipher.getInstance(mode, providerName) : Cipher.getInstance(mode);
            if (ivLen > 0) {
                byte[] ivBytes = wrapBytes(key.getEncoded(), ivLen);
                cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(ivBytes));
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, key);
            }
            return cipher.doFinal(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Encryption Error", ex);
        }
    }

    /**
     * Decrypts a byte array with the previously set key.
     *
     * @param encrypted bytes to decrypt
     * @return decrypted bytes
     */
    public byte[] decrypt(byte[] encrypted) {
        if (privateKey == null) {
            throw new NullPointerException("Crypto key has to be set first");
        }

        return decrypt(encrypted, privateKey);
    }

    protected byte[] decrypt(byte[] encrypted, Key key) {
        try {
            Cipher cipher = providerName != null ? Cipher.getInstance(mode, providerName) : Cipher.getInstance(mode);
            if (ivLen > 0) {
                byte[] ivBytes = wrapBytes(key.getEncoded(), ivLen);
                cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivBytes));
            } else {
                cipher.init(Cipher.DECRYPT_MODE, key);
            }
            return cipher.doFinal(encrypted);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Decryption Error", ex);
        }
    }
}
