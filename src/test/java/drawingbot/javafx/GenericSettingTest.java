package drawingbot.javafx;

import com.google.gson.JsonObject;
import drawingbot.javafx.settings.*;
import javafx.scene.paint.Color;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GenericSettingTest extends TestCase {

    public List<GenericSetting<?, ?>> settingList;

    public BooleanSetting<SettingsTest> testBooleanSetting;
    public StringSetting<SettingsTest> testStringSetting;

    public IntegerSetting<SettingsTest> testIntSetting;
    public FloatSetting<SettingsTest> testFloatSetting;
    public DoubleSetting<SettingsTest> testDoubleSetting;
    public LongSetting<SettingsTest> testLongSetting;

    public IntegerSetting<SettingsTest> testRangedIntSetting;
    public FloatSetting<SettingsTest> testRangedFloatSetting;
    public DoubleSetting<SettingsTest> testRangedDoubleSetting;
    public LongSetting<SettingsTest> testRangedLongSetting;

    public ColourSetting<SettingsTest> testColorSetting;
    public ListSetting<SettingsTest, Integer> testListSetting;
    public ObjectSetting<SettingsTest, ObjectTest> testObjectSetting;
    public OptionSetting<SettingsTest, OptionTest> testOptionSetting;


    public void setUp() throws Exception {
        super.setUp();
        settingList = new ArrayList<>();
        settingList.add(testBooleanSetting = (BooleanSetting<SettingsTest>) GenericSetting.createBooleanSetting(SettingsTest.class, "testBoolean", false, (c, v) -> c.testBoolean = v).setGetter(c -> c.testBoolean));
        settingList.add(testStringSetting = (StringSetting<SettingsTest>) GenericSetting.createStringSetting(SettingsTest.class, "testString", "TestValue: {}:@~~<>?", (c, v) -> c.testString = v).setGetter(c -> c.testString));

        settingList.add(testIntSetting = (IntegerSetting<SettingsTest>) GenericSetting.createIntSetting(SettingsTest.class, "testInt", 1600, (c, v) -> c.testInt = v).setGetter(c -> c.testInt));
        settingList.add(testFloatSetting = (FloatSetting<SettingsTest>) GenericSetting.createFloatSetting(SettingsTest.class, "testFloat", 3200F, (c, v) -> c.testFloat = v).setGetter(c -> c.testFloat));
        settingList.add(testDoubleSetting = (DoubleSetting<SettingsTest>) GenericSetting.createDoubleSetting(SettingsTest.class, "testDouble", 6400D, (c, v) -> c.testDouble = v).setGetter(c -> c.testDouble));
        settingList.add(testLongSetting = (LongSetting<SettingsTest>) GenericSetting.createLongSetting(SettingsTest.class, "testLong", 12800L, (c, v) -> c.testLong = v).setGetter(c -> c.testLong));

        settingList.add(testRangedIntSetting = (IntegerSetting<SettingsTest>) GenericSetting.createRangedIntSetting(SettingsTest.class, "testRangedInt", 1600, 100, 50000, (c, v) -> c.testRangedInt = v).setGetter(c -> c.testRangedInt));
        settingList.add(testRangedFloatSetting = (FloatSetting<SettingsTest>) GenericSetting.createRangedFloatSetting(SettingsTest.class, "testRangedFloat", 3200F, 100, 50000, (c, v) -> c.testRangedFloat = v).setGetter(c -> c.testRangedFloat));
        settingList.add(testRangedDoubleSetting = (DoubleSetting<SettingsTest>) GenericSetting.createRangedDoubleSetting(SettingsTest.class, "testRangedDouble", 6400D, 100, 50000, (c, v) -> c.testRangedDouble = v).setGetter(c -> c.testRangedDouble));
        settingList.add(testRangedLongSetting = (LongSetting<SettingsTest>) GenericSetting.createRangedLongSetting(SettingsTest.class, "testRangedLong", 12800L, 100, 50000, (c, v) -> c.testRangedLong = v).setGetter(c -> c.testRangedLong));

        settingList.add(testColorSetting = (ColourSetting<SettingsTest>) GenericSetting.createColourSetting(SettingsTest.class, "testColor", Color.AQUA, (c, v) -> c.testColor = v).setGetter(c -> c.testColor));
        settingList.add(testListSetting = (ListSetting<SettingsTest, Integer>) GenericSetting.createListSetting(SettingsTest.class, Integer.class, "testList", new ArrayList<>(List.of(100, 500, 1000, 1500)), (c, v) -> c.testList = v).setGetter(c -> c.testList));
        settingList.add(testObjectSetting = (ObjectSetting<SettingsTest, ObjectTest>) GenericSetting.createObjectSetting(SettingsTest.class, ObjectTest.class, "testObject", new ObjectTest("test", 100), (c, v) -> c.testObject = v).setGetter(c -> c.testObject));
        settingList.add(testOptionSetting = (OptionSetting<SettingsTest, OptionTest>) GenericSetting.createOptionSetting(SettingsTest.class, OptionTest.class, "testOption", List.of(OptionTest.values()), OptionTest.TEST1, (c, v) -> c.testOption = v).setGetter(c -> c.testOption));

    }

    public void testSerializeSettings(){
        SettingsTest testA = new SettingsTest();
        GenericSetting.applySettingsToInstance(settingList, testA);
        JsonObject jsonObjectA = GenericSetting.toJsonObject(settingList, testA, false);

        SettingsTest testB = new SettingsTest();
        GenericSetting.fromJsonObject(jsonObjectA, settingList, testB, false);
        JsonObject jsonObjectB = GenericSetting.toJsonObject(settingList, testB, false);

        assertEquals(testA, testB);
        assertEquals(jsonObjectA, jsonObjectB);
    }

    public void testBooleanSetting(){
        SettingsTest test = new SettingsTest();
        Boolean original = testBooleanSetting.getValue();
        testBooleanSetting.applySetting(test);
        Boolean value = testBooleanSetting.getValueFromInstance(test);
        assertEquals(original, value);
    }

    public void testStringSetting(){
        SettingsTest test = new SettingsTest();
        String original = testStringSetting.getValue();
        testStringSetting.applySetting(test);
        String value = testStringSetting.getValueFromInstance(test);
        assertEquals(original, value);
    }

    public void testIntSetting(){
        SettingsTest test = new SettingsTest();
        Integer original = testIntSetting.getValue();
        testIntSetting.applySetting(test);
        Integer value = testIntSetting.getValueFromInstance(test);
        assertEquals(original, value);
    }

    public void testFloatSetting(){
        SettingsTest test = new SettingsTest();
        Float original = testFloatSetting.getValue();
        testFloatSetting.applySetting(test);
        Float value = testFloatSetting.getValueFromInstance(test);
        assertEquals(original, value);
    }

    public void testDoubleSetting(){
        SettingsTest test = new SettingsTest();
        Double original = testDoubleSetting.getValue();
        testDoubleSetting.applySetting(test);
        Double value = testDoubleSetting.getValueFromInstance(test);
        assertEquals(original, value);
    }

    public void testLongSetting(){
        SettingsTest test = new SettingsTest();
        Long original = testLongSetting.getValue();
        testLongSetting.applySetting(test);
        Long value = testLongSetting.getValueFromInstance(test);
        assertEquals(original, value);
    }

    public void testRangedIntMinSetting(){
        SettingsTest test = new SettingsTest();
        testRangedIntSetting.applySetting(test, 0);
        Integer value = testRangedIntSetting.getValueFromInstance(test);
        assertEquals(testRangedIntSetting.minValue, value);
    }

    public void testRangedIntMaxSetting(){
        SettingsTest test = new SettingsTest();
        testRangedIntSetting.applySetting(test, Integer.MAX_VALUE);
        Integer value = testRangedIntSetting.getValueFromInstance(test);
        assertEquals(testRangedIntSetting.maxValue, value);
    }

    public void testRangedFloatMinSetting(){
        SettingsTest test = new SettingsTest();
        testRangedFloatSetting.applySetting(test, 0F);
        Float value = testRangedFloatSetting.getValueFromInstance(test);
        assertEquals(testRangedFloatSetting.minValue, value);
    }

    public void testRangedFloatMaxSetting(){
        SettingsTest test = new SettingsTest();
        testRangedFloatSetting.applySetting(test, Float.MAX_VALUE);
        Float value = testRangedFloatSetting.getValueFromInstance(test);
        assertEquals(testRangedFloatSetting.maxValue, value);
    }

    public void testRangedDoubleMinSetting(){
        SettingsTest test = new SettingsTest();
        testRangedDoubleSetting.applySetting(test, 0D);
        Double value = testRangedDoubleSetting.getValueFromInstance(test);
        assertEquals(testRangedDoubleSetting.minValue, value);
    }

    public void testRangedDoubleMaxSetting(){
        SettingsTest test = new SettingsTest();
        testRangedDoubleSetting.applySetting(test, Double.MAX_VALUE);
        Double value = testRangedDoubleSetting.getValueFromInstance(test);
        assertEquals(testRangedDoubleSetting.maxValue, value);
    }

    public void testRangedLongMinSetting(){
        SettingsTest test = new SettingsTest();
        testRangedLongSetting.applySetting(test, 0L);
        Long value = testRangedLongSetting.getValueFromInstance(test);
        assertEquals(testRangedLongSetting.minValue, value);
    }

    public void testRangedLongMaxSetting(){
        SettingsTest test = new SettingsTest();
        testRangedLongSetting.applySetting(test, Long.MAX_VALUE);
        Long value = testRangedLongSetting.getValueFromInstance(test);
        assertEquals(testRangedLongSetting.maxValue, value);
    }

    public void testColorSetting(){
        SettingsTest test = new SettingsTest();
        Color original = testColorSetting.getValue();
        testColorSetting.applySetting(test);
        Color value = testColorSetting.getValueFromInstance(test);
        assertEquals(original, value);
    }

    public void testListSetting(){
        SettingsTest test = new SettingsTest();
        List<Integer> original = testListSetting.getValue();
        testListSetting.applySetting(test);
        List<Integer> value = testListSetting.getValueFromInstance(test);
        Assert.assertArrayEquals(original.toArray(new Integer[0]), value.toArray(new Integer[0]));
    }

    public void testObjectSetting(){
        SettingsTest test = new SettingsTest();
        ObjectTest original = testObjectSetting.getValue();
        testObjectSetting.applySetting(test);
        ObjectTest value = testObjectSetting.getValueFromInstance(test);
        assertEquals(original, value);
    }

    public void testOptionSetting(){
        SettingsTest test = new SettingsTest();
        OptionTest original = testOptionSetting.getValue();
        testOptionSetting.applySetting(test);
        OptionTest value = testOptionSetting.getValueFromInstance(test);
        assertEquals(original, value);
    }


    public static class SettingsTest{

        public boolean testBoolean;
        public String testString;

        public int testInt;
        public float testFloat;
        public double testDouble;
        public long testLong;

        public int testRangedInt;
        public float testRangedFloat;
        public double testRangedDouble;
        public long testRangedLong;

        public Color testColor;
        public ArrayList<Integer> testList;
        public ObjectTest testObject;
        public OptionTest testOption;

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof SettingsTest){
                SettingsTest other = (SettingsTest) obj;
                return Objects.equals(testBoolean, other.testBoolean)
                        && Objects.equals(testString, other.testString)
                        && Objects.equals(testInt, other.testInt)
                        && Objects.equals(testFloat, other.testFloat)
                        && Objects.equals(testDouble, other.testDouble)
                        && Objects.equals(testLong, other.testLong)
                        && Objects.equals(testRangedInt, other.testRangedInt)
                        && Objects.equals(testRangedFloat, other.testRangedFloat)
                        && Objects.equals(testRangedDouble, other.testRangedDouble)
                        && Objects.equals(testRangedLong, other.testRangedLong)
                        && Objects.equals(testColor, other.testColor)
                        && Objects.equals(testList, other.testList)
                        && Objects.equals(testObject, other.testObject);
            }
            return super.equals(obj);
        }
    }

    public static class ObjectTest{

        public String name;
        public int num;

        public ObjectTest(){}

        public ObjectTest(String name, int num) {
            this.name = name;
            this.num = num;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof ObjectTest){
                ObjectTest other = (ObjectTest) obj;
                return Objects.equals(name, other.name) && Objects.equals(num, other.num);
            }
            return super.equals(obj);
        }
    }

    public enum OptionTest{
        TEST1,
        TEST2,
        TEST3,
        TEST4,
        TEST5;
    }
}