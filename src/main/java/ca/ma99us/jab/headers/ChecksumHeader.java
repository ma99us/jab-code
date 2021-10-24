package ca.ma99us.jab.headers;

import ca.ma99us.jab.JabParser;

import java.io.IOException;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class ChecksumHeader<P> extends NoNullHeader<P> {
    private Long checksum;

    @Override
    public void populate(P dto, String payload) {
        //TODO: maybe use original DTO to get the checksum rather then payload barcode portion string?
        JabParser.Hasher hasher = new JabParser.Hasher();
        checksum = hasher.hashString(payload);
    }

    @Override
    public void validate(P dto, String payload) throws IOException {
        //TODO: maybe use resulting DTO to get the checksum rather then payload barcode portion string?
        JabParser.Hasher hasher = new JabParser.Hasher();
        long hash = hasher.hashString(payload);
        if (checksum != hash) {
            throw new IOException("Barcode checksum mismatch; expected " + checksum + ", but got " + hash);
        }
    }
}
