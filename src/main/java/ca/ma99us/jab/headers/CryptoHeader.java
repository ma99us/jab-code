package ca.ma99us.jab.headers;

import ca.ma99us.jab.JabCrypto;
import ca.ma99us.jab.JabHasher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Encrypts/decrypts payload bytes using default symmetrical cypher with a string Key
 * @param <P>
 */
@Data
public class CryptoHeader<P> extends AbstractHeader<P> {
    private Long keyId;

    @Getter
    private final static Keys keys = new Keys();

    @Override
    public void populate(P dto) throws IOException {
        // populate key id
        if(keys.encryptKey == null){
            throw new IOException("Encryption key has to be specified. Set CryptoChecksumHeader.Registry.encryptKey(...) first");
        }
        keyId = keys.encryptKey.getKeyId();
    }

    @Override
    public byte[] obfuscate(byte[] payload) throws IOException{
        // encrypt the payload
        return new JabCrypto().encrypt(payload, keys.encryptKey.getKey());
    }

    @Override
    public byte[] deobfuscate(byte[] payload) throws IOException {
        // validate the key id first
        Keys.CryptoKey key = keys.findKey(keyId);
        if (key == null) {
            throw new IOException("Not registered key id: " + keyId);
        }

        //decrypt the payload
        return new JabCrypto().decrypt(payload, key.getKey());
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
                return new JabHasher().hashString(getKeyWithSalt());
            }
        }
    }
}
