package ca.ma99us.jab;

import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Simple platform-independent way to create hashes for any data.
 */
public class JabHasher {
    private final String ALGORITHM = "SHA-256";  // default
    private final long MAX_SAFE_INTEGER = 9007199254740991L;  // 2^53 - 1 is the maximum "safe" integer for json/javascript

    @Getter
    @Setter
    private static JabHasher globalHasher;

    // register default global hasher. Different implementation could be injected later.
    static {
        globalHasher = new JabHasher();
    }

    /**
     * Calculate hash code of the given array of byte arrays
     * @param datas array of byte arrays
     * @return long number hash
     */
    public long hash(byte[]... datas) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            for (byte[] data : datas) {
                os.write(data);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Hash Error", e);
        }

        return hash(os.toByteArray());
    }

    /**
     * Calculate hash code of the given string
     *
     * @param data string
     * @return long number hash
     */
    public long hash(String data) {
        if (data == null) {
            throw new IllegalArgumentException("Hash Error; data can not be null");
        }
        return hash(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Calculate hash code of the given bytes
     *
     * @param data bytes array
     * @return long number hash
     */
    public long hash(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] digest = md.digest(data);

            // shrink it to 8 bytes
            digest = wrapBytes(digest, 8);

            // build a long integer value
            long msb = 0;
            for (int i = 0; i < digest.length; i++) {
                msb = (msb << 8) | (digest[i] & 0xff);
            }

            // make it Unsigned and not larger then MAX_SAFE_INTEGER
            return Math.abs(msb) % MAX_SAFE_INTEGER;
        } catch (Exception e) {
            throw new IllegalArgumentException("Hash Error", e);
        }
    }

    /**
     * Wrap byte array onto itself to create different length byte array as a result
     *
     * @param sBytes source byte array
     * @param rLen   length of desired byte array
     * @return new byte array of the given length
     */
    public byte[] wrapBytes(byte[] sBytes, int rLen) {
        int sLen = sBytes.length;
        byte[] rBytes = new byte[rLen];
        for (int step = 0, s = 0, r = 0; step < Math.max(rLen, sLen); step++, s++, r++) {
            s %= sLen;
            r %= rLen;
            rBytes[r] ^= sBytes[s];
        }
        return rBytes;
    }
}
