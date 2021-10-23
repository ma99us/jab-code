package org.maggus.jab.headers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.maggus.jab.JabParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper=true)
public class CryptoChecksumHeader extends ChecksumHeader {
    private Long keyId;

    @Getter
    private final static Keys keys = new Keys();

    @Override
    public String obfuscate(Object dto, String payload) {
        // super shrinks 'null's
        payload = super.obfuscate(dto, payload);

        // populate key id
        if(keys.encryptKey == null){
            throw new IllegalArgumentException("Encryption key has to be specified. Set CryptoChecksumHeader.Registry.encryptKey(...) first");
        }
        keyId = keys.encryptKey.getKeyId();

        // encrypt the payload
        String encrypted = new JabParser.Crypto().encryptString(payload, keys.encryptKey.getKeyWithSalt());

        // wrap in "[", "]"
        return "[" + encrypted + "]";
    }

    @Override
    public String deobfuscate(Object dto, String payload) throws IOException {
        // validate key id first
        Keys.CryptoKey key = keys.findKey(keyId);
        if (key == null) {
            throw new IOException("Not registered key id: " + keyId);
        }

        // trim off "[", "]";
        payload = payload.substring(1, payload.length() - 1);

        //decrypt payload
        String decrypted = new JabParser.Crypto().decryptString(payload, key.getKeyWithSalt());
        if (!decrypted.startsWith("[") || !decrypted.endsWith("]")) {
            throw new IOException("Bad decrypted payload");
        }

        // super inflates 'null's back
        decrypted = super.deobfuscate(dto, decrypted);

        return decrypted;
    }

    /**
     * Simple collection of registered crypo keys and salts.
     * Finds crypto key from the barcode header key id.
     */
    public static class Keys {
        private final Map<Long, CryptoKey> keys = new HashMap<>();
        private CryptoKey encryptKey;

        public Keys encryptKey(String key, String salt) {
            encryptKey = new CryptoKey(key, salt);
            decryptKey(key, salt);  // also register this key for decryption
            return this;
        }

        public synchronized Keys decryptKey(String key, String salt) {
            CryptoKey cryptoKey = new CryptoKey(key, salt);
            keys.put(cryptoKey.getKeyId(), cryptoKey);
            return this;
        }

        public synchronized CryptoKey findKey(Long id) {
            return id != null ? keys.get(id) : null;
        }

        @Data
        @AllArgsConstructor
        public static class CryptoKey {
            private final String key;
            private final String salt;

            String getKeyWithSalt(){
                return key + (salt != null ? salt : "");
            }

            public long getKeyId() {
                return new JabParser.Hasher().hashString(getKeyWithSalt());
            }
        }
    }
}
