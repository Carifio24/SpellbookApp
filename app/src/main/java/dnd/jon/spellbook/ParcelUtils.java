package dnd.jon.spellbook;

import android.os.Parcel;

import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Function;
import java.util.function.BiConsumer;

public class ParcelUtils {

    static private <T,R> void write(Parcel out, T item, Function<T,R> transform, BiConsumer<Parcel,R> writer) {
        final R toWrite = transform.apply(item);
        writer.accept(out, toWrite);
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
    static void writeSourcebook(Parcel out, Source source) { writeInt(out, source, Source::getValue); }
    static void writeCastingTimeType(Parcel out, CastingTime.CastingTimeType castingTimeType) { writeNameDisplayable(out, castingTimeType); }
    static void writeDurationType(Parcel out, Duration.DurationType durationType) { writeNameDisplayable(out, durationType); }
    static void writeRangeType(Parcel out, Range.RangeType rangeType) { writeNameDisplayable(out, rangeType); }
    static void writeLengthUnit(Parcel out, LengthUnit lengthUnit) { writeString(out, lengthUnit, LengthUnit::getInternalName); }
    static void writeTimeUnit(Parcel out, TimeUnit timeUnit) { writeString(out, timeUnit, TimeUnit::getInternalName); }

    static private <T> void writeCollection(Parcel out, Collection<T> collection, BiConsumer<Parcel,T> writer) {
        out.writeInt(collection.size());
        for (T item : collection) {
            writer.accept(out, item);
        }
    }

    static void writeSourcebookCollection(Parcel out, Collection<Source> collection) { writeCollection(out, collection, ParcelUtils::writeSourcebook); }
    static void writeSchoolCollection(Parcel out, Collection<School> collection) { writeCollection(out, collection, ParcelUtils::writeSchool); }
    static void writeCasterClassCollection(Parcel out, Collection<CasterClass> collection) { writeCollection(out, collection, ParcelUtils::writeCasterClass); }
    static void writeCastingTimeTypeCollection(Parcel out, Collection<CastingTime.CastingTimeType> collection) { writeCollection(out, collection, ParcelUtils::writeCastingTimeType); }
    static void writeDurationTypeCollection(Parcel out, Collection<Duration.DurationType> collection) { writeCollection(out, collection, ParcelUtils::writeDurationType); }
    static void writeRangeTypeCollection(Parcel out, Collection<Range.RangeType> collection) { writeCollection(out, collection, ParcelUtils::writeRangeType); }


    static private <T, R> R read(Parcel in, Function<Parcel,T> reader, Function<T,R> maker) {
        final T representation = reader.apply(in);
        final R result = maker.apply(representation);
        return result;
        //return maker.apply(reader.apply(in));
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
    static Source readSourcebook(Parcel in) { return readFromInt(in, Source::fromValue); }
    static CastingTime.CastingTimeType readCastingTimeType(Parcel in) { return readFromString(in, CastingTime.CastingTimeType::fromInternalName); }
    static Duration.DurationType readDurationType(Parcel in) { return readFromString(in, Duration.DurationType::fromInternalName); }
    static Range.RangeType readRangeType(Parcel in) { return readFromString(in, Range.RangeType::fromInternalName); }
    static LengthUnit readLengthUnit(Parcel in) { return readFromString(in, LengthUnit::fromInternalName); }
    static TimeUnit readTimeUnit(Parcel in) { return readFromString(in, TimeUnit::fromInternalName); }

    static private <E extends Enum<E>> EnumSet<E> readEnumSet(Parcel in, Class<E> enumType, Function<Parcel,E> reader) {
        final EnumSet<E> enumSet = EnumSet.noneOf(enumType);
        final int size = in.readInt();
        for (int i = 0; i < size; i++) {
            final E e = reader.apply(in);
            enumSet.add(e);
        }
        return enumSet;
    }

    //static EnumSet<SortField> readSortFieldEnumSet(Parcel in) { return readEnumSet(in, SortField.class, ParcelUtils::readSortField); }
    //static EnumSet<StatusFilterField> readStatusFilterFieldEnumSet(Parcel in) { return readEnumSet(in, StatusFilterField.class, ParcelUtils::readStatusFilterField); }
    static EnumSet<Source> readSourcebookEnumSet(Parcel in) { return readEnumSet(in, Source.class, ParcelUtils::readSourcebook); }
    static EnumSet<School> readSchoolEnumSet(Parcel in) { return readEnumSet(in, School.class, ParcelUtils::readSchool); }
    static EnumSet<CasterClass> readCasterClassEnumSet(Parcel in) { return readEnumSet(in, CasterClass.class, ParcelUtils::readCasterClass); }
    static EnumSet<CastingTime.CastingTimeType> readCastingTimeTypeEnumSet(Parcel in) { return readEnumSet(in, CastingTime.CastingTimeType.class, ParcelUtils::readCastingTimeType); }
    static EnumSet<Duration.DurationType> readDurationTypeEnumSet(Parcel in) { return readEnumSet(in, Duration.DurationType.class, ParcelUtils::readDurationType); }
    static EnumSet<Range.RangeType> readRangeTypeEnumSet(Parcel in) { return readEnumSet(in, Range.RangeType.class, ParcelUtils::readRangeType); }
}
