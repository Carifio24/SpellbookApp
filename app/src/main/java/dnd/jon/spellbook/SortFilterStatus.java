package dnd.jon.spellbook;

import org.parceler.Parcel;

@Parcel
public class SortFilterStatus {

    SortField sortField1;
    SortField sortField2;
    boolean sortReverse1;
    boolean sortReverse2;

    int minLevel;
    int maxLevel;

    boolean applyFiltersToLists;
    boolean applyFiltersToSearch;
    boolean useTashasExpanded;

    boolean yesRitual;
    boolean noRitual;
    boolean yesConcentration;
    boolean noConcentration;

    boolean[] yesComponents;
    boolean[] noComponents;

    String[] visibleSourcebookCodes;
    String[] visibleClasses;
    String[] visibleSchools;

    String[] visibleCastingTimeTypes;
    int minCTValue;
    int maxCTValue;
    String minCTUnit;
    String maxCTUnit;

    String[] visibleDurationTypes;
    int minDurationValue;
    int maxDurationValue;
    String minDurationUnit;
    String maxDurationUnit;

    String[] visibleRangeTypes;
    int minRangeValue;
    int maxRangeValue;
    String minRangeUnit;
    String maxRangeUnit;

    SortFilterStatus(CharacterProfile cp) {
        sortField1 = cp.getFirstSortField();
        sortField2 = cp.getSecondSortField();
        sortReverse1 = cp.getFirstSortReverse();
        sortReverse2 = cp.getSecondSortReverse();
        minLevel = cp.getMinSpellLevel();
        maxLevel = cp.getMaxSpellLevel();
        applyFiltersToLists = cp.getApplyFiltersToSpellLists();
        applyFiltersToSearch = cp.getApplyFiltersToSearch();
        useTashasExpanded = cp.getUseTCEExpandedLists();
        yesRitual = cp.getRitualFilter(true);
        noRitual = cp.getRitualFilter(false);
        yesConcentration = cp.getConcentrationFilter(true);
        noConcentration = cp.getConcentrationFilter(false);
        yesComponents = new boolean[]{ cp.getVerbalComponentFilter(true), cp.getSomaticComponentFilter(true), cp.getMaterialComponentFilter(true) };
        noComponents = new boolean[]{ cp.getVerbalComponentFilter(false), cp.getSomaticComponentFilter(false), cp.getMaterialComponentFilter(false) };
        visibleSourcebookCodes = cp.getVisibleValueNames(Sourcebook.class, true, Sourcebook::getInternalCode);
        visibleClasses = cp.getVisibleValueInternalNames(CasterClass.class, true);
        visibleSchools = cp.getVisibleValueInternalNames(School.class, true);
        visibleCastingTimeTypes = cp.getVisibleValueInternalNames(CastingTime.CastingTimeType.class, true);
        minCTValue = cp.getMinValue(CastingTime.CastingTimeType.class);
        maxCTValue = cp.getMaxValue(CastingTime.CastingTimeType.class);

    }
}
