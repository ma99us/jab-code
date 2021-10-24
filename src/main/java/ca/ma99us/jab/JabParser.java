package ca.ma99us.jab;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import ca.ma99us.jab.headers.JabHeader;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * My custom barcode format called JAB (Json Array Barcode) looks like this:
 * JAB|12345678|["abc", 123, ...]["string field", 567,]
 * which are:
 * JAB|barcode id|[coma-separated header fields values][coma-separated payload fields values].
 * The data in square brackets is a json array of the bean fields values (no fields names).
 * The barcode id is a combined checksum of Header and Payload classes names.
 */
public class JabParser {
    public static final String PREFIX = "JAB";
    public static final String DELIMITER = "|";
    private static final Hasher hasher = new Hasher();
    @Getter
    private final Formats formats = new Formats();

    private final Pattern pattern = Pattern.compile("^JAB\\|\\d+\\|(?:\\[.*\\])?\\[.*\\]$", Pattern.MULTILINE);

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            ;

    /**
     * Quick check that barcode looks like a correct format
     *
     * @param barcode string to check
     * @return possible JabBarcode, and parsing could be attempted
     */
    public boolean isPossibleJab(String barcode) {
        // no regex
//        if (barcode != null
//                && barcode.startsWith(PREFIX + DELIMITER)
//                && barcode.indexOf(DELIMITER, (PREFIX + DELIMITER).length() + 1) > 0
//                && barcode.indexOf("[") > 0
//                && barcode.endsWith("]")) {
//            return true;
//        }
//        return false;

        // regex
        if (barcode == null) {
            return false;
        }
        final Matcher matcher = pattern.matcher(barcode);
        return matcher.find();
    }

    public Long findJabFormatId(String barcode) {
        if (!isPossibleJab(barcode)) {
            return null; // bad format
        }
        try {
            //TODO: do this in regex maybe?
            barcode = barcode.substring((PREFIX + DELIMITER).length());
            int p0 = barcode.indexOf(DELIMITER);
            if (p0 <= 0) {
                return null;  // no format id
            }
            return Long.parseLong(barcode.substring(0, p0));
        } catch (Exception ex) {
            // bad format id value
            return null;
        }
    }

    public <H extends JabHeader, P> String objectToJab(H header, P payload) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append(PREFIX);
        sb.append(DELIMITER);
        sb.append(new Formats.JabFormat<H, P>(header, payload).getFormatId());
        sb.append(DELIMITER);
        String payloadStr = objectValuesToJsonArrayString(payload);
        if (header != null) {
            // populate the header
            header.populate(payload, payloadStr);
            // encrypt the payload
            payloadStr = header.obfuscate(payload, payloadStr);
            sb.append(objectValuesToJsonArrayString(header));
        }
        sb.append(payloadStr);

