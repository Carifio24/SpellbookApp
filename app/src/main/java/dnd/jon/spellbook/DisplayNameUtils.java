package dnd.jon.spellbook;

import android.content.Context;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

public class DisplayNameUtils {

    static <E extends Enum<E>> E getEnumFromResourceID(int resourceID, Context context, Class<E> enumType, ToIntFunction<E> enumIDGetter, BiFunction<Context,Integer,E> resourceGetter) {
        final E resource = resourceGetter.apply(context, resourceID);
        final E[] es = enumType.getEnumConstants();
        if (es == null) { return null; }
        for (E e : es) {
            final int id = enumIDGetter.applyAsInt(e);
            if (resource.equals(resourceGetter.apply(context, id))) {
                return e;
            }
        }
        return null;
    }

//    static <E extends Enum<E> & NameDisplayable> String[] displayNames( Context context, Class<E> enumType) {
//        return valuesArray(enumType, String.class, (e) -> context.getString(e.getDisplayNameID()));
//    }

    static <E extends Enum<E>> String[] getDisplayNames( Context context, Class<E> enumType, BiFunction<Context,E,String> idGetter) {
        final E[] es = enumType.getEnumConstants();
        if (es == null) { return null; }
        final String[] names = Arrays.stream(es).map((e) -> idGetter.apply(context, e)).toArray(String[]::new);
        Arrays.sort(names);
        return names;
    }

    static <E extends Enum<E> & NameDisplayable> String[] getDisplayNames(Context context, Class<E> enumType) {
        return getDisplayNames(context, enumType, (ctxt, e) -> ctxt.getString(e.getDisplayNameID()));
    }

    public static <T extends NameDisplayable> String getDisplayName(Context context, T item) {
        return context.getString(item.getDisplayNameID());
    }

    public static String classesString(Context context, Spell spell) {
        final Collection<CasterClass> classes = spell.getClasses();
        final String[] classStrings = new String[classes.size()];
        int i = 0;
        for (CasterClass cc : classes) {
            classStrings[i++] = getDisplayName(context, cc);
        }
        return TextUtils.join(", ", classStrings);
    }

    public static String sourcebookCode(Context context, Spell spell) {
        return context.getString(spell.getSourcebook().getCodeID());
    }

    public static String locationString(Context context, Spell spell) {
        final String code = sourcebookCode(context, spell);
        return code + " " + spell.getPage();
    }

}
