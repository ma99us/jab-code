package org.maggus.jab;

import org.junit.Assert;
import org.junit.Test;
import org.maggus.jab.headers.CryptoChecksumHeader;
import org.maggus.jab.headers.ChecksumHeader;
import org.maggus.jab.headers.JabHeader;
import org.maggus.jab.dummy.DummyDTO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class JabParserTest {

    @Test
    public void isPossibleJabBarcodeTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(false, false);

        JabParser jabParser = new JabParser();
        Assert.assertEquals(false, jabParser.isPossibleJab(null));
        Assert.assertEquals(false, jabParser.isPossibleJab("akdjhfo237fh8fgo8dgfvb587gtb"));

        // generate barcode
        String barcode = null;
        try {
            barcode = jabParser.objectToJab(null, dto);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertEquals(true, jabParser.isPossibleJab(barcode));
    }

    @Test
    public void findJabBarcodeFormatIdTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(false, false);
        ChecksumHeader header = new ChecksumHeader();

        JabParser jabParser = new JabParser();
        Assert.assertNull(jabParser.findJabFormatId(null));
        Assert.assertNull(jabParser.findJabFormatId("akdjhfo237fh8fgo8dgfvb587gtb"));

        Long simpleBarcodeFormatId = new JabParser.Formats.JabFormat((Class<?>) null, dto.getClass()).getFormatId();
        Long checksumBarcodeFormatId = new JabParser.Formats.JabFormat(header.getClass(), dto.getClass()).getFormatId();

        // generate barcode
        String barcode = null, csBarcode = null;
        try {
            barcode = jabParser.objectToJab(null, dto);
            csBarcode = jabParser.objectToJab(header, dto);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertEquals(simpleBarcodeFormatId, jabParser.findJabFormatId(barcode));
        Assert.assertEquals(checksumBarcodeFormatId, jabParser.findJabFormatId(csBarcode));
    }

    @Test
    public void registeredBarcodeFormatTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(false, false);

        JabParser jabParser = new JabParser();

        // generate barcodes
        String sBarcode = null, csBarcode = null;
        try {
            sBarcode = jabParser.objectToJab(null, dto);
            csBarcode = jabParser.objectToJab(new ChecksumHeader(), dto);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        // register barcode formats
        jabParser.getFormats().registerFormat(ChecksumHeader.class, DummyDTO.class);

        // parse it back
        try {
            jabParser.jabToObject(sBarcode);   // should throw an exception
            Assert.fail();
        } catch (IOException e) {
            System.out.println("Expected exception: " + e.getMessage());
        }

        DummyDTO res = null;
        try {
            res = (DummyDTO) jabParser.jabToObject(csBarcode);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertEquals(dto, res);
    }

    @Test
    public void basicBarcodeTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(false, false);

//        ObjectMapper objectMapper = new ObjectMapper();
//        Map<String, Object> objData = objectMapper.convertValue(dto, LinkedHashMap.class);

        JabParser jabParser = new JabParser();

        // generate barcode
        String barcode = null;
        try {
            barcode = jabParser.objectToJab(null, dto);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        System.out.println(barcode);
        Assert.assertNotNull(barcode);
        Assert.assertTrue(barcode.startsWith(JabParser.PREFIX + JabParser.DELIMITER));

        // parse it back
        DummyDTO res = null;
        try {
            res = jabParser.jabToObject(barcode, null, DummyDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        System.out.println(res);
        Assert.assertNotNull(res);
        Assert.assertEquals(dto, res);
    }

    @Test
    public void checksumHeaderBarcodeTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(false, false);
        ChecksumHeader header = new ChecksumHeader();

        barcodeTest(header, ChecksumHeader.class, dto, DummyDTO.class);
    }

    @Test
    public void checksumHeaderCompositionDtoBarcodeTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, false);
        ChecksumHeader header = new ChecksumHeader();

        barcodeTest(header, ChecksumHeader.class, dto, DummyDTO.class);
    }

    @Test
    public void checksumHeaderCollectionsDtoBarcodeTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(false, true);
        ChecksumHeader header = new ChecksumHeader();

        barcodeTest(header, ChecksumHeader.class, dto, DummyDTO.class);
    }

    @Test
    public void checksumHeaderCollectionsCompositionDtoBarcodeTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);
        ChecksumHeader header = new ChecksumHeader();

        barcodeTest(header, ChecksumHeader.class, dto, DummyDTO.class);
    }

    @Test
    public void encryptedChecksumHeaderCollectionsCompositionDtoBarcodeTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);
        CryptoChecksumHeader header = new CryptoChecksumHeader();
        CryptoChecksumHeader.getKeys().encryptKey("SomeSuperSecretKey", "SomeSalt");

        barcodeTest(header, CryptoChecksumHeader.class, dto, DummyDTO.class);
    }

    @Test
    public void wrapBytesTest() {
        // input shorter then the output
        String sStr = "short string", sStr1 = "short str1ng";

        byte[] rBytes = new JabParser.Hasher().wrapBytes(sStr.getBytes(StandardCharsets.UTF_8), 256);
        byte[] rBytes1 = new JabParser.Hasher().wrapBytes(sStr1.getBytes(StandardCharsets.UTF_8), 256);

        Assert.assertNotNull(rBytes);
        Assert.assertEquals(256, rBytes.length);
        Assert.assertNotEquals(rBytes, rBytes1);

        // input longer then the  output
        sStr = "some very long string to get bytes from for wrapping into shorter buffer";
        sStr1 = "some very 1ong string to get bytes from for wrapping into shorter buffer";

        rBytes = new JabParser.Hasher().wrapBytes(sStr.getBytes(StandardCharsets.UTF_8), 8);
        rBytes1 = new JabParser.Hasher().wrapBytes(sStr1.getBytes(StandardCharsets.UTF_8), 8);

        Assert.assertNotNull(rBytes);
        Assert.assertEquals(8, rBytes.length);
        Assert.assertNotEquals(rBytes, rBytes1);
    }

    private <H extends JabHeader, P> P barcodeTest(H header, Class<H> hClass, P dto, Class<P> pClass) {
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

        // alter the barcode!
        int length = barcode.length();
        int p0 = barcode.lastIndexOf("[");
        Assert.assertTrue(p0 > 0);
        int i = p0 + (int) (Math.random() * (length - p0));
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

        return res;
    }
}
