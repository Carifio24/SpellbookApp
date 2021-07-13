package dnd.jon.spellbook;

import android.os.Parcel;

import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Function;
import java.util.function.BiConsumer;

public class ParcelUtils {

    static private <T,R> void write(Parcel out, T item, Function<T,R> transform, BiConsumer<Parcel,R> writer) {
        writer.accept(out, transform.apply(item));
    }

    static private <T> void writeInt(Parcel out, T item, Function<T,Integer> transform) {
        write(out, item, transform, Parcel::writeInt);
    }

    static private <T> void writeString(Parcel out, T item, Function<T,String> transform) {
        write(out, item, transform, Parcel::writeString);
    }

    static private void writeNameDisplayable(Parcel out, NameDisplayable nameDisplayable) {
        writeString(out, nameDisplayable, NameDisplayable::getInternalName);
    }

    static void writeSortField(Parcel out, SortField sortField) { writeInt(out, sortField, SortField::getIndex); }
    static void writeStatusFilterField(Parcel out, StatusFilterField statusFilterField) { writeInt(out, statusFilterField, StatusFilterField::getIndex); }
    static void writeCasterClass(Parcel out, CasterClass casterClass) { writeInt(out, casterClass, CasterClass::getValue); }
    static void writeSchool(Parcel out, School school) { writeInt(out, school, School::getValue); }
    static void writeSourcebook(Parcel out, Sourcebook sourcebook) { writeInt(out, sourcebook, Sourcebook::getValue); }
    static void writeCastingTimeType(Parcel out, CastingTime.CastingTimeType castingTimeType) { writeNameDisplayable(out, castingTimeType); }
    static void writeDurationType(Parcel out, Duration.DurationType durationType) { writeNameDisplayable(out, durationType); }
    static void writeRangeType(Parcel out, Range.RangeType rangeType) { writeNameDisplayable(out, rangeType); }
    static void writeLengthUnit(Parcel out, LengthUnit lengthUnit) { writeString(out, lengthUnit, LengthUnit::getInternalName); }
    static void writeTimeUnit(Parcel out, TimeUnit timeUnit) { writeString(out, timeUnit, TimeUnit::getInternalName); }

    static private <T, R> R read(Parcel in, Function<Parcel,T> reader, Function<T,R> maker) {
        return maker.apply(reader.apply(in));
    }

    static private <T> T readFromString(Parcel in, Function<String,T> maker) {
        return read(in, Parcel::readString, maker);
    }

    static private <T> T readFromInt(Parcel in, Function<Integer,T> maker) {
        return read(in, Parcel::readInt, maker);
    }

    static SortField readSortField(Parcel in) { return readFromInt(in, SortField::fromIndex); }
    static StatusFilterField readStatusFilterField(Parcel in) { return readFromInt(in, StatusFilterField::fromIndex); }
    static CasterClass readCasterClass(Parcel in) { return readFromInt(in, CasterClass::fromValue); }
    static School readSchool(Parcel in) { return readFromInt(in, School::fromValue); }
    static Sourcebook readSourcebook(Parcel in) { return readFromInt(in, Sourcebook::fromValue); }
    static CastingTime.CastingTimeType readCastingTimeType(Parcel in) { return readFromString(in, CastingTime.CastingTimeType::fromInternalName); }
    static Duration.DurationType readDurationType(Parcel in) { return readFromString(in, Duration.DurationType::fromInternalName); }
    static Range.RangeType readRangeType(Parcel in) { return readFromString(in, Range.RangeType::fromInternalName); }
    static LengthUnit readLengthUnit(Parcel in) { return readFromString(in, LengthUnit::fromInternalName); }
    static TimeUnit readTimeUnit(Parcel in) { return readFromString(in, TimeUnit::fromInternalName); }


    static private <T,R> void writeCollection(Parcel out, Collection<T> collection, Function<T,R> transform, BiConsumer<Parcel,R> writer) {
        for (T item : collection) {
            writer.accept(out, transform.apply(item));
        }
        writer.accept(out, null);
    }

