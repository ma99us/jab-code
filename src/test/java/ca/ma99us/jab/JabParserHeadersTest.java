package ca.ma99us.jab;

import ca.ma99us.jab.dummy.DummyDTO;
import ca.ma99us.jab.headers.*;
import ca.ma99us.jab.headers.groups.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;


public class JabParserHeadersTest {

    @Test
    public void noNullsHeaderBarcodeTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);
        NoNullsHeader<DummyDTO> header = new NoNullsHeader<DummyDTO>();

        barcodeTest(header, NoNullsHeader.class, dto, DummyDTO.class, false);
    }

    @Test
    public void checksumHeaderTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);
        ChecksumHeader<DummyDTO> header = new ChecksumHeader<DummyDTO>();

        barcodeTest(header, ChecksumHeader.class, dto, DummyDTO.class, true);
    }

    @Test
    public void signatureHeaderTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);
        SignatureHeader<DummyDTO> header = new SignatureHeader<DummyDTO>();
        header.setSigner(new JabSigner().setSecretKey("SecretSignerKey", "SomeSalt"));

        barcodeTest(header, SignatureHeader.class, dto, DummyDTO.class, true);
    }

    @Test
    public void cryptoHeaderTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);
        CryptoHeaderGroup<DummyDTO> header = new CryptoHeaderGroup<DummyDTO>();

        try {
            // no encryption key, this should fail
            JabParser jabParser = new JabParser();
            jabParser.objectToJab(header, dto);
            Assert.fail();
        } catch (IOException ex) {
            System.out.println("Expected exception: " + ex.getMessage());
        }

        System.out.println("\nNoNulls, then Crypto:");
        // set the key
        header.getCryptoHeader().setCrypto(new JabCrypto().setSecretKey("SomeSuperSecretKey", "SomeSalt"));

        barcodeTest(header, CryptoHeaderGroup.class, dto, DummyDTO.class, true);

        // compressed version
        System.out.println("\nNoNulls, then Compress, then Crypto:");
        NoNullsCompressCryptoHeaderGroup<DummyDTO> header1 = new NoNullsCompressCryptoHeaderGroup<DummyDTO>();
        header1.getCryptoHeader().setCrypto(new JabCrypto().setRandomKey());

        barcodeTest(header1, NoNullsCompressCryptoHeaderGroup.class, dto, DummyDTO.class, true);

        // compressed cbor version
        System.out.println("\nCbor, then Compress, then Crypto:");
        CborCompressCryptoHeaderGroup<DummyDTO> header2 = new CborCompressCryptoHeaderGroup<DummyDTO>();
        header2.getCryptoHeader().setCrypto(new JabCrypto().setSecretKey("SomeSuperSecretKey", "SomeSalt"));

        barcodeTest(header2, CborCompressCryptoHeaderGroup.class, dto, DummyDTO.class, true);

        // compressed cbor version
        System.out.println("\nCbor, then Crypto then Compressed:");
        CborCryptoCompressHeaderGroup<DummyDTO> header3 = new CborCryptoCompressHeaderGroup<DummyDTO>();
        header3.getCryptoHeader().setCrypto(new JabCrypto().setRandomKey());

        barcodeTest(header3, CborCryptoCompressHeaderGroup.class, dto, DummyDTO.class, true);
    }

    @Test
    public void compressHeaderTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);
        CompressHeaderGroup<DummyDTO> header = new CompressHeaderGroup<DummyDTO>();

        barcodeTest(header, CompressHeaderGroup.class, dto, DummyDTO.class, true);
    }

    @Test
    public void cborHeaderTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);
        CborHeaderGroup<DummyDTO> header = new CborHeaderGroup<DummyDTO>();

        barcodeTest(header, CborHeaderGroup.class, dto, DummyDTO.class, true);

        // compressed version
        System.out.println("\nCompressed:");
        CompressCborHeaderGroup<DummyDTO> header1 = new CompressCborHeaderGroup<DummyDTO>();
        barcodeTest(header1, CompressCborHeaderGroup.class, dto, DummyDTO.class, true);
    }

    @Test
    public void bsonHeaderTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);
        BsonHeaderGroup<DummyDTO> header = new BsonHeaderGroup<DummyDTO>();

        barcodeTest(header, BsonHeaderGroup.class, dto, DummyDTO.class, true);

        // compressed version
        System.out.println("\nCompressed:");
        CompressBsonHeaderGroup<DummyDTO> header1 = new CompressBsonHeaderGroup<DummyDTO>();
        barcodeTest(header1, CompressBsonHeaderGroup.class, dto, DummyDTO.class, true);
    }

    @Test
    public void messagePackHeaderTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);
        MessagePackHeaderGroup<DummyDTO> header = new MessagePackHeaderGroup<DummyDTO>();

        barcodeTest(header, MessagePackHeaderGroup.class, dto, DummyDTO.class, true);
    }

    @Test
    public void cryptoKeysFromStringSecretTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);

        JabParser jabParser = new JabParser();
        JabCrypto encrypt = new JabCrypto().setSecretKey("someCoolSecretKey", null);
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

        // unregister old one
        JabCrypto oldCrypto = CryptoHeader.getDecryptors().unregister(encrypt);
        Assert.assertNotNull(oldCrypto);

        // register new key from the key bytes
        JabCrypto decrypt = new JabCrypto().setSecretKey("someCoolSecretKey", null);
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

        // unregister old one
        oldCrypto = CryptoHeader.getDecryptors().unregister(decrypt);
        Assert.assertNotNull(oldCrypto);

        // register new key from the key bytes
        JabCrypto wrongDecrypt = new JabCrypto().setSecretKey("someWrongSecretKey", null);
        CryptoHeader.getDecryptors().register(wrongDecrypt);

        // parse it back
        res = null;
        try {
            res = jabParser.jabToObject(barcode, CryptoHeaderGroup.class, DummyDTO.class);
            Assert.fail();
        } catch (IOException e) {
            System.out.println("Expected exception: " + e.getMessage());
        }
        Assert.assertNull(res);
    }

    @Test
    public void cryptoKeysFromBytesTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);

        JabParser jabParser = new JabParser();
        JabCrypto encrypt = new JabCrypto().setRandomKey();
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

        // unregister old one
        JabCrypto oldCrypto = CryptoHeader.getDecryptors().unregister(encrypt);
        Assert.assertNotNull(oldCrypto);

        // register new decrypt key from the key bytes
        CryptoHeader.getDecryptors().register(new JabCrypto().setPrivateKeyBytes(keyBytes));

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
    public void cryptoAsyncKeysTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);

        JabParser jabParser = new JabParser();
        JabAsyncCrypto encrypt = new JabAsyncCrypto().setRandomKey();
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
        JabAsyncCrypto decrypt = new JabAsyncCrypto().setPrivateKeyBytes(keyBytes);
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

    private <H extends JabHeader<P>, P> P barcodeTest(H header, Class<H> hClass, P dto, Class<P> pClass, boolean withValidation) {
        JabParser jabParser = new JabParser();

        // generate barcode
        String barcode = null;
        try {
            barcode = jabParser.objectToJab(header, dto);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertNotNull(barcode);
        System.out.println("barcode: (" + barcode.length() + " bytes): \"" + barcode + "\"");
        Assert.assertTrue(barcode.startsWith(JabParser.PREFIX + JabParser.DELIMITER));

        // parse it back
        P res = null;
        try {
            res = jabParser.jabToObject(barcode, hClass, pClass);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        System.out.println(res);
        Assert.assertNotNull(res);
        Assert.assertEquals(dto, res);

        if (withValidation) {
            // alter the barcode!
            int length = barcode.length();
            int p0 = barcode.lastIndexOf("[");
            Assert.assertTrue(p0 > 0);
            int i = p0 + (int) (0.5 * (length - p0));
            StringBuilder sb = new StringBuilder(barcode);
            char c = sb.charAt(i);
            c = c == 'A' ? 'B' : 'A';
            sb.setCharAt(i, c);
            String altBarcode = sb.toString();
            Assert.assertNotEquals(barcode, altBarcode);

            // try to decode it again (it should fail!)
            Object noRes = null;
            try {
                noRes = jabParser.jabToObject(altBarcode, hClass, pClass);
                // should not get here!
                Assert.fail();
            } catch (IOException e) {
                // expected exception
                System.out.println("Expected exception: " + e.getMessage());
            }
            Assert.assertNull(noRes);
        }

        return res;
    }
}
