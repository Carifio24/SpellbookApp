package dnd.jon.spellbook;

import android.content.Context;
import android.text.TextUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class DisplayUtils {

    static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.#");

    ///// General functions
    static <E extends Enum<E>,T> E getEnumFromResourceValue(Context context, Class<E> enumType, T resourceValue, ToIntFunction<E> enumIDGetter, BiFunction<Context,Integer,T> valueGetter) {
        final E[] es = enumType.getEnumConstants();
        if (es == null) { return null; }
        for (E e : es) {
            final int id = enumIDGetter.applyAsInt(e);
            if (resourceValue.equals(valueGetter.apply(context, id))) {
                return e;
            }
        }
        return null;
    }

    static <E extends Enum<E>> E getEnumFromResourceID(Integer resourceID, Class<E> enumType, ToIntFunction<E> enumIDGetter) {
        return getEnumFromResourceValue(null, enumType, resourceID, enumIDGetter, (ctx, id) -> id); // Use whatever ID as the 'resource value'
    }

    static <E extends Enum<E> & NameDisplayable> E getEnumFromDisplayName(Context context, Class<E> enumType, String name) {
        return getEnumFromResourceValue(context, enumType, name, E::getDisplayNameID, Context::getString);
    }

    public static <T,R> R getProperty(Context context, T item, Function<T,Integer> resourceIDGetter, BiFunction<Context,Integer,R> resourceGetter) {
        if (item == null) { return null; }
//        if (item.getClass().equals(CastingTime.CastingTimeType.class)) {
//            System.out.println("id is " + resourceIDGetter.apply(item));
//            System.out.println(context.getString(resourceIDGetter.apply(item)));
//            System.out.println(dnd.jon.spellbook.R.string.one_action);
//            System.out.println(context.getString(dnd.jon.spellbook.R.string.action));
//        }
        return resourceGetter.apply(context, resourceIDGetter.apply(item));
    }

    ///// Display names

    static <E extends Enum<E>> String[] getDisplayNames(Context context, Class<E> enumType, BiFunction<Context,E,String> idGetter) {
        final E[] es = enumType.getEnumConstants();
        if (es == null) { return null; }
        final String[] names = Arrays.stream(es).map((e) -> idGetter.apply(context, e)).toArray(String[]::new);
        Arrays.sort(names);
        return names;
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
        final Map<Sourcebook,Integer> locations = spell.getLocations();
        final String[] locationStrings = new String[locations.size()];
        int i = 0;
        for (Map.Entry<Sourcebook,Integer> entry: locations.entrySet()) {
            final String sbString = context.getString(entry.getKey().getCodeID());
            locationStrings[i++] = sbString + " " + entry.getValue();
        }
        return TextUtils.join(", ", locationStrings);
    }

    public static String sourcebooksString(Context context, Spell spell) {
        if (spell == null) { return ""; }
        final Map<Sourcebook,Integer> locations = spell.getLocations();
        final String[] locationStrings = new String[locations.size()];
        int i = 0;
        for (Map.Entry<Sourcebook,Integer> entry: locations.entrySet()) {
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
        final U unit = SpellbookUtils.coalesce(getEnumFromResourceValue(context, unitType, s, Unit::getSingularNameID, Context::getString), getEnumFromResourceValue(context, unitType, s, Unit::getPluralNameID, Context::getString));
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

}
