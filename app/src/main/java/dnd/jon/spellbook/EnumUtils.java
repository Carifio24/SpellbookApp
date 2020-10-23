package dnd.jon.spellbook;

import java.lang.reflect.Array;
import java.util.function.Function;

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

}
