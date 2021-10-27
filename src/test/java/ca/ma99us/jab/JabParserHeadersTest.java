package ca.ma99us.jab;

import ca.ma99us.jab.dummy.DummyDTO;
import ca.ma99us.jab.headers.*;
import ca.ma99us.jab.headers.groups.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;


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

        // set the key
        CryptoHeader.getKeys().encryptKey("SomeSuperSecretKey", "SomeSalt");

        barcodeTest(header, CryptoHeaderGroup.class, dto, DummyDTO.class, true);

        // compressed version
        System.out.println("Compressed\n");
        CompressCryptoHeaderGroup<DummyDTO> header1 = new CompressCryptoHeaderGroup<DummyDTO>();

        barcodeTest(header1, CompressCryptoHeaderGroup.class, dto, DummyDTO.class, true);
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

        barcodeTest(header, CborHeaderGroup.class, dto, DummyDTO.class, false);

        // compressed version
        System.out.println("Compressed\n");
        CompressCborHeaderGroup<DummyDTO> header1 = new CompressCborHeaderGroup<DummyDTO>();
        barcodeTest(header1, CompressCborHeaderGroup.class, dto, DummyDTO.class, false);
    }

    @Test
    public void bsonHeaderTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);
        BsonHeaderGroup<DummyDTO> header = new BsonHeaderGroup<DummyDTO>();

        barcodeTest(header, BsonHeaderGroup.class, dto, DummyDTO.class, false);

        // compressed version
        System.out.println("Compressed\n");
        CompressBsonHeaderGroup<DummyDTO> header1 = new CompressBsonHeaderGroup<DummyDTO>();
        barcodeTest(header1, CompressBsonHeaderGroup.class, dto, DummyDTO.class, false);
    }

    @Test
    public void messagePackHeaderTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);
        MessagePackHeaderGroup<DummyDTO> header = new MessagePackHeaderGroup<DummyDTO>();

        barcodeTest(header, MessagePackHeaderGroup.class, dto, DummyDTO.class, false);
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
