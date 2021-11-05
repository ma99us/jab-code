package ca.ma99us.jab;

import ca.ma99us.jab.dummy.DummyDTO;
import ca.ma99us.jab.headers.CryptoHeader;
import ca.ma99us.jab.headers.SignatureHeader;
import ca.ma99us.jab.headers.groups.CryptoHeaderGroup;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.security.Provider;
import java.util.Arrays;


public class BouncyCastleProviderTest {

    @Test
    public void esCryptoTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);

        // setup crypto
        JabAsyncCrypto.setCryptoConfig(new AbstractSecret.CryptoConfig() {
            @Override
            public Provider getSecurityProvider() {
                return new BouncyCastleProvider();
            }
        });

        //TODO: EC might need newer JRE, for JRE 1.8 or older see https://www.oracle.com/ca-en/java/technologies/javase-jce8-downloads.html
        JabAsyncCrypto encrypt = new JabAsyncCrypto("EC", "ECIES", 24, 0).setRandomKey();

        JabParser jabParser = new JabParser();
        byte[] keyBytes = encrypt.getPrivateKeyBytes();
        System.out.println("key bytes (" + keyBytes.length * 8 + " bits): " + Arrays.toString(keyBytes));

        // generate barcode
        String barcode = null;
        try {
            CryptoHeaderGroup<DummyDTO> header = new CryptoHeaderGroup<DummyDTO>();
            header.getCryptoHeader().setCrypto(encrypt);
            barcode = jabParser.objectToJab(header, dto);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
        Assert.assertNotNull(barcode);
        System.out.println("barcode: (" + barcode.length() + " bytes): \"" + barcode + "\"");

        // unregister old one
        JabCrypto oldCrypto = CryptoHeader.getDecryptors().unregister(encrypt);
        Assert.assertNotNull(oldCrypto);

        // register new decrypt key from the key bytes
        JabAsyncCrypto decrypt = new JabAsyncCrypto("EC", "ECIES", 24, 0).setPrivateKeyBytes(keyBytes);
        CryptoHeader.getDecryptors().register(decrypt);

        // parse it back
        DummyDTO res = null;
        try {
            res = jabParser.jabToObject(barcode, CryptoHeaderGroup.class, DummyDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
        Assert.assertNotNull(res);
        Assert.assertEquals(dto, res);
    }

    @Test
    public void esSignTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);

        // setup crypto
        JabAsyncSigner.setCryptoConfig(new AbstractSecret.CryptoConfig() {
            @Override
            public Provider getSecurityProvider() {
                return new BouncyCastleProvider();
            }
        });

        //TODO: EC might need newer JRE, for JRE 1.8 or older see https://www.oracle.com/ca-en/java/technologies/javase-jce8-downloads.html
        JabAsyncSigner signer = new JabAsyncSigner("ECDSA", "SHA256withECDSA", 24).setRandomKey();

        JabParser jabParser = new JabParser();
        byte[] keyBytes = signer.getPublicKeyBytes();
        System.out.println("key bytes (" + keyBytes.length * 8 + " bits): " + Arrays.toString(keyBytes));

        // generate barcode
        String barcode = null;
        try {
            SignatureHeader<DummyDTO> header = new SignatureHeader<DummyDTO>();
            header.setSigner(signer);
            barcode = jabParser.objectToJab(header, dto);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
        Assert.assertNotNull(barcode);
        System.out.println("barcode: (" + barcode.length() + " bytes): \"" + barcode + "\"");

        // unregister old one
        JabSigner old = SignatureHeader.getVerifiers().unregister(signer);
        Assert.assertNotNull(old);

        // register new verifier key from the key bytes
        JabAsyncSigner verifier = new JabAsyncSigner("ECDSA", "SHA256withECDSA", 24).setPublicKeyBytes(keyBytes);
        SignatureHeader.getVerifiers().register(verifier);

        // parse it back
        DummyDTO res = null;
        try {
            res = jabParser.jabToObject(barcode, SignatureHeader.class, DummyDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
        Assert.assertNotNull(res);
        Assert.assertEquals(dto, res);

        // alter the barcode and parse it back again (should fail)
        barcode = barcode.replace("Some Name", "Some Other Name");

        res = null;
        try {
            res = jabParser.jabToObject(barcode, SignatureHeader.class, DummyDTO.class);
            Assert.fail();
        } catch (IOException e) {
            System.out.println("Expected exception: " + e.getMessage());
        }
        Assert.assertNull(res);
    }
}
