package ca.ma99us.jab.headers;

import java.io.IOException;

public interface JabHeader<P> {

    /**
     * Populate header state during barcode creation.
     * @param dto java bean POJO
     * @param payload serialized payload
     */
    void populate(P dto, String payload);

    /**
     * Verify header state during barcode parsing
     * @param dto java bean POJO
     * @param payload serialized payload
     * @throws IOException if validation fails
     */
    void validate(P dto, String payload) throws IOException;

    /**
     * Modify Payload barcode portion during barcode creation.
     * @param dto java bean POJO
     * @param payload original payload barcode string
     * @return modified payload barcode string
     */
    String obfuscate(P dto, String payload);

    /**
     * Modify Payload barcode portion during barcode parsing.
     * @param dto java bean POJO
     * @param payload obfuscated payload barcode string
     * @return modified payload barcode string
     * @throws IOException if validation/decryption fails
     */
    String deobfuscate(P dto, String payload) throws IOException;
}
