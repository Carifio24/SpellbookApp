package dnd.jon.spellbook;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;


public class TypeConverters {

    @TypeConverter public static String convertSourcebooktoString(Sourcebook sourcebook) { return sourcebook.getCode(); }
    @TypeConverter public static String convertCasterClassToString(CasterClass casterClass) { return casterClass.getDisplayName(); }
    @TypeConverter public static String convertSchoolToString(School school) { return school.getDisplayName(); }
    @TypeConverter public static String convertCastingTimeTypeToString(CastingTime.CastingTimeType castingTimeType) { return castingTimeType.getDisplayName(); }
    @TypeConverter public static String convertDurationTypeToString(Duration.DurationType durationType) { return durationType.getDisplayName(); }
    @TypeConverter public static String convertRangeTypeToString(Range.RangeType rangeType) { return rangeType.getDisplayName(); }
    @TypeConverter public static String convertSortFieldToString(SortField sortField) { return sortField.getDisplayName(); }
    @TypeConverter public static String convertStatusFilterFieldToString(StatusFilterField statusFilterField) { return statusFilterField.getDisplayName(); }
    @TypeConverter public static Range.RangeType convertStringToRangeType(String name) { return Range.RangeType.fromDisplayName(name); }
    @TypeConverter public static Duration.DurationType convertStringToDurationType(String name) { return Duration.DurationType.fromDisplayName(name); }
    @TypeConverter public static CastingTime.CastingTimeType convertStringToCastingTimeType(String name) { return CastingTime.CastingTimeType.fromDisplayName(name); }
    @TypeConverter public static School convertStringToSchool(String name) { return School.fromDisplayName(name); }
    @TypeConverter public static Sourcebook convertStringToSourcebook(String code) { return Sourcebook.fromCode(code); }
    @TypeConverter public static CasterClass convertStringToCasterClass(String name) { return CasterClass.fromDisplayName(name); }
    @TypeConverter public static StatusFilterField convertStringToStatusFilterField(String name) { return StatusFilterField.fromDisplayName(name); }

    @TypeConverter public static String convertQuantityToString(Quantity quantity) { return quantity.string(); }
    @TypeConverter public static Range convertStringToRange(String s) { return Range.fromString(s); }
    @TypeConverter public static Duration convertStringToDuration(String s) { return Duration.fromString(s); }
    @TypeConverter public static CastingTime convertStringToCastingTime(String s) { return CastingTime.fromString(s); }

    static <T> String convertIterableToString(Iterable<T> iterable, CharSequence separator, Function<T,String> stringify) {
        final Iterator<T> it = iterable.iterator();
        if (!it.hasNext()) { return ""; }
        final StringBuilder sb = new StringBuilder(stringify.apply(it.next()));
        while (it.hasNext()) {
            sb.append(separator);
            sb.append(stringify.apply(it.next()));
        }
        return sb.toString();
    }

    public static <T> List<T> convertStringToList(String string, String separator, Function<String,T> converter) {
        final String[] splits = string.split(separator);
        final List<T> list = new ArrayList<>();
        for (String s : splits) {
            list.add(converter.apply(s));
        }
        return list;
    }


    @TypeConverter public static <T extends Named> String convertNamedListToString(List<T> list) { return convertIterableToString(list, ",", T::getDisplayName); }
    @TypeConverter public static List<CasterClass> convertStringToCasterClassList(String string) { return convertStringToList(string, ",", CasterClass::fromDisplayName ); }
    @TypeConverter public static List<SubClass> convertStringToSubClassList(String string) { return convertStringToList(string, ",", SubClass::fromDisplayName ); }


    @TypeConverter
    public static String convertBoolArrayToString(boolean[] arr) {
        final StringBuilder sb = new StringBuilder();
        final String separator = ",";
        for (int i = 0; i < arr.length; ++i) {
            sb.append(arr[i] ? "true" : "false");
            if (i != arr.length - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }


}
