package dnd.jon.spellbook;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;

import dnd.jon.spellbook.databinding.SpellRowBinding;

public class SpellRowAdapter extends RecyclerView.Adapter<SpellRowAdapter.SpellRowHolder> implements Filterable {

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
    private static final BiFunction<Spell, Sourcebook, Boolean> sourcebookFilter = (spell, sourcebook) -> spell.getSourcebook() == sourcebook;
    private static final BiFunction<Spell, School, Boolean> schoolFilter = (spell, school) -> spell.getSchool() == school;
    private static final BiFunction<Spell, CastingTime.CastingTimeType,Boolean> castingTimeTypeFilter = (spell, castingTimeType) -> spell.getCastingTime().getType() == castingTimeType;
    private static final BiFunction<Spell, Duration.DurationType, Boolean> durationTypeFilter = (spell, durationType) -> spell.getDuration().getType() == durationType;
    private static final BiFunction<Spell, Range.RangeType, Boolean> rangeTypeFilter = (spell, rangeType) -> spell.getRange().getType() == rangeType;

    // Inner class for holding the spell row views
    public class SpellRowHolder extends RecyclerView.ViewHolder {

        private Spell spell = null;
        private final SpellRowBinding binding;
        private final MainActivity main;
        private Runnable postToggleAction = () -> {};

        // For convenience, we construct the adapter directly from the SpellRowBinding generated from the XML
        public SpellRowHolder(SpellRowBinding b) {
            super(b.getRoot());
            binding = b;
            main = (MainActivity) b.getRoot().getContext();
            itemView.setTag(this);
            itemView.setOnClickListener(listener);
            //itemView.setOnLongClickListener(longListener);
        }

        public void bind(Spell s) {
            spell = s;
            binding.setSpell(spell);
            binding.executePendingBindings();

            //Set the buttons to show the appropriate images
            if (main != null && main.getCharacterProfile() != null && spell != null) {
                binding.spellRowFavoriteButton.set(main.getCharacterProfile().isFavorite(spell));
                binding.spellRowPreparedButton.set(main.getCharacterProfile().isPrepared(spell));
                binding.spellRowKnownButton.set(main.getCharacterProfile().isKnown(spell));
            }


            // Set button callbacks
            postToggleAction = () -> {
                main.saveCharacterProfile();
                main.updateSpellWindow(spell, main.getCharacterProfile().isFavorite(spell), main.getCharacterProfile().isPrepared(spell), main.getCharacterProfile().isKnown(spell));
            };
            binding.spellRowFavoriteButton.setOnClickListener( (v) -> { main.getCharacterProfile().toggleFavorite(spell); postToggleAction.run(); } );
            binding.spellRowPreparedButton.setOnClickListener( (v) -> { main.getCharacterProfile().togglePrepared(spell); postToggleAction.run(); } );
            binding.spellRowKnownButton.setOnClickListener( (v) -> { main.getCharacterProfile().toggleKnown(spell); postToggleAction.run(); } );

        }

        public Spell getSpell() { return spell; }
    }

    // Inner class for filtering the list
    private class SpellFilter extends Filter {

        private final CharacterProfile cp;

