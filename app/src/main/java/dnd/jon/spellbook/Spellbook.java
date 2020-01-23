package dnd.jon.spellbook;

class Spellbook {

    static final String[] casterNames = EnumUtils.valuesArray(CasterClass.class, String.class, CasterClass::getDisplayName);
    static final String[] sortFieldNames = EnumUtils.valuesArray(SortField.class, String.class, SortField::getDisplayName);
    //static final String[] schoolNames = EnumUtils.valuesArray(School.class, String.class, School::getDisplayName);
    //static final String[] subclassNames = EnumUtils.valuesArray(SubClass.class, String.class, SubClass::getDisplayName);
    //static final String[] sourcebookCodes = EnumUtils.valuesArray(Sourcebook.class, String.class, Sourcebook::getCode);

    static final int MIN_SPELL_LEVEL = 0;
    static final int MAX_SPELL_LEVEL = 9;

}
