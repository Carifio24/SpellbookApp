package dnd.jon.spellbook;

class Spellbook {

    //static final String[] casterNames = EnumUtils.displayNames(CasterClass.class);
    static final String[] sortFieldNames = EnumUtils.displayNames(SortField.class);
    static final String[] schoolNames = EnumUtils.displayNames(School.class);
    //static final String[] subclassNames = EnumUtils.valuesArray(SubClass.class, String.class, SubClass::getParseName);
    //static final String[] sourcebookCodes = EnumUtils.valuesArray(Sourcebook.class, String.class, Sourcebook::getCode);

    static final int MIN_SPELL_LEVEL = 0;
    static final int MAX_SPELL_LEVEL = 9;

}
