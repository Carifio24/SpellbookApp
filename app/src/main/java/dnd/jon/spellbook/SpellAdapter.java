package dnd.jon.spellbook;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import android.util.Pair;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.BiFunction;

import dnd.jon.spellbook.databinding.SpellRowBinding;

//public class SpellRowAdapter extends ItemListAdapter<Spell, SpellRowBinding, SpellRowAdapter.SpellRowHolder> implements Filterable {
public class SpellAdapter extends RecyclerView.Adapter<SpellAdapter.SpellRowHolder> {

    private static final Object sharedLock = new Object();

    // Filters for SpellFilter
//    private static final BiFunction<Spell, Sourcebook, Boolean> sourcebookFilter = (spell, sourcebook) -> spell.getSourcebook() == sourcebook;
//    private static final BiFunction<Spell, School, Boolean> schoolFilter = (spell, school) -> spell.getSchool() == school;
//    private static final BiFunction<Spell, CastingTime.CastingTimeType,Boolean> castingTimeTypeFilter = (spell, castingTimeType) -> spell.getCastingTime().getType() == castingTimeType;
//    private static final BiFunction<Spell, Duration.DurationType, Boolean> durationTypeFilter = (spell, durationType) -> spell.getDuration().getType() == durationType;
//    private static final BiFunction<Spell, Range.RangeType, Boolean> rangeTypeFilter = (spell, rangeType) -> spell.getRange().getType() == rangeType;

    // Inner class for holding the spell row views
    public class SpellRowHolder extends RecyclerView.ViewHolder {

        // The binding
        final SpellRowBinding binding;

        // For convenience, we construct the row holder directly from the SpellRowBinding generated from the XML
        public SpellRowHolder(SpellRowBinding b) {
            super(b.getRoot());
            this.binding = b;
            itemView.setTag(this);
            itemView.setOnClickListener(listener);
            //itemView.setOnLongClickListener(longListener);
        }

        Spell getSpell() { return binding.getSpell(); }

        public void bind(Spell spell) {

            //Set the buttons to show the appropriate images
            if (spell != null) {

                // Set the spell
                binding.setSpell(spell);

                // Set the sourcebook and school names
                binding.setCode(spellbookViewModel.getCodeOrName(spell.getSourceID()));
                binding.setSchool(spellbookViewModel.getSchoolName(spell.getSchoolID()));

                // Set the buttons correctly
                binding.spellRowFavoriteButton.set(spellbookViewModel.isFavorite(spell));
                binding.spellRowPreparedButton.set(spellbookViewModel.isPrepared(spell));
                binding.spellRowKnownButton.set(spellbookViewModel.isKnown(spell));

                // Set button callbacks
                binding.spellRowFavoriteButton.setOnClickListener((v) -> spellbookViewModel.toggleFavorite(spell));
                binding.spellRowPreparedButton.setOnClickListener((v) -> spellbookViewModel.togglePrepared(spell));
                binding.spellRowKnownButton.setOnClickListener((v) -> spellbookViewModel.toggleKnown(spell));

                // Set the source abbreviation
                binding.spellRowSourcebookLabel.setText(spellbookViewModel.sourceCode(spell));

                binding.executePendingBindings();

            }

        }

    }

    // Inner class for filtering the list
    private class SpellFilter extends Filter {

        SpellFilter() { }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            synchronized (sharedLock) {
                final FilterResults filterResults = new FilterResults();
                spellbookViewModel.setFilterText(constraint.toString());
                filteredSpellList = SpellbookUtils.coalesce(spellbookViewModel.getCurrentSpells().getValue(), new ArrayList<>());
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
    // The list of spells, and the click listeners
    private List<Spell> filteredSpellList;
    private final SpellbookViewModel spellbookViewModel;
    private final View.OnClickListener listener;
    private final SpellFilter filter;
    //    private final View.OnLongClickListener longListener = (View view) -> {
//        final SpellRowHolder srh = (SpellRowHolder) view.getTag();
//        final Spell spell = srh.getSpell();
//        main.openSpellPopup(view, spell);
//        return true;
//    };


    // Constructor
    SpellAdapter(SpellbookViewModel spellbookViewModel) {
        this.spellbookViewModel = spellbookViewModel;
        filteredSpellList = new ArrayList<>();
        filter = new SpellFilter();
        listener = (View view) -> {
            final SpellRowHolder srh = (SpellRowHolder) view.getTag();
            final Spell spell = srh.getSpell();
            final Integer index = srh.getAdapterPosition();
            this.spellbookViewModel.setCurrentSpell(spell, index);
        };
    }

    void setSpells(List<Spell> spells) {
        System.out.println("Spells are: " + spells);
        filteredSpellList = spells;
        //filter();
        notifyDataSetChanged();
        System.out.println("There are " + getItemCount() + " visible spells");
    }

    // Filterable methods
    public Filter getFilter() {
        synchronized (sharedLock) {
            return filter;
        }
    }

    // For use from MainActivity
    void filter() {
        synchronized (sharedLock) {
            getFilter().filter(null);
        }
    }

    void doubleSort(SortField sf1, SortField sf2, boolean reverse1, boolean reverse2) {
        synchronized (sharedLock) {
            final ArrayList<Pair<SortField,Boolean>> sortParameters = new ArrayList<Pair<SortField,Boolean>>() {{
                add(new Pair<>(sf1, reverse1));
                add(new Pair<>(sf2, reverse2));
            }};
            Collections.sort(filteredSpellList, new SpellComparator(sortParameters));
            //filter();
            notifyDataSetChanged();
        }
    }

    void sort() {
        synchronized (sharedLock) {
            if (getItemCount() == 0) { return; }
            System.out.println("First sort field is " + spellbookViewModel.getFirstSortReverse());
            System.out.println("Second sort field is " + spellbookViewModel.getSecondSortReverse());
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

    @Override
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
            return filteredSpellList != null ? filteredSpellList.size() : 0;
        }
    }

}
