package ca.ma99us.jab.headers;

import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

/**
 * Compresses/uncompresses payload bytes using default ZIP algorithm
 */
@Data
public class CompressHeader<P> extends AbstractHeader<P> {

    // compress
    @Override
    public byte[] obfuscate(byte[] payload) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (DeflaterOutputStream dos = new DeflaterOutputStream(os, new Deflater(Deflater.DEFAULT_COMPRESSION))) {
            dos.write(payload);
        }
        return os.toByteArray();
    }

    // uncompress
    @Override
    public byte[] deobfuscate(byte[] payload) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (OutputStream ios = new InflaterOutputStream(os)) {
            ios.write(payload);
        }
        return os.toByteArray();
    }
}
