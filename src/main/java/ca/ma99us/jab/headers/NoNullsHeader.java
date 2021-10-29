package ca.ma99us.jab.headers;

import ca.ma99us.jab.JabParser;
import lombok.Data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Remove/adds back 'null's from json array string to make payload bytes a bit shorter.
 */
@Data
public class NoNullsHeader<P> extends AbstractHeader<P> {

    /**
     * Shrinks 'null's from json array
     */
    @Override
    public byte[] obfuscate(byte[] payload) throws IOException {
        // shrink the nulls
        String str = JabParser.wrap(new String(payload));
        Pattern regex = Pattern.compile("([\\[,])(null)([,\\]])");
        Matcher matcher = regex.matcher(str);
        while (matcher.find()) {
            str = matcher.replaceAll("$1$3");
            matcher = regex.matcher(str);
        }
        return JabParser.unwrap(str).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Inflates 'null's back into json array
     */
    @Override
    public byte[] deobfuscate(byte[] payload) throws IOException {
        // expand nulls back
        String str = JabParser.wrap(new String(payload));
        Pattern regex = Pattern.compile("([\\[,])([,\\]])");
        Matcher matcher = regex.matcher(str);
        while (matcher.find()) {
            str = matcher.replaceAll("$1null$2");
            matcher = regex.matcher(str);
        }
        return JabParser.unwrap(str).getBytes(StandardCharsets.UTF_8);
    }
}
