package ca.ma99us.jab.headers;

import ca.ma99us.jab.JabHasher;
import ca.ma99us.jab.JabParser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;

/**
 * Calculates POJO payload checksum and adds it to the header fields
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ChecksumHeader<P> extends AbstractHeader<P> {
    private Long checksum;

    @Override
    public void populate(P dto) throws IOException {
        // just checksum of the DTO JSON values array string. Ignore the payload string.
        JabParser parser = new JabParser();
        checksum = JabHasher.getGlobalHasher().hash(parser.objectValuesToJsonArrayString(dto));
    }

    @Override
    public void validate(P dto) throws IOException {
        // just checksum of the DTO JSON values array string. Ignore the payload string.
        JabParser parser = new JabParser();
        long hash = JabHasher.getGlobalHasher().hash(parser.objectValuesToJsonArrayString(dto));
        if (checksum != hash) {
            throw new IOException("Barcode checksum mismatch; expected " + checksum + ", but got " + hash);
        }
    }
}
