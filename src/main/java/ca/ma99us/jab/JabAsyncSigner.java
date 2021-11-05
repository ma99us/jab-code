package ca.ma99us.jab;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Sign/verify byte arrays with asymmetrical keys.
 */
public class JabAsyncSigner extends JabSigner {
    protected final String mode;
    protected Key publicKey;

    /**
     * Default algorithm.
     */
    public JabAsyncSigner() {
        this("ECDSA", "SHA256withECDSA", 24);   // default
    }

    /**
     * @param keyAlgorithm algorithm name
     * @param mode         algorithm mode
     * @param keyLen       key length in bytes
     */
    public JabAsyncSigner(String keyAlgorithm, String mode, int keyLen) {
        super(keyAlgorithm, keyLen);
        this.mode = mode;
    }

    @Override
    public JabAsyncSigner setRandomKey() {
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

    @Override
    public Long getKeyId() {
        if (publicKey == null) {
            throw new IllegalStateException("Public key has to be set first");
        }

        return JabHasher.getGlobalHasher().hash(this.keyAlgorithm.getBytes(StandardCharsets.UTF_8), publicKey.getEncoded());

    }

    public byte[] getPublicKeyBytes() {
        return publicKey != null ? publicKey.getEncoded() : null;
    }

    public JabAsyncSigner setPublicKeyBytes(byte[] keyBytes) {
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
    public JabAsyncSigner setPrivateKeyBytes(byte[] keyBytes) {
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
     * @param data bytes to encrypt
     * @return encrypted bytes
     */
    public byte[] sign(byte[] data) {
        try {
            if (privateKey == null) {
                throw new NullPointerException("Private key has to be set first");
            }
            Signature sig = providerName != null ? Signature.getInstance(mode, providerName) : Signature.getInstance(mode);
            sig.initSign((PrivateKey) privateKey);
            sig.update(data);
            return sig.sign();
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
            if (publicKey == null) {
                throw new NullPointerException("Public key has to be set first");
            }
            Signature sig = providerName != null ? Signature.getInstance(mode, providerName) : Signature.getInstance(mode);
            sig.initVerify((PublicKey) publicKey);
            sig.update(data);
            return sig.verify(signature);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Verification Error", ex);
        }
    }
}
