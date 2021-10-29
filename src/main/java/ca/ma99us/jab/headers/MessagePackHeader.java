package ca.ma99us.jab.headers;

import ca.ma99us.jab.JabParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Encode/decode payload bytes using Google's CBOR algorithm
 * @see <a href="https://github.com/msgpack/msgpack-java">MessagePack</a>
 */
@Data
public class MessagePackHeader<P> extends AbstractHeader<P> {

    // compress
    @Override
    public byte[] obfuscate(byte[] payload) throws IOException {
        // wrap to turn in to json array string
        String payloadStr = JabParser.wrap(new String(payload));

        // read the array to List<>
        ObjectMapper jmapper = new ObjectMapper();
        List<Object> list = jmapper.readValue(payloadStr, new TypeReference<List<Object>>() {
        });

        // encode to bson bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectMapper bmapper = new ObjectMapper(new MessagePackFactory());
        byte[] bytes = bmapper.writeValueAsBytes(list);

        return bytes;
    }

    // uncompress
    @Override
    public byte[] deobfuscate(byte[] payload) throws IOException {
        // decode bson bytes
        ObjectMapper bmapper = new ObjectMapper(new MessagePackFactory());
        List<Object> list = bmapper.readValue(payload, new TypeReference<List<Object>>() {
        });

        // write as json array string
        ObjectMapper jmapper = new ObjectMapper();
        String payloadStr = jmapper.writeValueAsString(list);

        // unwrap from json array
        payloadStr = JabParser.unwrap(payloadStr);

        return payloadStr.getBytes(StandardCharsets.UTF_8);
    }
}
