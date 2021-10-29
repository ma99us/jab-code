package ca.ma99us.jab.headers;

import java.io.IOException;

/**
 * Does nothing. Just an interface adapter
 */
public abstract class AbstractHeader<P> implements JabHeader<P> {

    @Override
    public void populate(P dto) throws IOException {
        // no-op
    }

    @Override
    public void validate(P dto) throws IOException {
        // no-op
    }

    @Override
    public byte[] obfuscate(byte[] payload) throws IOException {
        return payload;  // no-op
    }

    @Override
    public byte[] deobfuscate(byte[] payload) throws IOException {
        return payload;  // no-op
    }
}
