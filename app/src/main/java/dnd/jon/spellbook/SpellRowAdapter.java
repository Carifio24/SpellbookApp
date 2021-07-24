package dnd.jon.spellbook;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import android.util.Pair;

import org.javatuples.Sextet;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

import dnd.jon.spellbook.databinding.SpellRowBinding;

public class SpellRowAdapter extends RecyclerView.Adapter<SpellRowAdapter.SpellRowHolder> implements Filterable {

    // This is for synchronization between various processes
    // which can otherwise fire simultaneously
    // We don't want multiple threads mutating the spell list at the same time
    private static final Object sharedLock = new Object();

    // Filters, etc.
//    private static final HashMap<Class<? extends Enum<?>>, Pair<BiFunction<Spell,Object,Boolean>, Class<? extends Quantity>>> enumData = new HashMap<Class<? extends Enum<?>>, Pair<BiFunction<Spell,Object,Boolean>, Class<? extends Quantity>>>() {{
//       put(Sourcebook.class, new Pair<>((spell, sourcebook) -> (spell.getSourcebook() == sourcebook), null));
//       put(CasterClass.class, new Pair<>((spell, caster) -> spell.usableByClass( (CasterClass) caster), null));
//       put(School.class, new Pair<>((spell, school) -> (spell.getSchool() == school), null));
//       put(CastingTime.CastingTimeType.class, new Pair<>((spell, castingTimeType) -> (spell.getCastingTime().getType() == castingTimeType), CastingTime.class));
//       put(Duration.DurationType.class, new Pair<>((spell, durationType) -> (spell.getDuration().getType() == durationType), Duration.class));
//       put(Range.RangeType.class, new Pair<>((spell, rangeType) -> (spell.getRange().getType() == rangeType), Range.class));
//    }};

    // Filters for SpellFilter
    private static final BiFunction<Spell, Sourcebook, Boolean> sourcebookFilter = (spell, sourcebook) -> spell.getLocations().containsKey(sourcebook);
    private static final BiFunction<Spell, School, Boolean> schoolFilter = (spell, school) -> spell.getSchool() == school;
    private static final BiFunction<Spell, CastingTime.CastingTimeType,Boolean> castingTimeTypeFilter = (spell, castingTimeType) -> spell.getCastingTime().getType() == castingTimeType;
    private static final BiFunction<Spell, Duration.DurationType, Boolean> durationTypeFilter = (spell, durationType) -> spell.getDuration().getType() == durationType;
    private static final BiFunction<Spell, Range.RangeType, Boolean> rangeTypeFilter = (spell, rangeType) -> spell.getRange().getType() == rangeType;

    // Inner class for holding the spell row views
    public class SpellRowHolder extends RecyclerView.ViewHolder {

        private Spell spell = null;
        private final SpellRowBinding binding;
        private final SpellTableFragment.SpellTableHandler handler;
        private Runnable postToggleAction = () -> {};

        // For convenience, we construct the adapter directly from the SpellRowBinding generated from the XML
        public SpellRowHolder(SpellRowBinding binding, SpellTableFragment.SpellTableHandler handler) {
            super(binding.getRoot());
            this.binding = binding;
            this.handler = handler;
            itemView.setTag(this);
            itemView.setOnClickListener(listener);
            //itemView.setOnLongClickListener(longListener);
        }

        public void bind(Spell s) {
            spell = s;
            binding.setSpell(spell);
            binding.executePendingBindings();

            //Set the buttons to show the appropriate images
            if (spell != null) {
                final SpellFilterStatus spellFilterStatus = handler.getSpellFilterStatus();
                binding.spellRowFavoriteButton.set(spellFilterStatus.isFavorite(spell));
                binding.spellRowPreparedButton.set(spellFilterStatus.isPrepared(spell));
                binding.spellRowKnownButton.set(spellFilterStatus.isKnown(spell));
            }


            // Set button callbacks
            postToggleAction = handler::saveSpellFilterStatus;
            binding.spellRowFavoriteButton.setOnClickListener( (v) -> { handler.getSpellFilterStatus().toggleFavorite(spell); postToggleAction.run(); } );
            binding.spellRowPreparedButton.setOnClickListener( (v) -> { handler.getSpellFilterStatus().togglePrepared(spell); postToggleAction.run(); } );
            binding.spellRowKnownButton.setOnClickListener( (v) -> { handler.getSpellFilterStatus().toggleKnown(spell); postToggleAction.run(); } );

        }

