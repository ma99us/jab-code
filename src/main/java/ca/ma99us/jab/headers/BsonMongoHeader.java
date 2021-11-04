package ca.ma99us.jab.headers;

import ca.ma99us.jab.JabParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.BasicBSONDecoder;
import org.bson.BasicBSONEncoder;
import org.bson.BasicBSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Encode/decode payload bytes using Google's CBOR algorithm
 * @see <a href="https://www.mongodb.com/basics/bson">MongoDB BSON</a>
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BsonMongoHeader<P> extends AbstractHeader<P> {

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
        //BasicDBObject dbObject = BasicDBObject.parse(payloadStr);
        BasicDBList dbObject = new BasicDBList();
        dbObject.addAll(list);
        BasicBSONEncoder encoder = new BasicBSONEncoder();
        byte[] bytes = encoder.encode(dbObject);

        return bytes;
    }

    // uncompress
    @Override
    public byte[] deobfuscate(byte[] payload) throws IOException {
        // decode bson bytes
        BasicBSONDecoder decoder = new BasicBSONDecoder();
        BasicBSONObject bsonObject = (BasicBSONObject)decoder.readObject(payload);
        ArrayList<Object> list = new ArrayList<>(bsonObject.values());

        // write as json array string
        ObjectMapper jmapper = new ObjectMapper();
        String payloadStr = jmapper.writeValueAsString(list);

        // unwrap from json array
        payloadStr = JabParser.unwrap(payloadStr);

        return payloadStr.getBytes(StandardCharsets.UTF_8);
    }
}