    static private <T> void writeCollectionAsStrings(Parcel out, Collection<T> collection, Function<T,String> transform) {
        writeCollection(out, collection, transform, Parcel::writeString);
    }

    static private <T> void writeCollectionAsInts(Parcel out, Collection<T> collection, Function<T,Integer> transform) {
        writeCollection(out, collection, transform, Parcel::writeInt);
    }

    static private void writeNameDisplayableCollection(Parcel out, Collection<NameDisplayable> collection) {
        writeCollection(out, collection, NameDisplayable::getInternalName, Parcel::writeString);
    }

    static void writeSourcebookCollection(Parcel out, Collection<Sourcebook> collection) { writeCollectionAsInts(out, collection, Sourcebook::getValue); }
    static void writeSchoolCollection(Parcel out, Collection<School> collection) { writeCollectionAsInts(out, collection, School::getValue); }
    static void writeCasterClassCollection(Parcel out, Collection<CasterClass> collection) { writeCollectionAsInts(out, collection, CasterClass::getValue); }
    static void writeCastingTimeTypeCollection(Parcel out, Collection<CastingTime.CastingTimeType> collection) { writeCollectionAsStrings(out, collection, CastingTime.CastingTimeType::getInternalName); }
    static void writeDurationTypeCollection(Parcel out, Collection<Duration.DurationType> collection) { writeCollectionAsStrings(out, collection, Duration.DurationType::getInternalName); }
    static void writeRangeTypeCollection(Parcel out, Collection<Range.RangeType> collection) { writeCollectionAsStrings(out, collection, Range.RangeType::getInternalName); }

    static private <E extends Enum<E>, T> EnumSet<E> readEnumSet(Parcel in, Class<E> enumType, Function<Parcel,T> reader, Function<T,E> maker) {
        final EnumSet<E> enumSet = EnumSet.noneOf(enumType);
        E e;
        while ((e = read(in, reader, maker)) != null) {
            enumSet.add(e);
        }
        return enumSet;
    }

    static private <E extends Enum<E>> EnumSet<E> readEnumSetFromInt(Parcel in, Class<E> enumType, Function<Integer,E> maker) {
        return readEnumSet(in, enumType, Parcel::readInt, maker);
    }

    static private <E extends Enum<E> > EnumSet<E> readEnumSetFromString(Parcel in, Class<E> enumType, Function<String,E> maker) {
        return readEnumSet(in, enumType, Parcel::readString, maker);
    }

    static EnumSet<SortField> readSortFieldEnumSet(Parcel in) { return readEnumSetFromInt(in, SortField.class, SortField::fromIndex); }
    static EnumSet<StatusFilterField> readStatusFilterFieldEnumSet(Parcel in) { return readEnumSetFromInt(in, StatusFilterField.class, StatusFilterField::fromIndex); }
    static EnumSet<Sourcebook> readSourcebookEnumSet(Parcel in) { return readEnumSetFromInt(in, Sourcebook.class, Sourcebook::fromValue); }
    static EnumSet<School> readSchoolEnumSet(Parcel in) { return readEnumSetFromInt(in, School.class, School::fromValue); }
    static EnumSet<CasterClass> readCasterClassEnumSet(Parcel in) { return readEnumSetFromInt(in, CasterClass.class, CasterClass::fromValue); }
    static EnumSet<CastingTime.CastingTimeType> readCastingTimeTypeEnumSet(Parcel in) { return readEnumSetFromString(in, CastingTime.CastingTimeType.class, CastingTime.CastingTimeType::fromInternalName); }
    static EnumSet<Duration.DurationType> readDurationTypeEnumSet(Parcel in) { return readEnumSetFromString(in, Duration.DurationType.class, Duration.DurationType::fromInternalName); }
    static EnumSet<Range.RangeType> readRangeTypeEnumSet(Parcel in) { return readEnumSetFromString(in, Range.RangeType.class, Range.RangeType::fromInternalName); }

}
