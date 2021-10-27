package ca.ma99us.jab;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Simple platform-independent way to create hashes from strings.
 */
public class JabHasher {
    private final String ALGORITHM = "SHA-256"; // default
    private final long MAX_SAFE_INTEGER = 9007199254740991L;      // 2^53 - 1 is the maximum "safe" integer for json/javascript

    public long hashString(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));

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
