package dnd.jon.spellbook;

import android.util.Pair;
import android.widget.Filter;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

// Inner class for filtering the list
class SpellFilter extends Filter {

    private static final Object sharedLock = new Object();

    // Filters for SpellFilter
    private static final BiFunction<Spell, Source, Boolean> sourcebookFilter = (spell, sourcebook) -> {
        final boolean filter = spell.getLocations().containsKey(sourcebook);
        return filter;
    };
    private static final BiFunction<Spell, School, Boolean> schoolFilter = (spell, school) -> spell.getSchool() == school;
    private static final BiFunction<Spell, CastingTime.CastingTimeType,Boolean> castingTimeTypeFilter = (spell, castingTimeType) -> spell.getCastingTime().getType() == castingTimeType;
    private static final BiFunction<Spell, Duration.DurationType, Boolean> durationTypeFilter = (spell, durationType) -> spell.getDuration().getType() == durationType;
    private static final BiFunction<Spell, Range.RangeType, Boolean> rangeTypeFilter = (spell, rangeType) -> spell.getRange().getType() == rangeType;

    private final SpellbookViewModel viewModel;

    SpellFilter(SpellbookViewModel viewModel) {
        this.viewModel = viewModel;
    }

    private <T> boolean filterThroughArray(Spell spell, T[] items, BiFunction<Spell,T,Boolean> filter) {
        for (T t : items) {
            if (filter.apply(spell, t)) {
                return false;
            }
        }
        return true;
    }

    private <U extends Unit, V extends Enum<V> & QuantityType, T extends Quantity<V,U>> boolean filterAgainstBounds(Spell spell, Pair<T,T> bounds, Function<Spell,T> quantityGetter) {

        // If the bounds are null, this check should be skipped
        if (bounds == null) { return false; }

        // Get the quantity
        // If it isn't of the spanning type, return false
        final T quantity = quantityGetter.apply(spell);
        if (quantity.isTypeSpanning()) {
            return ( (quantity.compareTo(bounds.first) < 0) || (quantity.compareTo(bounds.second) > 0) );
        } else {
            return false;
        }
    }

    private <U extends Unit, V extends Enum<V> & QuantityType, Q extends Quantity<V,U>> Pair<Q,Q> boundsFromData(int minValue, U minUnit, int maxValue, U maxUnit, V spanningType, QuadFunction<V,Float,U,String,Q> quantityMaker) {
        final Q minQuantity = quantityMaker.apply(spanningType, (float)minValue, minUnit, "");
        final Q maxQuantity = quantityMaker.apply(spanningType, (float)maxValue, maxUnit, "");
        return new Pair<>(minQuantity, maxQuantity);
    }

    private <U extends Unit, V extends Enum<V> & QuantityType, Q extends Quantity<V,U>> Pair<Q,Q> boundsFromGetters(SortFilterStatus sfs,
                                                                                                                    Function<SortFilterStatus,Integer> minValGetter,
                                                                                                                    Function<SortFilterStatus,U> minUnitGetter,
                                                                                                                    Function<SortFilterStatus,Integer> maxValGetter,
                                                                                                                    Function<SortFilterStatus,U> maxUnitGetter,
                                                                                                                    V spanningType,
                                                                                                                    QuadFunction<V,Float,U,String,Q> quantityMaker)
    {
        return boundsFromData(minValGetter.apply(sfs), minUnitGetter.apply(sfs), maxValGetter.apply(sfs), maxUnitGetter.apply(sfs), spanningType, quantityMaker);
    }

    private Pair<CastingTime,CastingTime> castingTimeBounds(SortFilterStatus sfs) {
        return boundsFromGetters(sfs, SortFilterStatus::getMinCastingTimeValue, SortFilterStatus::getMinCastingTimeUnit, SortFilterStatus::getMaxCastingTimeValue, SortFilterStatus::getMaxCastingTimeUnit, CastingTime.CastingTimeType.TIME, CastingTime::new);
    }

    private Pair<Duration,Duration> durationBounds(SortFilterStatus sfs) {
        return boundsFromGetters(sfs, SortFilterStatus::getMinDurationValue, SortFilterStatus::getMinDurationUnit, SortFilterStatus::getMaxDurationValue, SortFilterStatus::getMaxDurationUnit, Duration.DurationType.SPANNING, Duration::new);
    }

