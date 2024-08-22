package dnd.jon.spellbook;

import android.os.Parcel;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

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
    static void writeSourcebook(Parcel out, Source source) { writeInt(out, source, Source::getValue); }
    static void writeCastingTimeType(Parcel out, CastingTime.CastingTimeType castingTimeType) { writeNameDisplayable(out, castingTimeType); }
    static void writeDurationType(Parcel out, Duration.DurationType durationType) { writeNameDisplayable(out, durationType); }
    static void writeRangeType(Parcel out, Range.RangeType rangeType) { writeNameDisplayable(out, rangeType); }
    static void writeLengthUnit(Parcel out, LengthUnit lengthUnit) { writeString(out, lengthUnit, LengthUnit::getInternalName); }
    static void writeTimeUnit(Parcel out, TimeUnit timeUnit) { writeString(out, timeUnit, TimeUnit::getInternalName); }
    static void writeRuleset(Parcel out, Ruleset ruleset) { writeInt(out, ruleset, Ruleset::getValue);}

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
        return maker.apply(representation);
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
    static Source readSource(Parcel in) { return readFromInt(in, Source::fromValue); }
    static CastingTime.CastingTimeType readCastingTimeType(Parcel in) { return readFromString(in, CastingTime.CastingTimeType::fromInternalName); }
    static Duration.DurationType readDurationType(Parcel in) { return readFromString(in, Duration.DurationType::fromInternalName); }
    static Range.RangeType readRangeType(Parcel in) { return readFromString(in, Range.RangeType::fromInternalName); }
    static LengthUnit readLengthUnit(Parcel in) { return readFromString(in, LengthUnit::fromInternalName); }
    static TimeUnit readTimeUnit(Parcel in) { return readFromString(in, TimeUnit::fromInternalName); }
    static Ruleset readRuleset(Parcel in) { return readFromInt(in, Ruleset::fromValue); }

    static private <T, S extends Set<T>> S readSet(Parcel in, Function<Parcel,T> reader, Supplier<S> setProducer) {
        final S set = setProducer.get();
        final int size = in.readInt();
        for (int i = 0; i < size; i++) {
            final T t = reader.apply(in);
            set.add(t);
        }
        return set;
    }

    static private <T> Set<T> readSet(Parcel in, Function<Parcel,T> reader) {
        return readSet(in, reader, HashSet::new);
    }

    static private <E extends Enum<E>> EnumSet<E> readEnumSet(Parcel in, Class<E> enumType, Function<Parcel,E> reader) {
        return readSet(in, reader, () -> EnumSet.noneOf(enumType));
    }

    //static EnumSet<SortField> readSortFieldEnumSet(Parcel in) { return readEnumSet(in, SortField.class, ParcelUtils::readSortField); }
    //static EnumSet<StatusFilterField> readStatusFilterFieldEnumSet(Parcel in) { return readEnumSet(in, StatusFilterField.class, ParcelUtils::readStatusFilterField); }
    static Set<Source> readSourceSet(Parcel in) { return readSet(in, ParcelUtils::readSource); }
    static EnumSet<School> readSchoolEnumSet(Parcel in) { return readEnumSet(in, School.class, ParcelUtils::readSchool); }
    static EnumSet<CasterClass> readCasterClassEnumSet(Parcel in) { return readEnumSet(in, CasterClass.class, ParcelUtils::readCasterClass); }
    static EnumSet<CastingTime.CastingTimeType> readCastingTimeTypeEnumSet(Parcel in) { return readEnumSet(in, CastingTime.CastingTimeType.class, ParcelUtils::readCastingTimeType); }
    static EnumSet<Duration.DurationType> readDurationTypeEnumSet(Parcel in) { return readEnumSet(in, Duration.DurationType.class, ParcelUtils::readDurationType); }
    static EnumSet<Range.RangeType> readRangeTypeEnumSet(Parcel in) { return readEnumSet(in, Range.RangeType.class, ParcelUtils::readRangeType); }
}