        public Spell getSpell() { return spell; }
    }

    // Inner class for filtering the list
    private class SpellFilter extends Filter {

        private final SpellTableFragment.SpellTableHandler handler;

        SpellFilter(SpellTableFragment.SpellTableHandler handler) {
            this.handler = handler;
        }

        private <E extends Enum<E>> boolean filterThroughArray(Spell spell, E[] enums, BiFunction<Spell,E,Boolean> filter) {
            for (E e : enums) {
                if (filter.apply(spell, e)) {
                    return false;
                }
            }
            return true;
        }

        private <T extends Quantity> boolean filterAgainstBounds(Spell spell, Pair<T,T> bounds, Function<Spell,T> quantityGetter) {

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

        private <T extends Quantity> Pair<T,T> boundsFromData(Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> data, Class<T> quantity, Class<? extends Unit> unitType, QuantityType spanningType) {
            try {
                final Class<? extends QuantityType> quantityType = spanningType.getClass();
                final Constructor constructor = quantity.getDeclaredConstructor(quantityType, float.class, unitType, String.class);
                final T min = (T) constructor.newInstance(spanningType, data.getValue4(), data.getValue2(), "");
                final T max = (T) constructor.newInstance(spanningType, data.getValue5(), data.getValue3(), "");
                return new Pair<>(min, max);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private boolean filterItem(Spell spell, SortFilterStatus sortFilterStatus, SpellFilterStatus spellFilterStatus, Sourcebook[] visibleSourcebooks, CasterClass[] visibleClasses, School[] visibleSchools, CastingTime.CastingTimeType[] visibleCastingTimeTypes, Duration.DurationType[] visibleDurationTypes, Range.RangeType[] visibleRangeTypes, Pair<CastingTime,CastingTime> castingTimeBounds, Pair<Duration,Duration> durationBounds, Pair<Range,Range> rangeBounds, boolean isText, String text) {

            // Get the spell name
            final String spellName = spell.getName().toLowerCase();

            //System.out.println("Spell name is " + spellName);
            //System.out.println("Casting time is " + DisplayUtils.string(main, spell.getCastingTime()));

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
            final boolean sourcebookHide = filterThroughArray(spell, visibleSourcebooks, sourcebookFilter);
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
            toHide = toHide || (isText && !spellName.contains(text));
            return toHide;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            synchronized (sharedLock) {
                // Filter the list of spells
                final String searchText = (constraint != null) ? constraint.toString() : "";
                final FilterResults filterResults = new FilterResults();
                filteredSpellList = new ArrayList<>();
                final SortFilterStatus sortFilterStatus = handler.getSortFilterStatus();
                final SpellFilterStatus spellFilterStatus = handler.getSpellFilterStatus();
                final Sourcebook[] visibleSourcebooks = sortFilterStatus.getVisibleSourcebooks(true);
                final CasterClass[] visibleClasses = sortFilterStatus.getVisibleClasses(true);
                final School[] visibleSchools = sortFilterStatus.getVisibleSchools(true);
                final CastingTime.CastingTimeType[] visibleCastingTimeTypes = sortFilterStatus.getVisibleCastingTimeTypes(true);
                final Duration.DurationType[] visibleDurationTypes = sortFilterStatus.getVisibleDurationTypes(true);
                final Range.RangeType[] visibleRangeTypes = sortFilterStatus.getVisibleRangeTypes(true);
                final boolean isText = !searchText.isEmpty();
                final Pair<CastingTime,CastingTime> castingTimeMinMax = castingTimeBounds(sortFilterStatus);
                final Pair<Duration, Duration> durationMinMax = durationBounds(sortFilterStatus);
                final Pair<Range, Range> rangeMinMax = rangeBounds(sortFilterStatus);
                for (Spell s : spellList) {
                    if (!filterItem(s, sortFilterStatus, spellFilterStatus, visibleSourcebooks, visibleClasses, visibleSchools, visibleCastingTimeTypes, visibleDurationTypes, visibleRangeTypes, castingTimeMinMax, durationMinMax, rangeMinMax, isText, searchText)) {
                        filteredSpellList.add(s);
                    }
                }
                filterResults.values = filteredSpellList;
                filterResults.count = filteredSpellList.size();

                return filterResults;
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            notifyDataSetChanged();
        }

    }

    // Member values
    // References to the RecyclerView and the MainActivity
    // Also the list of spells, and the click listeners
    private final List<Spell> spellList;
    private List<Spell> filteredSpellList;
    private final SpellTableFragment.SpellTableHandler handler;
    private final Context context;
    private final View.OnClickListener listener;
//    private final View.OnLongClickListener longListener = (View view) -> {
//        final SpellRowHolder srh = (SpellRowHolder) view.getTag();
//        final Spell spell = srh.getSpell();
//        main.openSpellPopup(view, spell);
//        return true;
//    };


    // Constructor from the list of spells
    SpellRowAdapter(Context context, List<Spell> spells, SpellTableFragment.SpellTableHandler handler) {
        this.spellList = spells;
        this.filteredSpellList = spells;
        this.handler = handler;
        this.context = context;
        this.listener = (View view) -> {
            final SpellRowHolder srh = (SpellRowHolder) view.getTag();
            final Spell spell = srh.getSpell();
            final int pos = srh.getLayoutPosition();
            this.handler.handleSpellSelected(spell, pos);
        };
    }

    // Filterable methods
    public Filter getFilter() {
        synchronized (sharedLock) {
            return new SpellFilter(handler);
        }
    }

    // For use from MainActivity
    void filter(CharSequence query) {
        synchronized (sharedLock) {
            getFilter().filter(query);
            notifyDataSetChanged();
        }
    }

    void filter() {
        synchronized (sharedLock) {
            getFilter().filter(handler.getSearchQuery());
            notifyDataSetChanged();
        }
    }

    private void sortFilter(List<Pair<SortField,Boolean>> sortParameters, CharSequence query) {
        synchronized (sharedLock) {
            spellList.sort(new SpellComparator(context, sortParameters));
            filter(query);
        }
    }

    private void sort(List<Pair<SortField,Boolean>> sortParameters) {
        synchronized (sharedLock) {
            spellList.sort(new SpellComparator(context, sortParameters));
        }
    }

    void singleSort() {
        final SortFilterStatus sortFilterStatus = handler.getSortFilterStatus();
        final SortField sf = sortFilterStatus.getFirstSortField();
        final boolean reverse = sortFilterStatus.getFirstSortReverse();
        final List<Pair<SortField,Boolean>> sortParameters = new ArrayList<Pair<SortField,Boolean>>() {{
            add(new Pair<>(sf, reverse));
        }};
        sortFilter(sortParameters, handler.getSearchQuery());
    }

    void doubleSort() {
        final SortFilterStatus sortFilterStatus = handler.getSortFilterStatus();
        final SortField sf1 = sortFilterStatus.getFirstSortField();
        final SortField sf2 = sortFilterStatus.getSecondSortField();
        final boolean reverse1 = sortFilterStatus.getFirstSortReverse();
        final boolean reverse2 = sortFilterStatus.getSecondSortReverse();
        final List<Pair<SortField,Boolean>> sortParameters = new ArrayList<Pair<SortField,Boolean>>() {{
            add(new Pair<>(sf1, reverse1));
            add(new Pair<>(sf2, reverse2));
        }};

        sortFilter(sortParameters, handler.getSearchQuery());
    }

    // ViewHolder methods
    @NonNull
    public SpellRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final SpellRowBinding binding = SpellRowBinding.inflate(inflater, parent, false);
        return new SpellRowHolder(binding, handler);
    }

    public void onBindViewHolder(@NonNull SpellRowHolder holder, int position) {

        // Do nothing if the index is out of bounds
        // This shouldn't happen, but it's better than a crash
        if ( (position >= filteredSpellList.size()) || (position < 0) ) { return; }

        // Get the appropriate spell and bind it to the holder
        final Spell spell = filteredSpellList.get(position);
        holder.bind(spell);
    }

    // The number of spells to be displayed
    public int getItemCount() {
        synchronized (sharedLock) {
            return filteredSpellList.size();
        }
    }

    int getSpellIndex(Spell spell) {
        synchronized (sharedLock) {
            return filteredSpellList.indexOf(spell);
        }
    }

    void updateSpell(Spell spell) {
        final int index = getSpellIndex(spell);
        if (index >= 0) {
            notifyItemChanged(index);
        }
    }
}
