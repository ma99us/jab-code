package ca.ma99us.jab.headers.groups;

import ca.ma99us.jab.headers.JabHeader;

import java.io.IOException;

/**
 * Collection of headers applied in sequence
 * @param <P> generic payload class
 */
public abstract class AbstractHeaderGroup<P> implements JabHeader<P> {

    /**
     * A sequence of headers to apply during the barcode creation. The order is important!
     * During parsing the reverse order wll be used.
     * @return the headers, in the order they should apply during barcode creation.
     */
    protected abstract JabHeader<P>[] headers();

    @Override
    public void populate(P dto) throws IOException {
        for (JabHeader<P> hdr : headers()) {
            hdr.populate(dto);
        }
    }

    @Override
    public void validate(P dto) throws IOException {
        // in reverse order
        JabHeader<P>[] headers = headers();
        for (int idx = headers.length - 1; idx >= 0; idx--) {
            headers[idx].validate(dto);
        }
    }

    @Override
    public byte[] obfuscate(byte[] payload) throws IOException {
        for (JabHeader<P> hdr : headers()) {
            payload = hdr.obfuscate(payload);
        }
        return payload;
    }

    @Override
    public byte[] deobfuscate(byte[] payload) throws IOException {
        // in reverse order
        JabHeader<P>[] headers = headers();
        for (int idx = headers.length - 1; idx >= 0; idx--) {
            payload = headers[idx].deobfuscate(payload);
        }
        return payload;
    }
}