    private Pair<Range,Range> rangeBounds(SortFilterStatus sfs) {
        return boundsFromGetters(sfs, SortFilterStatus::getMinRangeValue, SortFilterStatus::getMinRangeUnit, SortFilterStatus::getMaxRangeValue, SortFilterStatus::getMaxRangeUnit, Range.RangeType.RANGED, Range::new);
    }

    private boolean filterItem(Spell spell, SortFilterStatus sortFilterStatus, SpellFilterStatus spellFilterStatus, Source[] visibleSources, CasterClass[] visibleClasses, School[] visibleSchools, CastingTime.CastingTimeType[] visibleCastingTimeTypes, Duration.DurationType[] visibleDurationTypes, Range.RangeType[] visibleRangeTypes, Pair<CastingTime,CastingTime> castingTimeBounds, Pair<Duration,Duration> durationBounds, Pair<Range,Range> rangeBounds, boolean isText, String text) {

        // Get the spell name
        final String spellName = spell.getName().toLowerCase();

        // If we aren't going to filter when searching, and there's search text,
        // we only need to check whether the spell name contains the search text
        if (!sortFilterStatus.getApplyFiltersToSearch() && isText) {
            return !spellName.contains(text);
        }

        // If we aren't going to filter spell lists, and the current filter isn't ALL
        // just check if the spell is on the list
        // (and that it respects any search text)
        if (!sortFilterStatus.getApplyFiltersToLists() && sortFilterStatus.isStatusSet()) {
            boolean hide = spellFilterStatus.hiddenByFilter(spell, sortFilterStatus.getStatusFilterField());
            if (isText) {
                hide = hide || !spellName.contains(text);
            }
            return hide;
        }

        // Run through the various filtering fields

        // Level
        final int spellLevel = spell.getLevel();
        if ( (spellLevel > sortFilterStatus.getMaxSpellLevel()) || (spellLevel < sortFilterStatus.getMinSpellLevel()) ) { return true; }

        // Sourcebooks
        final boolean sourcebookHide = filterThroughArray(spell, visibleSources, sourcebookFilter);
        if (sourcebookHide) { return true; }

        // Classes
        final boolean listsHide = filterThroughArray(spell, visibleClasses, Spell::inSpellList);
        final boolean expandedListsHide = filterThroughArray(spell, visibleClasses, Spell::inExpandedSpellList);
        final boolean classHide = sortFilterStatus.getUseTashasExpandedLists() ? (listsHide && expandedListsHide) : listsHide;
        if (classHide) { return true; }

        // Schools
        final boolean schoolHide = filterThroughArray(spell, visibleSchools, schoolFilter);
        if (schoolHide) { return true; }

        // Casting time types
        final boolean castingTimeTypeHide = filterThroughArray(spell, visibleCastingTimeTypes, castingTimeTypeFilter);
        if (castingTimeTypeHide) { return true; }

        // Duration types
        final boolean durationTypeHide = filterThroughArray(spell, visibleDurationTypes, durationTypeFilter);
        if (durationTypeHide) { return true; }

        // Range types
        final boolean rangeTypeHide = filterThroughArray(spell, visibleRangeTypes, rangeTypeFilter);
        if (rangeTypeHide) { return true; }

        // Casting time bounds
        final boolean castingTimeBoundsHide = filterAgainstBounds(spell, castingTimeBounds, Spell::getCastingTime);
        if (castingTimeBoundsHide) { return true; }

        // Duration bounds
        final boolean durationBoundsHide = filterAgainstBounds(spell, durationBounds, Spell::getDuration);
        if (durationBoundsHide) { return true; }

        // Range bounds
        final boolean rangeBoundsHide = filterAgainstBounds(spell, rangeBounds, Spell::getRange);
        if (rangeBoundsHide) { return true; }

        // The rest of the filtering conditions
        boolean toHide = spellFilterStatus.hiddenByFilter(spell, sortFilterStatus.getStatusFilterField());
        toHide = toHide || !sortFilterStatus.getRitualFilter(spell.getRitual());
        toHide = toHide || !sortFilterStatus.getConcentrationFilter(spell.getConcentration());
        final boolean[] components = spell.getComponents();
        toHide = toHide || !sortFilterStatus.getVerbalFilter(components[0]);
        toHide = toHide || !sortFilterStatus.getSomaticFilter(components[1]);
        toHide = toHide || !sortFilterStatus.getMaterialFilter(components[2]);
        toHide = toHide || !sortFilterStatus.getRoyaltyFilter(components[3]);
        toHide = toHide || (isText && !spellName.contains(text));
        return toHide;
    }

