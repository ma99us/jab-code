package ca.ma99us.jab.headers;

import ca.ma99us.jab.JabParser;
import com.google.iot.cbor.CborArray;
import com.google.iot.cbor.CborObject;
import com.google.iot.cbor.CborParseException;
import lombok.Data;
import org.json.JSONArray;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Encode/decode payload bytes using Google's CBOR algorithm
 * @see <a href="https://github.com/google/cbortree">CborTree</a>
 */
@Data
public class CborArrayHeader<P> extends AbstractHeader<P> {

    // compress
    @Override
    public byte[] obfuscate(byte[] payload) throws IOException {
        // wrap to turn in to json array string
        String payloadStr = JabParser.wrap(new String(payload));

        // encode to cbor bytes
        CborArray cbor = CborArray.createFromJSONArray(new JSONArray(payloadStr));
        return cbor.toCborByteArray();
    }

    // uncompress
    @Override
    public byte[] deobfuscate(byte[] payload) throws IOException {
        // decode cbor bytes
        CborObject cbor = null;
        try {
            cbor = CborArray.createFromCborByteArray(payload);
        } catch (CborParseException e) {
            throw new IOException(e);
        }

        // unwrap from json array
        String payloadStr = JabParser.unwrap(cbor.toJsonString());

        return payloadStr.getBytes(StandardCharsets.UTF_8);
    }
}
