package ca.ma99us.jab;

import lombok.Getter;
import lombok.Setter;

import java.util.Base64;

/**
 * Injectable way to convert bytes to valid strings and back. By default it is just Base64.
 * @see <a href="https://en.wikipedia.org/wiki/Base64">Base64 wiki</a>
 */
public class JabToString {

    @Getter
    @Setter
    private static JabToString globalToString;

    // register default global instance. Different implementation could be injected later.
    static {
        globalToString = new JabToString();
    }

    /**
     * Convert arbitrary bytes to human-readable string. Base64 be default.
     *
     * @param bytes byte array
     * @return string
     */
    public String bytesToString(byte[] bytes) {
        // use URL_SAFE, NO_WRAP standards
        return Base64.getEncoder().encodeToString(bytes);   //TODO: this might not work on Android or older Java
    }

    /**
     * Convert a string representation back to byte array. Base64 be default.
     *
     * @param string bytes string representation
     * @return bytes
     */
    public byte[] stringToBytes(String string) {
        // use URL_SAFE, NO_WRAP standards
        return Base64.getDecoder().decode(string);        //TODO: this might not work on Android or older Java
    }
}
