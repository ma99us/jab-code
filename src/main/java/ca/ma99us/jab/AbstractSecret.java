package ca.ma99us.jab;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * Base class for all cryptography implementations
 */
public abstract class AbstractSecret<T extends AbstractSecret<?>> {
    protected final String keyAlgorithm;
    protected final int keyLen;

    protected Key key;

    /**
     * @param keyAlgorithm algorithm name
     * @param keyLen    key length in bytes
     */
    protected AbstractSecret(String keyAlgorithm, int keyLen) {
        this.keyAlgorithm = keyAlgorithm;
        this.keyLen = keyLen;
    }

    public T setKeySecrets(String key, String salt) {
        // generate key from string //TODO: maybe include salt?
        byte[] keyBytes = wrapBytes(key, keyLen);
        this.key = new SecretKeySpec(keyBytes, keyAlgorithm);

        return (T) this;
    }

    /**
     * Unique id of the crypto and it's key
     * @return long integer id
     */
    public Long getKeyId() {
        if (key == null) {
            throw new IllegalStateException("Crypto key has to be set first");
        }

        return JabHasher.getGlobalHasher().hash(this.keyAlgorithm.getBytes(StandardCharsets.UTF_8), key.getEncoded());
    }

    protected byte[] wrapBytes(String string, int length) {
        return wrapBytes(string.getBytes(StandardCharsets.UTF_8), length);
    }

    protected byte[] wrapBytes(byte[] bytes, int length) {
        return JabHasher.getGlobalHasher().wrapBytes(bytes, length);
    }
}
