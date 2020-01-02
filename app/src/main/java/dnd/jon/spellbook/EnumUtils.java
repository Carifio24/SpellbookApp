package dnd.jon.spellbook;

class EnumUtils {

    static <E extends Enum<E>> String[] namesArray(Class<E> enumType) {
        // Get the values of the enum
        E[] enumValues = enumType.getEnumConstants();

        // If this isn't an enum class, return an empty array
        if (enumValues == null) { return new String[0]; }

        // Loop over the enum values and populate the names array
        String[] enumNames = new String[enumValues.length];
        int i = 0;
        for (E value : enumValues) {
            enumNames[i++] = value.name();
        }
        return enumNames;
    }

}
