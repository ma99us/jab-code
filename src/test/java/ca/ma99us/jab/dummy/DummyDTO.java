package ca.ma99us.jab.dummy;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class DummyDTO {
    private String name;
    private int age;
    private Long id;

    private DummyItemDTO item;

    private byte[] bytes;
    private int[] numbers;
    private Long[] bigNumbers;
    private List<Integer> numbersList;
    private Map<String, Integer> numbersMap;

    private DummyItemDTO[] items;
    private List<DummyItemDTO> itemsList;
    private Map<String, DummyItemDTO> itemsMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DummyItemDTO getItem() {
        return item;
    }

    public void setItem(DummyItemDTO item) {
        this.item = item;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int[] getNumbers() {
        return numbers;
    }

    public void setNumbers(int[] numbers) {
        this.numbers = numbers;
    }

    public Long[] getBigNumbers() {
        return bigNumbers;
    }

    public void setBigNumbers(Long[] bigNumbers) {
        this.bigNumbers = bigNumbers;
    }

    public List<Integer> getNumbersList() {
        return numbersList;
    }

    public void setNumbersList(List<Integer> numbersList) {
        this.numbersList = numbersList;
    }

    public Map<String, Integer> getNumbersMap() {
        return numbersMap;
    }

    public void setNumbersMap(Map<String, Integer> numbersMap) {
        this.numbersMap = numbersMap;
    }

    public DummyItemDTO[] getItems() {
        return items;
    }

    public void setItems(DummyItemDTO[] items) {
        this.items = items;
    }

    public List<DummyItemDTO> getItemsList() {
        return itemsList;
    }

    public void setItemsList(List<DummyItemDTO> itemsList) {
        this.itemsList = itemsList;
    }

    public Map<String, DummyItemDTO> getItemsMap() {
        return itemsMap;
    }

    public void setItemsMap(Map<String, DummyItemDTO> itemsMap) {
        this.itemsMap = itemsMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DummyDTO dummyDTO = (DummyDTO) o;
        return age == dummyDTO.age && Objects.equals(name, dummyDTO.name) && Objects.equals(id, dummyDTO.id) && Objects.equals(item, dummyDTO.item) && Arrays.equals(bytes, dummyDTO.bytes) && Arrays.equals(numbers, dummyDTO.numbers) && Arrays.equals(bigNumbers, dummyDTO.bigNumbers) && Objects.equals(numbersList, dummyDTO.numbersList) && Objects.equals(numbersMap, dummyDTO.numbersMap) && Arrays.equals(items, dummyDTO.items) && Objects.equals(itemsList, dummyDTO.itemsList) && Objects.equals(itemsMap, dummyDTO.itemsMap);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, age, id, item, numbersList, numbersMap, itemsList, itemsMap);
        result = 31 * result + Arrays.hashCode(bytes);
        result = 31 * result + Arrays.hashCode(numbers);
        result = 31 * result + Arrays.hashCode(bigNumbers);
        result = 31 * result + Arrays.hashCode(items);
        return result;
    }

    @Override
    public String toString() {
        return "DummyDTO{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", id=" + id +
                ", item=" + item +
                ", bytes=" + Arrays.toString(bytes) +
                ", numbers=" + Arrays.toString(numbers) +
                ", bigNumbers=" + Arrays.toString(bigNumbers) +
                ", numbersList=" + numbersList +
                ", numbersMap=" + numbersMap +
                ", items=" + Arrays.toString(items) +
                ", itemsList=" + itemsList +
                ", itemsMap=" + itemsMap +
                '}';
    }

    public static class DummyItemDTO {
        private long itemId;
        private String itemName;

        public static DummyItemDTO makeDummyItemDTO(int itemId) {
            DummyItemDTO item = new DummyItemDTO();
            item.itemId = itemId;
            item.itemName = "Item #" + item.itemId;
            return item;
        }

        public long getItemId() {
            return itemId;
        }

        public void setItemId(long itemId) {
            this.itemId = itemId;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DummyItemDTO that = (DummyItemDTO) o;
            return itemId == that.itemId && Objects.equals(itemName, that.itemName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(itemId, itemName);
        }

        @Override
        public String toString() {
            return "DummyItemDTO{" +
                    "itemId=" + itemId +
                    ", itemName='" + itemName + '\'' +
                    '}';
        }
    }

    public static DummyDTO makeDummyDTO(boolean withComposition, boolean withCollections) {
        DummyDTO dto = new DummyDTO();
        dto.name = "Some Name";
        dto.age = 123456;
        dto.id = null;

        if (withComposition) {
            dto.item = DummyItemDTO.makeDummyItemDTO(69);
        }

        if (withCollections) {
            dto.bytes = "SomeBytes".getBytes(StandardCharsets.UTF_8);
            dto.numbers = new int[]{0, 1, 2, 3};
            dto.bigNumbers = new Long[]{5L, 6L, 7L, 0L};
            dto.numbersList = new ArrayList<Integer>() {{
                add(9);
                add(8);
                add(7);
            }};
            dto.numbersMap = new HashMap<String, Integer>() {{
                put("#4", 4);
                put("#5", 5);
                put("#6", 6);
            }};

            if (withComposition) {
                dto.items = new DummyItemDTO[2];
                dto.items[0] = DummyItemDTO.makeDummyItemDTO(0);
                dto.items[1] = DummyItemDTO.makeDummyItemDTO(1);

                dto.itemsList = new ArrayList<DummyItemDTO>() {{
                    add(DummyItemDTO.makeDummyItemDTO(2));
                    add(DummyItemDTO.makeDummyItemDTO(3));
                    add(DummyItemDTO.makeDummyItemDTO(4));
                }};
                dto.itemsMap = new HashMap<String, DummyItemDTO>() {{
                    DummyItemDTO item = DummyItemDTO.makeDummyItemDTO(5);
                    put(item.itemId + "#", item);
                    item = DummyItemDTO.makeDummyItemDTO(6);
                    put(item.itemId + "#", item);
                    item = DummyItemDTO.makeDummyItemDTO(7);
                    put(item.itemId + "#", item);
                }};
            }
        }

        return dto;
    }
}
