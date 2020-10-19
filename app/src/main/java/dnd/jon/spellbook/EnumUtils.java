package dnd.jon.spellbook;

import android.content.Context;

import java.lang.reflect.Array;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

class EnumUtils {

    static <E extends Enum<E>, T> T[] valuesArray(Class<E> enumType, Class<T> valueType, Function<E,T> property) {

        // Get the values of the enum
        final E[] enumValues = enumType.getEnumConstants();

        // If this isn't an enum class, return an empty array
        if (enumValues == null) {
            @SuppressWarnings(value = "unchecked")
            final T[] arr = (T[]) Array.newInstance(valueType, 0);
            return arr;
        }

        // Loop over the enum values and populate the names array
        @SuppressWarnings(value = "unchecked")
        final T[] arr = (T[]) Array.newInstance(valueType, enumValues.length);
        int i = 0;
        for (E value : enumValues) {
            arr[i++] = property.apply(value);
        }
        return arr;
    }

    static <E extends Enum<E> & NameDisplayable> String[] displayNames( Context context, Class<E> enumType) {
        return valuesArray(enumType, String.class, (e) -> context.getString(e.getDisplayNameID()));
    }

    static <U extends Enum<U> & Unit> String[] unitPluralNames(Class<U> unitType) {
        return valuesArray(unitType, String.class, U::pluralName);
    }

    static <U extends Enum<U> & Unit> String[] unitSingularNames(Class<U> unitType) {
        return valuesArray(unitType, String.class, U::singularName);
    }

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


}
