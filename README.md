# jab-code

A library to generate secure and compact barcodes data from POJO/java beans.

## Format
    JAB|format id|[header fields values][payload data fields values]
Java Beans fields values are stored as JSON array, 'null's are trimmed for brevity.
No fields names or ids are stored, making the resulting string pretty compact.
Reflection is used to populate proper fields from values during parsing. Fields order is alphabetic.

Various headers are added for additional validation and processing of the payload data. Like checksumming or encryption. 

An example where Header has only one field - the checksum of the payload data:
    
    JAB|6654389665621769|[3170455240234689][123456,,,,,,,,"Some Name",,,]

## Getting Started
### Checksum header example
     DummyDTO dto = new DummyDTO();
     ChecksumHeader<DummyDTO> header = new ChecksumHeader<DummyDTO>();
     
     // generate barcode
     JabParser jabParser = new JabParser();
     String barcode = null;
     try {
         barcode = jabParser.objectToJab(header, dto);
         System.out.println("barcode: (" + barcode.length() + " bytes): \"" + barcode + "\"");
     } catch (IOException e) {
         e.printStackTrace();
     }
     
     // parse barcode
     DummyDTO res = null;
     try {
         res = jabParser.jabToObject(barcode, ChecksumHeader.class, DummyDTO.class);
         System.out.println(res);
     } catch (IOException e) {
         e.printStackTrace();
     }

### Encryption Header example
     DummyDTO dto = new DummyDTO();
     CryptoChecksumHeader<DummyDTO> header = new CryptoChecksumHeader<DummyDTO>();
     CryptoChecksumHeader.getKeys().encryptKey("SomeSuperSecretKey", "SomeSalt");
             
     // generate barcode
     JabParser jabParser = new JabParser();
     String barcode = null;
     try {
         barcode = jabParser.objectToJab(header, dto);
         System.out.println("barcode: (" + barcode.length() + " bytes): \"" + barcode + "\"");
     } catch (IOException e) {
         e.printStackTrace();
     }
     
     // parse barcode
     DummyDTO res = null;
     try {
         res = jabParser.jabToObject(barcode, ChecksumHeader.class, DummyDTO.class);
         System.out.println(res);
     } catch (IOException e) {
         e.printStackTrace();
     }

## Maven dependency

    <dependency>
        <groupId>ca.ma99us</groupId>
        <artifactId>jab-code</artifactId>
        <version>1.0</version>
    </dependency>
