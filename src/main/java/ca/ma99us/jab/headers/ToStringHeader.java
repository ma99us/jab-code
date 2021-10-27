package ca.ma99us.jab.headers;

import ca.ma99us.jab.JabCrypto;
import lombok.Data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Converts unprintable bytes to printable string and back using default Base64 algorithm.
 * This probably should be the last header in the recipe.
 */
@Data
public class ToStringHeader<P>  extends AbstractHeader<P> {

    @Override
    public byte[] obfuscate(byte[] payload) throws IOException {
        JabCrypto crypto = new JabCrypto();
        return crypto.bytesToString(payload).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] deobfuscate(byte[] payload) throws IOException {
        JabCrypto crypto = new JabCrypto();
        return crypto.stringToBytes(new String(payload));
    }
}
