package dnd.jon.spellbook;

import android.content.Context;

import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

public class ResourceUtils {

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
}
