package ca.ma99us.jab.headers;

import ca.ma99us.jab.JabParser;
import lombok.Data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
