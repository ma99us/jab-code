package ca.ma99us.jab.headers;

import ca.ma99us.jab.JabParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.bson4jackson.BsonFactory;
import lombok.Data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Encode/decode payload bytes using Google's CBOR algorithm
 * @see <a href="https://github.com/michel-kraemer/bson4jackson">BSON for Jackson</a>
 */
@Data
public class BsonArrayHeader<P> extends AbstractHeader<P> {

    // compress
    @Override
    public byte[] obfuscate(byte[] payload) throws IOException {
        // wrap to turn in to json array string
        JabParser parser = new JabParser();
        String payloadStr = parser.wrap(new String(payload));

        // read the array to List<>
        ObjectMapper jmapper = new ObjectMapper();
        List<Object> list = jmapper.readValue(payloadStr, new TypeReference<List<Object>>() {
        });

        // encode to bson bytes
        ObjectMapper bmapper = new ObjectMapper(new BsonFactory());
        byte[] bytes = bmapper.writeValueAsBytes(list);

        return bytes;
    }

    // uncompress
    @Override
    public byte[] deobfuscate(byte[] payload) throws IOException {
        // decode bson bytes
        ObjectMapper bmapper = new ObjectMapper(new BsonFactory());
        List<Object> list = bmapper.readValue(payload, new TypeReference<List<Object>>() {
        });

        // write as json array string
        ObjectMapper jmapper = new ObjectMapper();
        String payloadStr = jmapper.writeValueAsString(list);

        // unwrap from json array
        JabParser parser = new JabParser();
        payloadStr = parser.unwrap(payloadStr);

        return payloadStr.getBytes(StandardCharsets.UTF_8);
    }
}
