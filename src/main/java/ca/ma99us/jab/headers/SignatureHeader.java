package ca.ma99us.jab.headers;

import ca.ma99us.jab.JabParser;
import ca.ma99us.jab.JabSigner;
import ca.ma99us.jab.JabToString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Encrypts/decrypts payload bytes using default symmetrical cypher with a string Key
 *
 * @param <P> generic payload class
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SignatureHeader<P> extends AbstractHeader<P> {
    private Long keyId;
    private String signature;

    @Getter
    private final static Verifiers verifiers = new Verifiers();

    @JsonIgnore
    private JabSigner signer;

    public SignatureHeader<P> setSigner(JabSigner signer) {
        this.signer = signer;
        verifiers.register(this.signer);   // also register globally for verification
        return this;
    }

    @Override
    public void populate(P dto) throws IOException {
        if (signer == null) {
            throw new IOException("Signature Crypto has to be specified. Set SignatureHeader.setSigner(...) first");
        }

        // populate key id
        keyId = signer.getKeyId();

        // populate signature
        JabParser parser = new JabParser();
        String payloadStr = parser.objectValuesToJsonArrayString(dto);
        byte[] signBytes = signer.sign(payloadStr.getBytes(StandardCharsets.UTF_8));
        signature = JabToString.getGlobalToString().bytesToString(signBytes);
    }

    @Override
    public void validate(P dto) throws IOException {
        // validate the key id first
        JabSigner verifier = verifiers.find(keyId);
        if (verifier == null) {
            throw new IOException("Not registered key id: " + keyId);
        }

        // verify signature
        byte[] signBytes = JabToString.getGlobalToString().stringToBytes(signature);
        JabParser parser = new JabParser();
        String payloadStr = parser.objectValuesToJsonArrayString(dto);
        boolean verify = verifier.verify(payloadStr.getBytes(StandardCharsets.UTF_8), signBytes);
        if (!verify) {
            throw new IOException("Signature verification failed");
        }
    }

    /**
     * Simple collection of registered crypo keys and salts.
     * Finds crypto key from the barcode header key id.
     */
    public static class Verifiers {
        private final Map<Long, JabSigner> keyIdSigners = new HashMap<>();

        public Verifiers register(JabSigner signer) {
            keyIdSigners.put(signer.getKeyId(), signer);
            return this;
        }

        public JabSigner unregister(JabSigner signer) {
            return keyIdSigners.remove(signer.getKeyId());
        }

        public synchronized JabSigner find(Long id) {
            return id != null ? keyIdSigners.get(id) : null;
        }
    }
}
