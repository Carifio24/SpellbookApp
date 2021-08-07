package dnd.jon.spellbook;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import dnd.jon.spellbook.databinding.SpellRowBinding;

public class SpellAdapter extends RecyclerView.Adapter<SpellAdapter.SpellRowHolder> implements Filterable {

    // This is for synchronization between various processes
    // which can otherwise fire simultaneously
    // We don't want multiple threads mutating the spell list at the same time
    private static final Object sharedLock = new Object();

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
        private final SpellbookViewModel viewModel;
        private Runnable postToggleAction = () -> {};

        // For convenience, we construct the adapter directly from the SpellRowBinding generated from the XML
        public SpellRowHolder(SpellRowBinding binding, SpellbookViewModel viewModel) {
            super(binding.getRoot());
            this.binding = binding;
            this.viewModel = viewModel;
            itemView.setTag(this);
            itemView.setOnClickListener(listener);
            //itemView.setOnLongClickListener(longListener);
        }

        private void toggleAction(Spell spell, BiConsumer<SpellFilterStatus,Spell> toggler) {
            final SpellFilterStatus status = viewModel.getCurrentSpellFilterStatus().getValue();
            if (status == null) { return; }
            toggler.accept(status, spell);
            postToggleAction.run();
        }

        public void bind(Spell s) {
            spell = s;
            binding.setSpell(spell);
            binding.executePendingBindings();

            //Set the buttons to show the appropriate images
            if (spell != null) {
                final SpellFilterStatus spellFilterStatus = viewModel.getCurrentSpellFilterStatus().getValue();
                if (spellFilterStatus != null) {
                    binding.spellRowFavoriteButton.set(spellFilterStatus.isFavorite(spell));
                    binding.spellRowPreparedButton.set(spellFilterStatus.isPrepared(spell));
                    binding.spellRowKnownButton.set(spellFilterStatus.isKnown(spell));
                }
            }


            // Set button callbacks
            postToggleAction = viewModel::saveSpellFilterStatus;
            binding.spellRowFavoriteButton.setOnClickListener((v) -> toggleAction(spell, SpellFilterStatus::toggleFavorite));
            binding.spellRowPreparedButton.setOnClickListener((v) -> toggleAction(spell, SpellFilterStatus::togglePrepared));
            binding.spellRowKnownButton.setOnClickListener((v) -> toggleAction(spell, SpellFilterStatus::toggleKnown));

        }

        public Spell getSpell() { return spell; }
    }

    // Inner class for filtering the list
    private class SpellFilter extends Filter {

        private final SpellbookViewModel viewModel;

        SpellFilter(SpellbookViewModel viewModel) {
            this.viewModel = viewModel;
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
                final SortFilterStatus sortFilterStatus = viewModel.getCurrentSortFilterStatus().getValue();
                final SpellFilterStatus spellFilterStatus = viewModel.getCurrentSpellFilterStatus().getValue();
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
    private final View.OnClickListener listener;
    private final SpellbookViewModel viewModel;
    private final Context context;
//    private final View.OnLongClickListener longListener = (View view) -> {
//        final SpellRowHolder srh = (SpellRowHolder) view.getTag();
//        final Spell spell = srh.getSpell();
//        main.openSpellPopup(view, spell);
//        return true;
//    };


    // Constructor from the list of spells
    SpellAdapter(Context context, SpellbookViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
        this.spellList = viewModel.getAllSpells();
        this.filteredSpellList = spellList;
        this.listener = (View view) -> {
            final SpellRowHolder srh = (SpellRowHolder) view.getTag();
            final Spell spell = srh.getSpell();
            //final int pos = srh.getLayoutPosition();
            this.viewModel.setCurrentSpell(spell);
        };
    }

    // Filterable methods
    public Filter getFilter() {
        synchronized (sharedLock) {
            return new SpellFilter(viewModel);
        }
    }

    // For use from MainActivity
    void filter(CharSequence query) {
        synchronized (sharedLock) {
            getFilter().filter(query);
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
        final SortFilterStatus sortFilterStatus = viewModel.getCurrentSortFilterStatus().getValue();
        if (sortFilterStatus == null) { return; }
        final SortField sf = sortFilterStatus.getFirstSortField();
        final boolean reverse = sortFilterStatus.getFirstSortReverse();
        final List<Pair<SortField,Boolean>> sortParameters = new ArrayList<Pair<SortField,Boolean>>() {{
            add(new Pair<>(sf, reverse));
        }};
        sortFilter(sortParameters, viewModel.getSearchQuery().getValue());
    }

    void doubleSort() {
        final SortFilterStatus sortFilterStatus = viewModel.getCurrentSortFilterStatus().getValue();
        final SortField sf1 = sortFilterStatus.getFirstSortField();
        final SortField sf2 = sortFilterStatus.getSecondSortField();
        final boolean reverse1 = sortFilterStatus.getFirstSortReverse();
        final boolean reverse2 = sortFilterStatus.getSecondSortReverse();
        final List<Pair<SortField,Boolean>> sortParameters = new ArrayList<Pair<SortField,Boolean>>() {{
            add(new Pair<>(sf1, reverse1));
            add(new Pair<>(sf2, reverse2));
        }};

        sortFilter(sortParameters, viewModel.getSearchQuery().getValue());
    }

    // ViewHolder methods
    @NonNull
    public SpellRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final SpellRowBinding binding = SpellRowBinding.inflate(inflater, parent, false);
        return new SpellRowHolder(binding, viewModel);
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