        return sb.toString();
    }

    public Object jabToObject(String barcode) throws IOException {
        Long formatId = findJabFormatId(barcode);
        if (formatId == null) {
            throw new IOException("Unrecognized format");
        }
        Formats.JabFormat<?, ?> format = formats.findFormat(formatId);
        if (format == null) {
            throw new IOException("Unregistered format id: " + formatId);
        }
        return jabToObject(barcode, format.getHeaderClass(), format.getPayloadClass());
    }

    public <H extends JabHeader, P> P jabToObject(String barcode, Class<H> headerClass, Class<P> payloadClass) throws IOException {
        if (barcode == null) {
            return null;
        }
        //TODO: do this in regex maybe?
        if (!barcode.startsWith(PREFIX + DELIMITER)) {
            throw new IOException("Unrecognized prefix");
        }
        barcode = barcode.substring((PREFIX + DELIMITER).length());
        int p0 = barcode.indexOf(DELIMITER);
        if (p0 <= 0) {
            throw new IOException("Unrecognized format; no checksum");
        }
        long barcodeFormatId = Long.parseLong(barcode.substring(0, p0));
        long formatId = new Formats.JabFormat<H, P>(headerClass, payloadClass).getFormatId();
        if (barcodeFormatId != formatId) {
            throw new IOException("Format id mismatch; expected " + formatId + ", but got " + barcodeFormatId);
        }
        barcode = barcode.substring(p0 + 1);
        H header = null;
        if (headerClass != null) {
            int p1 = barcode.indexOf("]["); // FIXME: not very reliable
            if (p1 <= 0) {
                throw new IOException("Bad format; can not parse header");
            }
            header = jsonArrayStringToObject(barcode.substring(0, p1 + 1), headerClass);
            barcode = barcode.substring(p1 + 1);
            // decrypt barcode payload
            barcode = header.deobfuscate(null, barcode);
        }
        P payload = jsonArrayStringToObject(barcode, payloadClass);
        if (header != null) {
            // validate checksum
            header.validate(payload, barcode);
        }
        return payload;
    }

    private String objectValuesToJsonArrayString(Object obj) throws IOException {
        List<Object> beanDataValues = getObjectValues(obj);
        return mapper.writeValueAsString(beanDataValues);
    }

    private <T> T jsonArrayStringToObject(String json, Class<T> clazz) throws IOException {
        List<Object> beanDataValues = mapper.readValue(json, ArrayList.class);
        return parseObjectValues(beanDataValues, clazz);
    }

    private <T> T parseObjectValues(List<Object> beanDataValues, Class<T> clazz) throws IOException {
        Map<String, Object> beanData = parseMapValues(beanDataValues, clazz);
        return mapper.convertValue(beanData, clazz);
    }

    private <T> Map<String, Object> parseMapValues(List<Object> beanDataValues, Class<T> clazz) throws IOException {
        int fieldIdx = 0;
        LinkedHashMap<String, Object> objectData = new LinkedHashMap<String, Object>();
        List<Field> fieldNames = getObjectFieldNames(clazz); // actual bean fields in proper order
        for (Object value : beanDataValues) {
            if (fieldIdx < fieldNames.size()) {
                Field field = fieldNames.get(fieldIdx);
                Class<?> fieldType = getFieldType(field);
                if (value instanceof List && !isJavaLangClass(fieldType)) {
                    // not a java.lang field, probably nested java bean, parse with Value Mapper recursively
                    if (field.getType().isArray() || List.class.isAssignableFrom(field.getType())) {
                        // list or array of non-java.lang types
                        List<Object> arrValue = new ArrayList<Object>();
                        for (Object elem : (List) value) {
                            arrValue.add(parseObjectValues((List) elem, fieldType));
                        }
                        value = arrValue;
                    } else {
                        // single composition object
                        value = parseObjectValues((List) value, fieldType);
                    }
                }

                objectData.put(field.getName(), value);
                fieldIdx++;
            }
        }
        return objectData;
    }

    private List<Object> getObjectValues(Object obj) throws IOException {
        Map<String, Object> objData = mapper.convertValue(obj, LinkedHashMap.class);
        return getMapValues(objData, obj);
    }

    private List<Object> getMapValues(Map<String, Object> objData, Object obj) throws IOException {
        List<Object> dataValues = new ArrayList<>();
        List<Field> fieldNames = getObjectFieldNames(obj.getClass());
        for (Field field : fieldNames) {
            Object value = objData.get(field.getName());
            Class<?> fieldType = getFieldType(field);
            if (value instanceof Map && !isJavaLangClass(fieldType)) {
                // not a java.lang property, probably nested java bean, serialize with Value Mapper recursively
                Object objectValue = getObjectFieldValueByName(obj, field.getName());
                value = getObjectValues(objectValue);
            } else if (value instanceof List && !isJavaLangClass(fieldType)) {
                // array of not a java.lang objects, probably array of java beans, serialize each element with Value Mapper recursively
                Object objectValue = getObjectFieldValueByName(obj, field.getName());
                List<Object> arrValue = new ArrayList<Object>();
                if (objectValue.getClass().isArray()) {
                    int length = Array.getLength(objectValue);
                    for (int i = 0; i < length; i++) {
                        Object elem = Array.get(objectValue, i);
                        arrValue.add(getObjectValues(elem));
                    }
                } else if (objectValue instanceof List) {
                    for (Object elem : (List) objectValue) {
                        arrValue.add(getObjectValues(elem));
                    }
                }
                value = arrValue;
            }
            dataValues.add(value);
        }
        return dataValues;
    }

    private List<Field> getObjectFieldNames(Class objClass) {
        List<Field> declaredFields = new ArrayList<>();
        for (Class<?> c = objClass; c != null; c = c.getSuperclass()) {
            if (c.equals(Object.class)) {
                continue;
            }
            List<Field> allFields = new ArrayList<>(Arrays.asList(c.getDeclaredFields()));
            //filter out static fields
            List<Field> fields = new ArrayList<>();
            for (Field f : allFields) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    fields.add(f);
                }
            }
            // Unfortunately, some JVMs do not guarantee DeclaredFields order, so we have to sort fields ourselfs
            Collections.sort(fields, new Comparator<Field>() {
                public int compare(Field f1, Field f2) {
                    // sort each class fields names alphabetically
                    return f1.getName().compareTo(f2.getName());
                }
            });

            // parent class fields always go before child's fields
            declaredFields.addAll(0, fields);
        }
        return declaredFields;
    }

    private boolean isJavaLangClass(Class<?> clazz) {
        return clazz != null && (clazz.isPrimitive() || clazz.getName().startsWith("java.")); // TODO: maybe java.lang ?
    }

    private Object getObjectFieldValueByName(Object obj, String fieldName) throws IOException {
        try {
//            return PropertyUtils.getProperty(obj, fieldName); // Unfortunately, commons-beanutils do not work on Android!

            // TODO: is there a better cross-platform way to getting a field value from object?
            try {
                // try to find a 'getter' for this field first
                Method method = obj.getClass().getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
                return method.invoke(obj);
            } catch (NoSuchMethodException ex) {
                // if no 'getter', then try to access field directly. This will fail if it is not public field.
                Class<?> clazz = obj.getClass();
                Field field = clazz.getField(fieldName);
                return field.get(obj);
            }
        } catch (Exception e) {
            throw new IOException("Can't access object's \"" + obj.getClass().getSimpleName() + "\" field \"" + fieldName + "\" value", e);
        }
    }

    private Class<?> getFieldType(Field field) {
        Class<?> type = field.getType();
        if (type.isArray()) {
            return type.getComponentType();
        } else if (List.class.isAssignableFrom(field.getType())) {
            ParameterizedType pt = (ParameterizedType) field.getGenericType();
            return (Class) pt.getActualTypeArguments()[0];
        }
        return type;
    }

    /**
     * Simple platform-independent way to create hashes from strings.
     */
    public static class Hasher {
        private final String ALGORITHM = "SHA-256"; // default
        private final long MAX_SAFE_INTEGER = 9007199254740991L;      // 2^53 - 1 is the maximum "safe" integer for json/javascript

        public long hashString(String data) {
            try {
                MessageDigest md = MessageDigest.getInstance(ALGORITHM);
                byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));

                // shrink it to 8 bytes
                digest = wrapBytes(digest, 8);

                // build a long integer value
                long msb = 0;
                for (int i = 0; i < digest.length; i++) {
                    msb = (msb << 8) | (digest[i] & 0xff);
                }

                // make it Unsigned and not larger then MAX_SAFE_INTEGER
                return Math.abs(msb) % MAX_SAFE_INTEGER;
            } catch (Exception e) {
                throw new IllegalArgumentException("Hash Error", e);
            }
        }

        public byte[] wrapBytes(byte[] sBytes, int rLen) {
            int sLen = sBytes.length;
            byte[] rBytes = new byte[rLen];
            for (int step = 0, s = 0, r = 0; step < Math.max(rLen, sLen); step++, s++, r++) {
                s %= sLen;
                r %= rLen;
                rBytes[r] ^= sBytes[s];
            }
            return rBytes;
        }
    }

    /**
     * Simple platform-independent way to encrypt/decrypt strings.
     */
    public static class Crypto {
        private final String algorithm;
        private final String mode;
        private final int keyLen;
        private final int ivLen;
        private final Hasher hasher = new Hasher();

        /**
         * Default Blowfish algorithm.
         */
        public Crypto() {
            this("Blowfish", "Blowfish/CBC/PKCS5Padding", 16, 8);   // default
        }

        /**
         * @param algorithm algorithm name
         * @param mode algorithm mode
         * @param keyLen key length in bytes
         * @param ivLen iV length in bytes
         */
        public Crypto(String algorithm, String mode, int keyLen, int ivLen) {
            this.algorithm = algorithm;
            this.mode = mode;
            this.keyLen = keyLen;
            this.ivLen = ivLen;
        }

        public String encryptString(String value, String key) {
            try {
                SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes(key), algorithm);
                Cipher cipher = Cipher.getInstance(mode);
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(ivBytes(key)));
                byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
                return bytesToString(encrypted);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Encryption Error", ex);
            }
        }

        public String decryptString(String value, String key) {
            try {
                byte[] encrypted = stringToBytes(value);
                SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes(key), algorithm);
                Cipher cipher = Cipher.getInstance(mode);
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(ivBytes(key)));
                return new String(cipher.doFinal(encrypted));
            } catch (Exception ex) {
                throw new IllegalArgumentException("Encryption Error", ex);
            }
        }

        protected byte[] keyBytes(String seed){
            return hasher.wrapBytes(seed.getBytes(StandardCharsets.UTF_8), keyLen);
        }

        protected byte[] ivBytes(String seed){
            return hasher.wrapBytes(seed.getBytes(StandardCharsets.UTF_8), ivLen);
        }

        public String bytesToString(byte[] bytes){
            // use URL_SAFE, NO_WRAP standards
            return Base64.getEncoder().encodeToString(bytes);   //TODO: this might not work on Android or older Java
        }

        public byte[] stringToBytes(String string) {
            // use URL_SAFE, NO_WRAP standards
            return Base64.getDecoder().decode(string);        //TODO: this might not work on Android or older Java
        }
    }

    /**
     * Simple collection of registered barcode formats.
     * Finds java bean classes from the barcode format id.
     */
    public static class Formats {
        private final Map<Long, JabFormat<?, ?>> formats = new HashMap<>();

        public synchronized <H extends JabHeader, P> Formats registerFormat(Class<H> headerClass, Class<P> payloadClass) {
            JabFormat<H, P> format = new JabFormat<H, P>(headerClass, payloadClass);
            formats.put(format.getFormatId(), format);
            return this;
        }

        public synchronized JabFormat<?, ?> findFormat(Long id) {
            return id != null ? formats.get(id) : null;
        }

        @Data
        @AllArgsConstructor
        public static class JabFormat<H extends JabHeader, P> {
            private final Class<H> headerClass;
            private final Class<P> payloadClass;

            public JabFormat(H header, P payload){
                headerClass = header != null ? (Class<H>) header.getClass() : null;
                payloadClass = (Class<P>) payload.getClass();
            }

            public long getFormatId() {
                StringBuilder sb = new StringBuilder();
                if (headerClass != null) {
                    sb.append(headerClass.getSimpleName());
                }
                sb.append(payloadClass.getSimpleName());
                return hasher.hashString(sb.toString());
            }
        }
    }
}
