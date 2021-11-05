package ca.ma99us.jab;

import javax.crypto.Mac;
import java.util.Arrays;

/**
 * Simple platform-independent way to sign/verify byte arrays.
 */
public class JabSigner extends AbstractSecret<JabSigner> {

    /**
     * Default algorithm.
     */
    public JabSigner() {
        this("HmacSHA256", 32);   // default
    }

    /**
     * @param keyAlgorithm algorithm name
     * @param keyLen    key length in bytes
     */
    public JabSigner(String keyAlgorithm, int keyLen) {
        super(keyAlgorithm, keyLen);
    }

    /**
     * Encrypt a byte array with the previously set Key.
     *
     * @param data bytes to encrypt
     * @return encrypted bytes
     */
    public byte[] sign(byte[] data) {
        try {
            if (privateKey == null) {
                throw new NullPointerException("Crypto key has to be set first");
            }
            Mac mac = Mac.getInstance(keyAlgorithm);
            mac.init(privateKey);
            return mac.doFinal(data);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Signing Error", ex);
        }
    }

    /**
     * Verify that provided signature matches the provided data byte array with the previously set key.
     *
     * @param data      bytes to verify
     * @param signature signature bytes
     * @return decrypted bytes
     */
    public boolean verify(byte[] data, byte[] signature) {
        try {
            if (privateKey == null) {
                throw new NullPointerException("Crypto key has to be set first");
            }
            Mac mac = Mac.getInstance(keyAlgorithm);
            mac.init(privateKey);
            byte[] newSignature = mac.doFinal(data);
            return Arrays.equals(newSignature, signature);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Verification Error", ex);
        }
    }
}
