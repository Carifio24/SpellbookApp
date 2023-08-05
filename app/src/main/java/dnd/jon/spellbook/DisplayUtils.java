package dnd.jon.spellbook;

import android.content.Context;
import android.text.TextUtils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class DisplayUtils {

    static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.#");

    ///// General functions
    static <T,R> T getItemFromResourceValue(Context context, T[] items, R resourceValue, ToIntFunction<T> idGetter, BiFunction<Context,Integer,R> valueGetter) {
        if (items == null) { return null; }
        for (T item : items) {
            final int id = idGetter.applyAsInt(item);
            if (resourceValue.equals(valueGetter.apply(context, id))) {
                return item;
            }
        }
        return null;
    }

    static <E extends Enum<E>,R> E getEnumFromResourceValue(Context context, Class<E> enumType, R resourceValue, ToIntFunction<E> enumIDGetter, BiFunction<Context,Integer,R> valueGetter) {
        final E[] enums = enumType.getEnumConstants();
        return getItemFromResourceValue(context, enums, resourceValue, enumIDGetter, valueGetter);
    }

    static <E extends Enum<E>> E getEnumFromResourceID(Integer resourceID, Class<E> enumType, ToIntFunction<E> enumIDGetter) {
        return getEnumFromResourceValue(null, enumType, resourceID, enumIDGetter, (ctx, id) -> id); // Use whatever ID as the 'resource value'
    }

    static <E extends Enum<E> & NameDisplayable> E getEnumFromDisplayName(Context context, Class<E> enumType, String name) {
        return getEnumFromResourceValue(context, enumType, name, E::getDisplayNameID, Context::getString);
    }

    public static <T,R> R getProperty(Context context, T item, Function<T,Integer> resourceIDGetter, BiFunction<Context,Integer,R> resourceGetter) {
        if (item == null) { return null; }
        return resourceGetter.apply(context, resourceIDGetter.apply(item));
    }

    ///// Display names
    static <T> String[] getDisplayNames(Context context, T[] items, BiFunction<Context,T,String> idGetter) {
        final String[] names = Arrays.stream(items).map((t) -> idGetter.apply(context, t)).toArray(String[]::new);
        Arrays.sort(names);
        return names;
    }
    static <E extends Enum<E>> String[] getDisplayNames(Context context, Class<E> enumType, BiFunction<Context,E,String> idGetter) {
        final E[] es = enumType.getEnumConstants();
        if (es == null) { return null; }
        return getDisplayNames(context, es, idGetter);
    }

    static <E extends Enum<E> & NameDisplayable> String[] getDisplayNames(Context context, Class<E> enumType) {
        return getDisplayNames(context, enumType, (ctx, e) -> ctx.getString(e.getDisplayNameID()));
    }

    public static <T extends NameDisplayable> String getDisplayName(Context context, T item) {
        return getProperty(context, item, NameDisplayable::getDisplayNameID, Context::getString);
    }

    ///// For the spell window

    public static String classesString(Context context, Spell spell) {
        if (spell == null) { return ""; }
        final Collection<CasterClass> classes = spell.getClasses();
        final String[] classStrings = new String[classes.size()];
        int i = 0;
        for (CasterClass cc : classes) {
            classStrings[i++] = getDisplayName(context, cc);
        }
        return TextUtils.join(", ", classStrings);
    }

    public static String tashasExpandedClassesString(Context context, Spell spell) {
        if (spell == null) { return ""; }
        final Collection<CasterClass> classes = spell.getTashasExpandedClasses();
        final String[] classStrings = new String[classes.size()];
        int i = 0;
        for (CasterClass cc : classes) {
            classStrings[i++] = getDisplayName(context, cc);
        }
        return TextUtils.join(", ", classStrings);
    }

    public static String locationString(Context context, Spell spell) {
        if (spell == null) { return ""; }
        final Map<Source,Integer> locations = spell.getLocations();
        final String[] locationStrings = new String[locations.size()];
        int i = 0;
        for (Map.Entry<Source,Integer> entry: locations.entrySet()) {
            final String sbString = context.getString(entry.getKey().getCodeID());
            locationStrings[i++] = sbString + " " + entry.getValue();
        }
        return TextUtils.join(", ", locationStrings);
    }

    public static String sourcebooksString(Context context, Spell spell) {
        if (spell == null) { return ""; }
        final Map<Source,Integer> locations = spell.getLocations();
        final String[] locationStrings = new String[locations.size()];
        int i = 0;
        for (Map.Entry<Source,Integer> entry: locations.entrySet()) {
            locationStrings[i++] = context.getString(entry.getKey().getCodeID());
        }
        return TextUtils.join(", ", locationStrings);
    }


    public static int ordinalID(int n) {
        switch (n) {
            case 1:
                return R.string.st_level;
            case 2:
                return R.string.nd_level;
            case 3:
                return R.string.rd_level;
            default:
                return R.string.th_level;
        }
    }

    public static String ordinalString(Context context, int n) { return context.getString(ordinalID(n)); }

    public static String boolString(Context context, boolean b, boolean capitalized) {
        int id;
        if (capitalized) {
            id = b ? R.string.yes : R.string.no;
        } else {
            id = b ? R.string.yes_lower : R.string.no_lower;
        }
        return context.getString(id);
    }

    public static String boolString(Context context, boolean b) { return boolString(context, b, false); }

    ///// Units

    static <U extends Enum<U> & Unit> U unitFromString(Context context, Class<U> unitType, String s) {
        //System.out.println("Unit string is " + s);
        //final U unit = SpellbookUtils.coalesce(getEnumFromResourceValue(context, unitType, s, Unit::getSingularNameID, Context::getString), getEnumFromResourceValue(context, unitType, s, Unit::getPluralNameID, Context::getString));
        //if (unit == null) { System.out.println("NULL HERE"); }
        return SpellbookUtils.coalesce(getEnumFromResourceValue(context, unitType, s, Unit::getSingularNameID, Context::getString), getEnumFromResourceValue(context, unitType, s, Unit::getPluralNameID, Context::getString));
    }

    public static String string(Context context, Duration duration) {
        if (duration == null) { return ""; }
        return duration.makeString((t) -> getDisplayName(context, t), (u) -> context.getString(u.getSingularNameID()), (u) -> context.getString(u.getPluralNameID()));
    }
    public static String string(Context context, CastingTime castingTime) {
        if (castingTime == null) { return ""; }
        return castingTime.makeString((t) -> getDisplayName(context, t), (u) -> context.getString(u.getSingularNameID()), (u) -> context.getString(u.getPluralNameID()));
    }
    public static String string(Context context, Range range) {
        if (range == null) { return ""; }
        return range.makeString((t) -> getDisplayName(context, t), (u) -> context.getString(u.getSingularNameID()), (u) -> context.getString(u.getPluralNameID()), context.getString(R.string.foot_radius));
    }

    static String getSingularName(Context context, Unit unit) {
        return getProperty(context, unit, Unit::getSingularNameID, Context::getString);
    }

    static String getPluralName(Context context, Unit unit) {
        return getProperty(context, unit, Unit::getPluralNameID, Context::getString);
    }


    ///// Quantities

    static Duration durationFromString(Context context, String s) {
        return Duration.fromString(s, (t) -> getDisplayName(context, t), context.getString(R.string.concentration_prefix), (us) -> unitFromString(context, TimeUnit.class, us));
    }

    static CastingTime castingTimeFromString(Context context, String s) {
        return CastingTime.fromString(s, (ct) -> getProperty(context, ct, CastingTime.CastingTimeType::getParseNameID, Context::getString), (us) -> unitFromString(context, TimeUnit.class, us));
    }

    static Range rangeFromString(Context context, String s) {
        return Range.fromString(s, (t) -> getDisplayName(context, t), (us) -> unitFromString(context, LengthUnit.class, us));
    }

    // Spell prompt text
    public static String locationPrompt(Context context, int nLocations) {
        return context.getString(nLocations == 1 ? R.string.location : R.string.location);
    }
    public static String concentrationPrompt(Context context) { return context.getString(R.string.concentration); }
    public static String castingTimePrompt(Context context) { return context.getString(R.string.casting_time); }
    public static String rangePrompt(Context context) { return context.getString(R.string.range); }
    public static String componentsPrompt(Context context) { return context.getString(R.string.components); }
    public static String materialsPrompt(Context context) { return context.getString(R.string.materials); }
    public static String royaltyPrompt(Context context) { return context.getString(R.string.royalty); }
    public static String durationPrompt(Context context) { return context.getString(R.string.duration); }
    public static String classesPrompt(Context context) { return context.getString(R.string.classes); }
    public static String tceExpandedClassesPrompt(Context context) { return context.getString(R.string.tce_expanded_classes); }
    public static String descriptionPrompt(Context context) { return context.getString(R.string.description); }
    public static String higherLevelsPrompt(Context context) { return context.getString(R.string.higher_level); }

    ///// Sources
    static String getDisplayName(Source source, Context context) {
        if (source.isCreated()) {
            return source.getDisplayName();
        } else {
            return getProperty(context, source, Source::getDisplayNameID, Context::getString);
        }
    }

    static String getCode(Source source, Context context) {
        if (source.isCreated()) {
            return source.getCode();
        } else {
            return getProperty(context, source, Source::getCodeID, Context::getString);
        }
    }
}
