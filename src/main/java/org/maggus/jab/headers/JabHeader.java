package org.maggus.jab.headers;

import java.io.IOException;

public interface JabHeader {

    /**
     * Populate header state during barcode creation.
     * @param dto
     * @param payload
     * @return
     */
    void populate(Object dto, String payload);

    /**
     * Verify header state during barcode parsing
     * @param dto
     * @param payload
     * @throws IOException
     */
    void validate(Object dto, String payload) throws IOException;

    /**
     * Modify Payload barcode portion during barcode creation.
     * @param dto
     * @param payload original payload barcode string
     * @return modified payload barcode string
     */
    String obfuscate(Object dto, String payload);

    /**
     * Modify Payload barcode portion during barcode parsing.
     * @param dto
     * @param payload obfuscated payload barcode string
     * @return modified payload barcode string
     */
    String deobfuscate(Object dto, String payload) throws IOException ;
}
