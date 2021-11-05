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
     CryptoHeaderGroup<DummyDTO> header = new CryptoHeaderGroup<DummyDTO>();
     CryptoHeaderGroup.getCryptoHeader().setCrypto(new JabCrypto().setSecretKey("SomeSuperSecretKey", "SomeSalt"));
             
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
         res = jabParser.jabToObject(barcode, CryptoHeaderGroup.class, DummyDTO.class);
         System.out.println(res);
     } catch (IOException e) {
         e.printStackTrace();
     }
     
## Cyphers, Signers, Hashers, Serializers, and Converters

Symmetric (Blowfish by default) and asymmetric (RSA by default) cyphers are supported. Custom cyphers can be registered by user 
by extending *JabCrypto* or *JabAsyncCrypto* classes.

Symmetric (HMAC by default) and asymmetric (ECDSA by default) signers are supported. Custom cyphers can be registered by user
by extending *JabSigner* or *JabAsyncSigner* classes.

Hashing is done *JabHasher* class which uses platform-independent process based on SHA256 algorithm to get long unsigned integer 
number representing byte arrays, strings, and POJOs. Custom hasher can be registered by user by extending *JabHasher* class.

*JabToString* class is responsible for converting bytes to printable characters and back (Base64 by default), 
and custom serialization mechanism can be registered by extending that class.

Additionally third-party data converters can be added. For example CBOR (https://cbor.io/) and BSON (https://bsonspec.org/) converter headers are included in the library. 
See unit-tests for usage examples.

## Maven dependency

    <dependency>
        <groupId>ca.ma99us</groupId>
        <artifactId>jab-code</artifactId>
        <version>1.2</version>
    </dependency>
    
### Third party dependencies (optional)

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-core</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
        </dependency>
        <dependency>
            <groupId>com.google.iot.cbor</groupId>
            <artifactId>cbor</artifactId>
        </dependency>
        <dependency>
            <groupId>de.undercouch</groupId>
            <artifactId>bson4jackson</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mongodb</groupId>
            <artifactId>mongodb-driver-sync</artifactId>
        </dependency>
        <dependency>
            <groupId>org.msgpack</groupId>
            <artifactId>msgpack</artifactId>
        </dependency>
        <dependency>
            <groupId>org.msgpack</groupId>
            <artifactId>jackson-dataformat-msgpack</artifactId>
        </dependency>
        <dependency>
            <groupId>org.bouncycastle</groupId>
            <artifactId>bcprov-jdk15on</artifactId>
        </dependency>