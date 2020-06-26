package dnd.jon.spellbook;

import androidx.room.TypeConverter;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class SpellbookTypeConverters {

    @TypeConverter public static String convertSourcebookToString(Sourcebook sourcebook) { return sourcebook.getCode(); }
    @TypeConverter public static String convertCasterClassToString(CasterClass casterClass) { return casterClass.getDisplayName(); }
    @TypeConverter public static String convertSchoolToString(School school) { return school.getDisplayName(); }
    @TypeConverter public static String convertCastingTimeTypeToString(CastingTime.CastingTimeType castingTimeType) { return castingTimeType.getDisplayName(); }
    @TypeConverter public static String convertDurationTypeToString(Duration.DurationType durationType) { return durationType.getDisplayName(); }
    @TypeConverter public static String convertRangeTypeToString(Range.RangeType rangeType) { return rangeType.getDisplayName(); }
    @TypeConverter public static String convertSortFieldToString(SortField sortField) { return sortField.getDisplayName(); }
    @TypeConverter public static String convertStatusFilterFieldToString(StatusFilterField statusFilterField) { return statusFilterField.getDisplayName(); }
    @TypeConverter public static String convertUnitToString(Unit unit) { return unit.singularName(); }
    @TypeConverter public static Range.RangeType convertStringToRangeType(String name) { return Range.RangeType.fromDisplayName(name); }
    @TypeConverter public static Duration.DurationType convertStringToDurationType(String name) { return Duration.DurationType.fromDisplayName(name); }
    @TypeConverter public static CastingTime.CastingTimeType convertStringToCastingTimeType(String name) { return CastingTime.CastingTimeType.fromParseName(name); }
    @TypeConverter public static School convertStringToSchool(String name) { return School.fromDisplayName(name); }
    @TypeConverter public static Sourcebook convertStringToSourcebook(String code) { return Sourcebook.fromCode(code); }
    @TypeConverter public static CasterClass convertStringToCasterClass(String name) { return CasterClass.fromDisplayName(name); }
    @TypeConverter public static SortField convertStringToSortField(String name) { return SortField.fromDisplayName(name); }
    @TypeConverter public static StatusFilterField convertStringToStatusFilterField(String name) { return StatusFilterField.fromDisplayName(name); }
    @TypeConverter public static String convertSpellStatusesMapToString(Map<String,SpellStatus> map) { return new JSONObject(map).toString(); }
    @TypeConverter public static Map<String,SpellStatus> convertStringToSpellStatusesMap(String string) {
        final Map<String,SpellStatus> map = new HashMap<>();
        try {
            final JSONObject json = new JSONObject(string);
            final Iterator<String> keyIterator = json.keys();
            while (keyIterator.hasNext()) {
                final String key = keyIterator.next();
                final SpellStatus status = (SpellStatus) json.get(key);
                map.put(key, status);
            }
        } catch (JSONException e) { e.printStackTrace(); }
        return map;
    }

    //@TypeConverter public static <V extends Enum<V> & QuantityType,U extends Unit> String convertQuantityToString(Quantity<V,U> quantity) { return quantity.string(); }
    @TypeConverter public static Range convertStringToRange(String s) { return Range.fromString(s); }
    @TypeConverter public static Duration convertStringToDuration(String s) { return Duration.fromString(s); }
    @TypeConverter public static CastingTime convertStringToCastingTime(String s) { return CastingTime.fromString(s); }
    @TypeConverter public static TimeUnit convertStringToTimeUnit(String s) { try { return TimeUnit.fromString(s); } catch (Exception e) { return TimeUnit.SECOND; } }
    @TypeConverter public static LengthUnit convertStringToLengthUnit(String s) { try { return LengthUnit.fromString(s); } catch (Exception e) { return LengthUnit.FOOT; } }


    static <T> String convertIterableToString(Iterable<T> iterable, CharSequence separator, Function<T,String> stringify) {
        final Iterator<T> it = iterable.iterator();
        if (!it.hasNext()) { return ""; }
        final StringBuilder sb = new StringBuilder(stringify.apply(it.next()));
        while (it.hasNext()) {
            sb.append(separator).append(stringify.apply(it.next()));
        }
        return sb.toString();
    }

    static <T> String convertIterableToString(Iterable<T> iterable, Function<T,String> stringify) { return convertIterableToString(iterable, ",", stringify); }
    static <T extends Named> String convertNamedIterableToString(Iterable<T> iterable) { return convertIterableToString(iterable, ",", T::getDisplayName); }

    public static <T> List<T> convertStringToList(String string, String separator, Function<String,T> converter) {
        final String[] splits = string.split(separator);
        final List<T> list = new ArrayList<>();
        for (String s : splits) {
            list.add(converter.apply(s));
        }
        return list;
    }

    public static <T> Set<T> convertStringToSet(String string, String separator, Function<String,T> converter) {
        final String[] splits = string.split(separator);
        final Set<T> set = new HashSet<>();
        for (String s : splits) {
            set.add(converter.apply(s));
        }
        return set;
    }

    public static <T extends Enum<T>> EnumSet<T> convertStringToEnumSet(String string, String separator, Class<T> type, Function<String,T> converter) {
        final String[] splits = string.split(separator);
        final EnumSet<T> set = EnumSet.noneOf(type);
        for (String s : splits) {
            set.add(converter.apply(s));
        }
        return set;
    }

    @TypeConverter public static String convertNamedCollectionToString(Collection<Named> collection) { return convertNamedIterableToString(collection); }
    @TypeConverter public static String convertCasterClassCollectionToString(Collection<CasterClass> collection) { return convertNamedIterableToString(collection); }
    @TypeConverter public static String convertCasterClassEnumSetToString(EnumSet<CasterClass> collection) { return convertNamedIterableToString(collection); }
    @TypeConverter public static String convertSourcebookSetToString(Set<Sourcebook> collection) { return convertIterableToString(collection, Sourcebook::getCode); }
    @TypeConverter public static String convertSchoolEnumSetToString(EnumSet<School> collection) { return convertNamedIterableToString(collection); }
    @TypeConverter public static String convertDurationTypeEnumSetToString(EnumSet<Duration.DurationType> collection) { return convertNamedIterableToString(collection); }
    @TypeConverter public static String convertCastingTimeTypeEnumSetToString(EnumSet<CastingTime.CastingTimeType> collection) { return convertNamedIterableToString(collection); }
    @TypeConverter public static String convertRangeTypeEnumSetToString(EnumSet<Range.RangeType> collection) { return convertNamedIterableToString(collection); }

    @TypeConverter public static String convertSubClassListToString(List<SubClass> list) { return convertNamedIterableToString(list); }

    @TypeConverter public static List<CasterClass> convertStringToCasterClassList(String string) { return convertStringToList(string, ",", CasterClass::fromDisplayName ); }
    @TypeConverter public static List<SubClass> convertStringToSubClassList(String string) { return convertStringToList(string, ",", SubClass::fromDisplayName ); }
    @TypeConverter public static EnumSet<CasterClass> convertStringToCasterClassEnumSet(String string) { return convertStringToEnumSet(string, ",", CasterClass.class, CasterClass::fromDisplayName); }
    @TypeConverter public static EnumSet<School> convertStringToSchoolEnumSet(String string) { return convertStringToEnumSet(string, ",", School.class, School::fromDisplayName); }
    @TypeConverter public static Set<Sourcebook> convertStringToSourcebookSet(String string) { System.out.println("String is " + string); return convertStringToSet(string, ",", Sourcebook::fromCode); }
    @TypeConverter public static EnumSet<CastingTime.CastingTimeType> convertStringToCastingTimeTypeEnumSet(String string) { return convertStringToEnumSet(string, ",", CastingTime.CastingTimeType.class, CastingTime.CastingTimeType::fromDisplayName); }
    @TypeConverter public static EnumSet<Duration.DurationType> convertStringToDurationTypeEnumSet(String string) { return convertStringToEnumSet(string, ",", Duration.DurationType.class, Duration.DurationType::fromDisplayName); }
    @TypeConverter public static EnumSet<Range.RangeType> convertStringToRangeTypeEnumSet(String string) { return convertStringToEnumSet(string, ",", Range.RangeType.class, Range.RangeType::fromDisplayName); }

//    @TypeConverter
//    public static String convertBoolArrayToString(boolean[] arr) {
//        final StringBuilder sb = new StringBuilder();
//        final String separator = ",";
//        for (int i = 0; i < arr.length; ++i) {
//            sb.append(arr[i] ? "true" : "false");
//            if (i != arr.length - 1) {
//                sb.append(separator);
//            }
//        }
//        return sb.toString();
//    }


}
