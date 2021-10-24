package ca.ma99us.jab.headers;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;

/**
 * Remove 'null's from json array string to make it a bit shorter
 */
@Data
public class NoNullHeader<P> implements JabHeader<P> {

    @Override
    public void populate(P dto, String payload) {
        // no-op
    }

    @Override
    public void validate(P dto, String payload) throws IOException {
        // no-op
    }

    @Override
    public String obfuscate(P dto, String payload) {
        // shrink the nulls
        Pattern regex = Pattern.compile("([\\[,])(null)([,\\]])");
        Matcher matcher = regex.matcher(payload);
        while (matcher.find()) {
            payload = matcher.replaceAll("$1$3");
            matcher = regex.matcher(payload);
        }
        return payload;
    }

    @Override
    public String deobfuscate(P dto, String payload) throws IOException {
        // expand nulls back
        Pattern regex = Pattern.compile("([\\[,])([,\\]])");
        Matcher matcher = regex.matcher(payload);
        while (matcher.find()) {
            payload = matcher.replaceAll("$1null$2");
            matcher = regex.matcher(payload);
        }
        return payload;
    }
}