        SpellFilter(CharacterProfile cp) {
            this.cp = cp;
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

        private <T extends Quantity> Pair<T,T> boundsFromData(Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> data, Class<T> quantity, Class<? extends Unit> unitType, QuantityType spanningType) {
            try {
                final Class<? extends QuantityType> quantityType = spanningType.getClass();
                final Constructor constructor = quantity.getDeclaredConstructor(quantityType, int.class, unitType, String.class);
                final T min = (T) constructor.newInstance(spanningType, data.getValue4(), data.getValue2(), "");
                final T max = (T) constructor.newInstance(spanningType, data.getValue5(), data.getValue3(), "");
                return new Pair<>(min, max);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private boolean filterItem(Spell s, Sourcebook[] visibleSourcebooks, CasterClass[] visibleClasses, School[] visibleSchools, CastingTime.CastingTimeType[] visibleCastingTimeTypes, Duration.DurationType[] visibleDurationTypes, Range.RangeType[] visibleRangeTypes, Pair<CastingTime,CastingTime> castingTimeBounds, Pair<Duration,Duration> durationBounds, Pair<Range,Range> rangeBounds, boolean isText, String text) {

            // Get the spell name
            final String spellName = s.getName().toLowerCase();

            // Run through the various filtering fields

            // Level
            final int spellLevel = s.getLevel();
            if ( (spellLevel > cp.getMaxSpellLevel()) || (spellLevel < cp.getMinSpellLevel()) ) { return true; }

            // Sourcebooks
            final boolean sourcebookHide = filterThroughArray(s, visibleSourcebooks, sourcebookFilter);
            if (sourcebookHide) { return true; }

            // Classes
            final boolean classHide = filterThroughArray(s, visibleClasses, Spell::usableByClass);
            if (classHide) { return true; }

            // Schools
            final boolean schoolHide = filterThroughArray(s, visibleSchools, schoolFilter);
            if (schoolHide) { return true; }

            // Casting time types
            final boolean castingTimeTypeHide = filterThroughArray(s, visibleCastingTimeTypes, castingTimeTypeFilter);
            if (castingTimeTypeHide) { return true; }

            // Duration types
            final boolean durationTypeHide = filterThroughArray(s, visibleDurationTypes, durationTypeFilter);
            if (durationTypeHide) { return true; }

            // Range types
            final boolean rangeTypeHide = filterThroughArray(s, visibleRangeTypes, rangeTypeFilter);
            if (rangeTypeHide) { return true; }

            // Casting time bounds
            final boolean castingTimeBoundsHide = filterAgainstBounds(s, castingTimeBounds, Spell::getCastingTime);
            if (castingTimeBoundsHide) { return true; }

            // Duration bounds
            final boolean durationBoundsHide = filterAgainstBounds(s, durationBounds, Spell::getDuration);
            if (durationBoundsHide) { return true; }

            // Range bounds
            final boolean rangeBoundsHide = filterAgainstBounds(s, rangeBounds, Spell::getRange);
            if (rangeBoundsHide) { return true; }


            // The rest of the filtering conditions
            boolean toHide = (cp.filterFavorites() && !cp.isFavorite(s));
            toHide = toHide || (cp.filterKnown() && !cp.isKnown(s));
            toHide = toHide || (cp.filterPrepared() && !cp.isPrepared(s));
            toHide = toHide || !cp.getRitualFilter(s.getRitual());
            toHide = toHide || !cp.getConcentrationFilter(s.getConcentration());
            final boolean[] components = s.getComponents();
            toHide = toHide || !cp.getVerbalComponentFilter(components[0]);
            toHide = toHide || !cp.getSomaticComponentFilter(components[1]);
            toHide = toHide || !cp.getMaterialComponentFilter(components[2]);
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
                final Sourcebook[] visibleSourcebooks = cp.getVisibleValues(Sourcebook.class);
                final CasterClass[] visibleClasses = cp.getVisibleValues(CasterClass.class);
                final School[] visibleSchools = cp.getVisibleValues(School.class);
                final CastingTime.CastingTimeType[] visibleCastingTimeTypes = cp.getVisibleValues(CastingTime.CastingTimeType.class);
                final Duration.DurationType[] visibleDurationTypes = cp.getVisibleValues(Duration.DurationType.class);
                final Range.RangeType[] visibleRangeTypes = cp.getVisibleValues(Range.RangeType.class);
                final boolean isText = !searchText.isEmpty();
                final Pair<CastingTime,CastingTime> castingTimeMinMax = boundsFromData(cp.getQuantityRangeInfo(CastingTime.CastingTimeType.class), CastingTime.class, TimeUnit.class, CastingTime.CastingTimeType.TIME);
                final Pair<Duration, Duration> durationMinMax = boundsFromData(cp.getQuantityRangeInfo(Duration.DurationType.class), Duration.class, TimeUnit.class, Duration.DurationType.SPANNING);
                final Pair<Range, Range> rangeMinMax = boundsFromData(cp.getQuantityRangeInfo(Range.RangeType.class), Range.class, LengthUnit.class, Range.RangeType.RANGED);
                for (Spell s : spellList) {
                    if (!filterItem(s, visibleSourcebooks, visibleClasses, visibleSchools, visibleCastingTimeTypes, visibleDurationTypes, visibleRangeTypes, castingTimeMinMax, durationMinMax, rangeMinMax, isText, searchText)) {
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
    private MainActivity main;
    private final List<Spell> spellList;
    private List<Spell> filteredSpellList;
    private final View.OnClickListener listener = (View view) -> {
        final SpellRowHolder srh = (SpellRowHolder) view.getTag();
        final Spell spell = srh.getSpell();
        final int pos = srh.getLayoutPosition();
        main.openSpellWindow(spell, pos);
    };
//    private final View.OnLongClickListener longListener = (View view) -> {
//        final SpellRowHolder srh = (SpellRowHolder) view.getTag();
//        final Spell spell = srh.getSpell();
//        main.openSpellPopup(view, spell);
//        return true;
//    };


    // Constructor from the list of spells
    SpellRowAdapter(List<Spell> spells) {
        spellList = spells;
        filteredSpellList = spells;
    }

    // Filterable methods
    public Filter getFilter() {
        synchronized (sharedLock) {
            return new SpellFilter(main.getCharacterProfile());
        }
    }

    // For use from MainActivity
    void filter(CharSequence query) {
        synchronized (sharedLock) {
            getFilter().filter(query);
            notifyDataSetChanged();
        }
    }

    private void sortFilter(List<Pair<SortField,Boolean>> sortParameters) {
        synchronized (sharedLock) {
            Collections.sort(spellList, new SpellComparator(sortParameters));
            filter(null);
        }
    }

    void singleSort(SortField sf, boolean reverse) {
        final List<Pair<SortField,Boolean>> sortParameters = new ArrayList<Pair<SortField,Boolean>>() {{
            add(new Pair<>(sf, reverse));
        }};
        sortFilter(sortParameters);
    }

    void doubleSort(SortField sf1, SortField sf2, boolean reverse1, boolean reverse2) {
        final List<Pair<SortField,Boolean>> sortParameters = new ArrayList<Pair<SortField,Boolean>>() {{
            add(new Pair<>(sf1, reverse1));
            add(new Pair<>(sf2, reverse2));
        }};
        sortFilter(sortParameters);
    }

    // ViewHolder methods
    @NonNull
    public SpellRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final SpellRowBinding binding = SpellRowBinding.inflate(inflater, parent, false);
        return new SpellRowHolder(binding);
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

    // When attached to a recycler view, set the relevant values
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        main = (MainActivity) recyclerView.getContext();
    }

    int getSpellIndex(Spell spell) {
        synchronized (sharedLock) {
            return filteredSpellList.indexOf(spell);
        }
    }
}
