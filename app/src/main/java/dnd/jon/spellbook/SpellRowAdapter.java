package dnd.jon.spellbook;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
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
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;

import dnd.jon.spellbook.databinding.SpellRowBinding;

public class SpellRowAdapter extends ItemListAdapter<Spell, SpellRowBinding> implements Filterable {

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
    public class SpellRowHolder extends ItemViewHolder<Spell, SpellRowBinding> {

        private final MainActivity main;
        private Runnable postToggleAction = () -> {};

        // For convenience, we construct the adapter directly from the SpellRowBinding generated from the XML
        public SpellRowHolder(SpellRowBinding b) {
            super(b, SpellRowBinding::setSpell);
            main = (MainActivity) b.getRoot().getContext();
            itemView.setTag(this);
            itemView.setOnClickListener(listener);
            //itemView.setOnLongClickListener(longListener);
        }

        public void bind(Spell spell) {
            super.bind(spell);

            //Set the buttons to show the appropriate images
            if (main != null && main.getCharacterProfile() != null && item != null) {
                binding.spellRowFavoriteButton.set(main.getCharacterProfile().isFavorite(item));
                binding.spellRowPreparedButton.set(main.getCharacterProfile().isPrepared(item));
                binding.spellRowKnownButton.set(main.getCharacterProfile().isKnown(item));
            }

            // Set button callbacks
            postToggleAction = () -> {
                main.saveCharacterProfile();
                main.updateSpellWindow(item, main.getCharacterProfile().isFavorite(item), main.getCharacterProfile().isPrepared(item), main.getCharacterProfile().isKnown(item));
            };
            binding.spellRowFavoriteButton.setOnClickListener( (v) -> { main.getCharacterProfile().toggleFavorite(item); postToggleAction.run(); } );
            binding.spellRowPreparedButton.setOnClickListener( (v) -> { main.getCharacterProfile().togglePrepared(item); postToggleAction.run(); } );
            binding.spellRowKnownButton.setOnClickListener( (v) -> { main.getCharacterProfile().toggleKnown(item); postToggleAction.run(); } );

        }
    }

    // Inner class for filtering the list
    private class SpellFilter extends Filter {

        private final CharacterProfile cp;

        SpellFilter(CharacterProfile cp) {
            this.cp = cp;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            synchronized (sharedLock) {
                filteredSpellList = spellbookViewModel.getSortFilteredSpells();
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
    private List<Spell> filteredSpellList;
    private SpellbookViewModel spellbookViewModel;
    private final View.OnClickListener listener = (View view) -> {
        final SpellRowHolder srh = (SpellRowHolder) view.getTag();
        final Spell spell = srh.getItem();
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
    SpellRowAdapter(Context context) {
        super(context, SpellRowBinding::inflate, SpellRowBinding::setSpell);
    }

    void setSpells(List<Spell> spells) {
        setItems(spells);
        filteredSpellList = spells;
        filter();
        notifyDataSetChanged();
    }

    // Filterable methods
    public Filter getFilter() {
        synchronized (sharedLock) {
            return new SpellFilter(main.getCharacterProfile());
        }
    }

    // For use from MainActivity
    void filter() {
        synchronized (sharedLock) {
            getFilter().filter(null);
        }
    }
    void singleSort(SortField sf, boolean reverse) {
        synchronized (sharedLock) {
            final ArrayList<Pair<SortField,Boolean>> sortParameters = new ArrayList<Pair<SortField,Boolean>>() {{
                add(new Pair<>(sf, reverse));
            }};
            Collections.sort(items, new SpellComparator(sortParameters));
            filter();
            notifyDataSetChanged();
        }
    }

    void doubleSort(SortField sf1, SortField sf2, boolean reverse1, boolean reverse2) {
        synchronized (sharedLock) {
            final ArrayList<Pair<SortField,Boolean>> sortParameters = new ArrayList<Pair<SortField,Boolean>>() {{
                add(new Pair<>(sf1, reverse1));
                add(new Pair<>(sf2, reverse2));
            }};
            Collections.sort(items, new SpellComparator(sortParameters));
            filter();
            notifyDataSetChanged();
        }
    }

    void sort() {
        synchronized (sharedLock) {
            doubleSort(spellbookViewModel.getFirstSortField().getValue(), spellbookViewModel.getSecondSortField().getValue(), spellbookViewModel.getFirstSortReverse().getValue(), spellbookViewModel.getSecondSortReverse().getValue());
        }
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
}
