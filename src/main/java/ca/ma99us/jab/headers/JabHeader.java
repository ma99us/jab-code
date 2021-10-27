package ca.ma99us.jab.headers;

import java.io.IOException;

public interface JabHeader<P> {

    /**
     * Populate header state during barcode creation.
     * @param dto java bean POJO
     * @throws IOException if header creation fails
     */
    void populate(P dto) throws IOException;

    /**
     * Verify header state during barcode parsing
     * @param dto java bean POJO
     * @throws IOException if header validation fails
     */
    void validate(P dto) throws IOException;

    /**
     * Modify Payload barcode portion during barcode creation.
     * @param payload original payload barcode bytes
     * @return modified payload barcode bytes
     * @throws IOException if compression/encryption fails
     */
    byte[] obfuscate(byte[] payload) throws IOException;

    /**
     * Modify Payload barcode portion during barcode parsing.
     * @param payload obfuscated payload barcode bytes
     * @return modified payload barcode bytes
     * @throws IOException if validation/decryption fails
     */
    byte[] deobfuscate(byte[] payload) throws IOException;
}
