package ca.ma99us.jab.headers;

import ca.ma99us.jab.JabCrypto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Encrypts/decrypts payload bytes using default symmetrical cypher with a string Key
 * @param <P> generic payload class
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CryptoHeader<P> extends AbstractHeader<P> {
    private Long keyId;

    @Getter
    private final static Cryptos cryptos = new Cryptos();
    @JsonIgnore
    private JabCrypto encrypt;

    public CryptoHeader<P> setCrypto(JabCrypto encrypt) {
        this.encrypt = encrypt;
        cryptos.registerCrypto(this.encrypt);   // also register globally for decryption
        return this;
    }

    @Override
    public void populate(P dto) throws IOException {
        if(encrypt == null){
            throw new IOException("Encryption Crypto has to be specified. Set CryptoHeader.setEncrypt(...) first");
        }

        // populate key id
        keyId = encrypt.getKeyId();
    }

    @Override
    public byte[] obfuscate(byte[] payload) throws IOException{
        if(encrypt == null){
            throw new IOException("Encryption Crypto has to be specified. Set CryptoHeader.setEncrypt(...) first");
        }

        // encrypt the payload
        return encrypt.encrypt(payload);
    }

    @Override
    public byte[] deobfuscate(byte[] payload) throws IOException {
        // validate the key id first
        JabCrypto decrypt = cryptos.findCrypto(keyId);
        if (decrypt == null) {
            throw new IOException("Not registered key id: " + keyId);
        }

        //decrypt the payload
        return decrypt.decrypt(payload);
    }

    /**
     * Simple collection of registered crypo keys and salts.
     * Finds crypto key from the barcode header key id.
     */
    public static class Cryptos {
        private final Map<Long, JabCrypto> keyIdCryptos = new HashMap<>();

        public Cryptos registerCrypto(JabCrypto crypto) {
            keyIdCryptos.put(crypto.getKeyId(), crypto);
            return this;
        }

        public JabCrypto unregister(JabCrypto crypto) {
            return keyIdCryptos.remove(crypto.getKeyId());
        }

        public synchronized JabCrypto findCrypto(Long id) {
            return id != null ? keyIdCryptos.get(id) : null;
        }
    }
}
