package ca.ma99us.jab;


import ca.ma99us.jab.headers.JabHeader;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
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
    @Getter
    private final Formats formats = new Formats();

    private final Pattern pattern = Pattern.compile("^JAB\\|\\d+\\|(?:\\[.*\\])?\\[.*\\]$", Pattern.MULTILINE);

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
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

    /**
     * Extracts format id from the Jab string
     * @param barcode Jab string
     * @return long format id or null
     */
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

    /**
     * Serializes POJO into a Jab string
     * @param header JabHeader(s) to apply to payload string portion
     * @param payload POJO java bean
     * @param <H> generic header class
     * @param <P> generic payload class
     * @return Jab string
     * @throws IOException in case of any failure
     */
    public <H extends JabHeader<P>, P> String objectToJab(H header, P payload) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append(PREFIX);
        sb.append(DELIMITER);
        sb.append(new Formats.JabFormat<H, P>(header, payload).getFormatId());
        sb.append(DELIMITER);
        String payloadStr = unwrap(objectValuesToJsonArrayString(payload));
        if (header != null) {
            // populate the header
            header.populate(payload);
            // encrypt the payload
            payloadStr = new String(header.obfuscate(payloadStr.getBytes(StandardCharsets.UTF_8)));
            sb.append(objectValuesToJsonArrayString(header));
        }
        sb.append(wrap(payloadStr) );

        return sb.toString();
    }

    /**
     * Serializes POJO into a byte array
     * @param header JabHeader(s) to apply to payload string portion
     * @param payload POJO java bean
     * @param <H> generic header class
     * @param <P> generic payload class
     * @return Jab byte array (non human readable bytes)
     * @throws IOException in case of any failure
     */
    public <H extends JabHeader<P>, P> byte[] objectToJabBytes(H header, P payload) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append(PREFIX);
        sb.append(DELIMITER);
        sb.append(new Formats.JabFormat<H, P>(header, payload).getFormatId());
        sb.append(DELIMITER);
        byte[] payloadBytes = unwrap(objectValuesToJsonArrayString(payload)).getBytes(StandardCharsets.UTF_8);
        if (header != null) {
            // populate the header
            header.populate(payload);
            // encrypt the payload
            payloadBytes = header.obfuscate(payloadBytes);
            sb.append(objectValuesToJsonArrayString(header));
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        os.write("[".getBytes(StandardCharsets.UTF_8));
        os.write(payloadBytes);
        os.write("]".getBytes(StandardCharsets.UTF_8));

        return os.toByteArray();
    }

    /**
     * Generates schema of the Jab serialization
     * @param header JabHeader(s) to apply to payload string portion
     * @param payload POJO java bean
     * @param <H> generic header class
     * @param <P> generic payload class
     * @return Jab string
     * @throws IOException in case of any failure
     */
    public <H extends JabHeader<P>, P> String objectToJabSchema(H header, P payload) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append(PREFIX);
        sb.append(DELIMITER);
        sb.append("format id");
        sb.append(DELIMITER);
        String payloadStr = objectFieldNamesToJsonArrayString(payload);
        if (header != null) {
            sb.append(objectFieldNamesToJsonArrayString(header));
        }
        sb.append(payloadStr);

        return sb.toString();
    }

    /**
     * Parses Jab string into corresponding POJO java bean. Tries automatically find registered format to decode.
     * @see Formats#registerFormat(java.lang.Class, java.lang.Class)
     * @param barcode Jab string
     * @return POJO java bean
     * @throws IOException in case of any failure
     */
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

    /**
     * Parses Jab string into corresponding POJO java bean.
     * @param barcode Jab string
     * @param headerClass JabHeader(s) class to apply to payload string portion
     * @param payloadClass POJO java bean class
     * @param <H> generic header class
     * @param <P> generic payload class
     * @return Jab string
     * @throws IOException in case of any failure
     */
    public <H extends JabHeader<P>, P> P jabToObject(String barcode, Class<H> headerClass, Class<P> payloadClass) throws IOException {
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
            barcode = unwrap(barcode);
            barcode = new String(header.deobfuscate(barcode.getBytes(StandardCharsets.UTF_8)));
            barcode = wrap(barcode);
        }
        P payload = jsonArrayStringToObject(barcode, payloadClass);
        if (header != null) {
            // validate checksum
            header.validate(payload);
        }
        return payload;
    }

    private String objectFieldNamesToJsonArrayString(Object obj) throws IOException {
        List<Object> beanDataValues = getObjectFieldNames(obj);
        return mapper.writeValueAsString(beanDataValues);
    }

    /**
     * Serialize POJO java bean into a JSON array string of only object fields values. Jab object serialization.
     * @param obj POJO java bean
     * @return Jab object string
     * @throws IOException in case of any failure
     */
    public String objectValuesToJsonArrayString(Object obj) throws IOException {
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

    private List<Object> getMapKeys(Map<String, Object> objData, Object obj) throws IOException {
        List<Object> dataValues = new ArrayList<>();
        List<Field> fieldNames = getObjectFieldNames(obj.getClass());
        for (Field field : fieldNames) {
            String name = field.getName();
            Object value = objData.get(name);
            Class<?> fieldType = getFieldType(field);
            if (value instanceof Map && !isJavaLangClass(fieldType)) {
                // not a java.lang property, probably nested java bean, serialize with Value Mapper recursively
                Object objectValue = getObjectFieldValueByName(obj, name);
                value = name + ":" + getObjectFieldNames(objectValue);
            } else if (value instanceof List && !isJavaLangClass(fieldType)) {
                // array of not a java.lang objects, probably array of java beans, serialize each element with Value Mapper recursively
                Object objectValue = getObjectFieldValueByName(obj, name);
                List<Object> arrValue = new ArrayList<Object>();
                if (objectValue.getClass().isArray()) {
                    int length = Array.getLength(objectValue);
                    for (int i = 0; i < length; i++) {
                        Object elem = Array.get(objectValue, i);
                        arrValue.add(getObjectFieldNames(elem));
                    }
                } else if (objectValue instanceof List) {
                    for (Object elem : (List) objectValue) {
                        arrValue.add(getObjectFieldNames(elem));
                    }
                }
                value = name + ":" + arrValue;
            } else {
                value = name;
            }
            dataValues.add(value);
        }
        return dataValues;
    }

    private List<Object> getObjectFieldNames(Object obj) throws IOException {
        Map<String, Object> objData = mapper.convertValue(obj, LinkedHashMap.class);
        return getMapKeys(objData, obj);
    }

    private List<Field> getObjectFieldNames(Class objClass) {
        List<Field> declaredFields = new ArrayList<>();
        for (Class<?> c = objClass; c != null; c = c.getSuperclass()) {
            if (c.equals(Object.class)) {
                continue;   // don't gig any further
            }
            List<Field> allFields = new ArrayList<>(Arrays.asList(c.getDeclaredFields()));
            // filter out static fields and fields with @JsonIgnore annotation
            List<Field> fields = new ArrayList<>();
            for (Field f : allFields) {
                if (!Modifier.isStatic(f.getModifiers()) && f.getAnnotation(JsonIgnore.class) == null) {
                    fields.add(f);
                }
            }
            // Unfortunately, some JVMs do not guarantee DeclaredFields order, so we have to sort fields ourselves
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
     * wrap in "[", "]"
     * @param str payload to wrap
     * @return wrapped string
     */
    public static String wrap(String str) {
        return "[" + str + "]";
    }

    /**
     * trim off "[", "]", if previously wrapped;
     * @param str payload to unwrap
     * @return unwrapped string
     */
    public static String unwrap(String str) {
        if (str.startsWith("[") && str.endsWith("]")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    /**
     * Simple collection of registered barcode formats.
     * Finds java bean classes from the barcode format id.
     */
    public static class Formats {
        private final Map<Long, JabFormat<?, ?>> formats = new HashMap<>();

        /**
         * Register a Jab Format - a pair of Header and Payload classes
         * @param headerClass header class
         * @param payloadClass POJO java bean class (payload)
         * @param <H> generic header class
         * @param <P> generic payload class
         * @return this
         */
        public synchronized <H extends JabHeader, P> Formats registerFormat(Class<H> headerClass, Class<P> payloadClass) {
            JabFormat<H, P> format = new JabFormat<H, P>(headerClass, payloadClass);
            formats.put(format.getFormatId(), format);
            return this;
        }

        /**
         * Finds registered format for a given id
         * @param id long number format id
         * @return registered Jab format or null
         */
        public synchronized JabFormat<?, ?> findFormat(Long id) {
            return id != null ? formats.get(id) : null;
        }

        /**
         * Jab code format - a pair of Header and Payload classes.
         * @param <H> generic header class
         * @param <P> generic payload class
         */
        @Data
        @AllArgsConstructor
        public static class JabFormat<H extends JabHeader, P> {
            private final Class<H> headerClass;
            private final Class<P> payloadClass;

            public JabFormat(H header, P payload){
                headerClass = header != null ? (Class<H>) header.getClass() : null;
                payloadClass = (Class<P>) payload.getClass();
            }

            /**
             * Calculate format id.
             * @return long number format id
             */
            public long getFormatId() {
                StringBuilder sb = new StringBuilder();
                if (headerClass != null) {
                    sb.append(headerClass.getSimpleName());
                }
                sb.append(payloadClass.getSimpleName());
                return JabHasher.getGlobalHasher().hashString(sb.toString());
            }
        }
    }
}
