package ca.ma99us.jab;

import ca.ma99us.jab.dummy.DummyDTO;
import ca.ma99us.jab.headers.ChecksumHeader;
import ca.ma99us.jab.headers.CompressCryptoHeaderGroup;
import ca.ma99us.jab.headers.JabHeader;
import org.junit.Assert;
import org.junit.Test;

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
        ChecksumHeader<DummyDTO> header = new ChecksumHeader<DummyDTO>();

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
            csBarcode = jabParser.objectToJab(new ChecksumHeader<DummyDTO>(), dto);
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

        barcodeTest(null, null, dto, DummyDTO.class);
    }

    @Test
    public void compositionDtoBarcodeTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, false);

        barcodeTest(null, null, dto, DummyDTO.class);
    }

    @Test
    public void collectionsDtoBarcodeTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(false, true);

        barcodeTest(null, null, dto, DummyDTO.class);
    }

    @Test
    public void collectionsCompositionDtoBarcodeTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);

        barcodeTest(null, null, dto, DummyDTO.class);
    }

    @Test
    public void collectionsCompositionDtoSchemaTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);

        JabParser jabParser = new JabParser();

        // generate barcode
        String barcode = null;
        try {
            barcode = jabParser.objectToJabSchema(null, dto);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertNotNull(barcode);
        System.out.println("schema: \"" + barcode + "\"");
        System.out.println(dto);
    }

    @Test
    public void headersGroupSchemaTest() {
        DummyDTO dto = DummyDTO.makeDummyDTO(true, true);
        CompressCryptoHeaderGroup<DummyDTO> header = new CompressCryptoHeaderGroup<DummyDTO>();

        JabParser jabParser = new JabParser();

        // generate barcode
        String barcode = null;
        try {
            barcode = jabParser.objectToJabSchema(header, dto);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertNotNull(barcode);
        System.out.println("schema: \"" + barcode + "\"");
        System.out.println(dto);
    }

    @Test
    public void wrapBytesTest() {
        // input shorter then the output
        String sStr = "short string", sStr1 = "short str1ng";

        byte[] rBytes = new JabHasher().wrapBytes(sStr.getBytes(StandardCharsets.UTF_8), 256);
        byte[] rBytes1 = new JabHasher().wrapBytes(sStr1.getBytes(StandardCharsets.UTF_8), 256);

        Assert.assertNotNull(rBytes);
        Assert.assertEquals(256, rBytes.length);
        Assert.assertNotEquals(rBytes, rBytes1);

        // input longer then the  output
        sStr = "some very long string to get bytes from for wrapping into shorter buffer";
        sStr1 = "some very 1ong string to get bytes from for wrapping into shorter buffer";

        rBytes = new JabHasher().wrapBytes(sStr.getBytes(StandardCharsets.UTF_8), 8);
        rBytes1 = new JabHasher().wrapBytes(sStr1.getBytes(StandardCharsets.UTF_8), 8);

        Assert.assertNotNull(rBytes);
        Assert.assertEquals(8, rBytes.length);
        Assert.assertNotEquals(rBytes, rBytes1);
    }

    private <H extends JabHeader<P>, P> P barcodeTest(H header, Class<H> hClass, P dto, Class<P> pClass) {
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

        return res;
    }
}