    private void sort(List<Spell> spells) {
        final CharacterProfile profile = viewModel.getProfile();
        final SortFilterStatus sortFilterStatus = profile.getSortFilterStatus();
        final List<Pair<SortField,Boolean>> sortParameters = new ArrayList<Pair<SortField,Boolean>>() {{
            add(new Pair<>(sortFilterStatus.getFirstSortField(), sortFilterStatus.getFirstSortReverse()));
            add(new Pair<>(sortFilterStatus.getSecondSortField(), sortFilterStatus.getSecondSortReverse()));
        }};
        spells.sort(new SpellComparator(viewModel.getContext(), sortParameters));
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        synchronized (sharedLock) {

            final Set<Integer> keptIDs = new HashSet<>();

            // Filter the list of spells
            final String searchText = (constraint != null) ? constraint.toString() : "";
            final FilterResults filterResults = new FilterResults();
            final List<Spell> spellList = viewModel.getAllSpells();
            final List<Spell> filteredSpellList = new ArrayList<>();
            final SortFilterStatus sortFilterStatus = viewModel.getSortFilterStatus();
            final SpellFilterStatus spellFilterStatus = viewModel.getSpellFilterStatus();
            final Source[] visibleSources = sortFilterStatus.getVisibleSources(true).toArray(new Source[0]);
            final CasterClass[] visibleClasses = sortFilterStatus.getVisibleClasses(true).toArray(new CasterClass[0]);
            final School[] visibleSchools = sortFilterStatus.getVisibleSchools(true).toArray(new School[0]);
            final CastingTime.CastingTimeType[] visibleCastingTimeTypes = sortFilterStatus.getVisibleCastingTimeTypes(true).toArray(new CastingTime.CastingTimeType[0]);
            final Duration.DurationType[] visibleDurationTypes = sortFilterStatus.getVisibleDurationTypes(true).toArray(new Duration.DurationType[0]);
            final Range.RangeType[] visibleRangeTypes = sortFilterStatus.getVisibleRangeTypes(true).toArray(new Range.RangeType[0]);
            final boolean isText = !searchText.isEmpty();
            final Pair<CastingTime, CastingTime> castingTimeMinMax = castingTimeBounds(sortFilterStatus);
            final Pair<Duration, Duration> durationMinMax = durationBounds(sortFilterStatus);
            final Pair<Range, Range> rangeMinMax = rangeBounds(sortFilterStatus);
            final boolean hideDuplicates = sortFilterStatus.getHideDuplicateSpells();

            for (Spell spell : spellList) {
                if (!filterItem(spell, sortFilterStatus, spellFilterStatus, visibleSources, visibleClasses, visibleSchools, visibleCastingTimeTypes, visibleDurationTypes, visibleRangeTypes, castingTimeMinMax, durationMinMax, rangeMinMax, isText, searchText)) {
                    filteredSpellList.add(spell);
                    if (hideDuplicates) {
                        keptIDs.add(spell.getID());
                    }
                }
            }

            // I'd rather avoid a second pass, but since linked spells won't necessarily
            // have the same data, we can't generally know whether we need to filter a spell
            // as a duplicate on the first pass
            final boolean searchTextOnly = isText && !sortFilterStatus.getApplyFiltersToSearch();
            final boolean doDuplicatesFilter = hideDuplicates && !searchTextOnly;
            if (doDuplicatesFilter) {
                final Ruleset rulesetToIgnore = sortFilterStatus.getPrefer2024Duplicates() ? Ruleset.RULES_2014 : Ruleset.RULES_2024;
                filteredSpellList.removeIf((spell) -> {
                    if (spell.getRuleset() != rulesetToIgnore) {
                        return false;
                    }
                    final Integer linkedID = Spellbook.linkedSpellID(spell);
                    return linkedID != null && keptIDs.contains(linkedID);
                });
            }

            sort(filteredSpellList);
            filterResults.values = filteredSpellList;
            filterResults.count = filteredSpellList.size();

            return filterResults;

        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void publishResults(CharSequence constraint, FilterResults results) {
        final List<Spell> filteredSpells = (List<Spell>) results.values;
        viewModel.setFilteredSpells(filteredSpells);
    }

}