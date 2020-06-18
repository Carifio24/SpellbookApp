package dnd.jon.spellbook;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
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

//public class SpellRowAdapter extends ItemListAdapter<Spell, SpellRowBinding, SpellRowAdapter.SpellRowHolder> implements Filterable {
public class SpellRowAdapter extends RecyclerView.Adapter<SpellRowAdapter.SpellRowHolder> {

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

        // For convenience, we construct the row holder directly from the SpellRowBinding generated from the XML
        public SpellRowHolder(SpellRowBinding b) {
            super(b, SpellRowBinding::setSpell);
            itemView.setTag(this);
            itemView.setOnClickListener(listener);
            //itemView.setOnLongClickListener(longListener);
        }

        public void bind(Spell spell) {
            super.bind(spell);

            //Set the buttons to show the appropriate images
            if (item != null) {
                binding.spellRowFavoriteButton.set(spellbookViewModel.isFavorite(spell));
                binding.spellRowPreparedButton.set(spellbookViewModel.isPrepared(spell));
                binding.spellRowKnownButton.set(spellbookViewModel.isKnown(spell));

//                final LifecycleOwner lifecycleOwner = binding.getLifecycleOwner();
//                spellbookViewModel.isFavorite(spell).observe(lifecycleOwner, binding.spellRowFavoriteButton::set);
//                spellbookViewModel.isPrepared(spell).observe(lifecycleOwner, binding.spellRowPreparedButton::set);
//                spellbookViewModel.isKnown(spell).observe(lifecycleOwner, binding.spellRowKnownButton::set);
            }

            // Set button callbacks
            binding.spellRowFavoriteButton.setOnClickListener( (v) -> spellbookViewModel.toggleFavorite(item));
            binding.spellRowPreparedButton.setOnClickListener( (v) -> spellbookViewModel.togglePrepared(item));
            binding.spellRowKnownButton.setOnClickListener( (v) -> spellbookViewModel.toggleKnown(item));

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
    SpellRowAdapter(SpellbookViewModel spellbookViewModel) {
        this.spellbookViewModel = spellbookViewModel;
        filteredSpellList = new ArrayList<>();
        filter = new SpellFilter();
        listener = (View view) -> {
            final SpellRowHolder srh = (SpellRowHolder) view.getTag();
            final Spell spell = srh.getItem();
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
