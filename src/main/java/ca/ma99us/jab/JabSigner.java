package ca.ma99us.jab;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Simple platform-independent way to sign/verify byte arrays.
 */
public class JabSigner {
    private final String algorithm;
    private final int keyLen;

    private CryptoKey cryptoKey;

    /**
     * Default Blowfish algorithm.
     */
    public JabSigner() {
        this("HmacSHA256", 32);   // default
    }

    /**
     * @param algorithm algorithm name
     */
    public JabSigner(String algorithm, int keyLen) {
        this.algorithm = algorithm;
        this.keyLen = keyLen;
    }

    public JabSigner setKeySecrets(String key, String salt) {
        this.cryptoKey = new CryptoKey(key, salt);
        return this;
    }

    /**
     * Unique id of the crypto and it's key
     * @return long integer id
     */
    public Long getSignerId() {
        if (cryptoKey == null) {
            throw new IllegalArgumentException("Crypto key has to be set first");
        }
        return JabHasher.getGlobalHasher().hashString(this.algorithm + cryptoKey.getKeyWithSalt());
    }

    /**
     * Encrypt a byte array with the previously set Key.
     *
     * @param value bytes to encrypt
     * @return encrypted bytes
     */
    public byte[] sign(byte[] value) {
        if (cryptoKey == null) {
            throw new IllegalArgumentException("Crypto key has to be set first");
        }

        return sign(value, cryptoKey.getKey());
    }

    /**
     * Encrypt a byte array.
     *
     * @param data bytes to encrypt
     * @param key   crypto key seed
     * @return encrypted bytes
     */
    public byte[] sign(byte[] data, String key) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes(key), algorithm);
            mac.init(secretKeySpec);
            return mac.doFinal(data);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Signing Error", ex);
        }
    }

    /**
     * Verify that provided signature matches the provided data byte array with the previously set key.
     *
     * @param data bytes to verify
     * @param signature signature bytes
     * @return decrypted bytes
     */
    public boolean verify(byte[] data, byte[] signature) {
        if (cryptoKey == null) {
            throw new IllegalArgumentException("Crypto key has to be set first");
        }

        return verify(data, signature, cryptoKey.getKey());
    }

    /**
     * Verify that provided signature matches the provided data byte array.
     *
     * @param data bytes to verify
     * @param signature signature bytes
     * @param key crypto key seed
     * @return true if signature matches, false otherwise
     */
    public boolean verify(byte[] data, byte[] signature, String key) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes(key), algorithm);
            mac.init(secretKeySpec);
            byte[] newSignature = mac.doFinal(data);
            return Arrays.equals(newSignature, signature);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Verification Error", ex);
        }
    }

    protected byte[] keyBytes(String seed) {
        return JabHasher.getGlobalHasher().wrapBytes(seed.getBytes(StandardCharsets.UTF_8), keyLen);
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
