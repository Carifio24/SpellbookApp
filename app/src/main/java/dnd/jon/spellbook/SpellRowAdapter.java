package dnd.jon.spellbook;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import dnd.jon.spellbook.databinding.SpellRowBinding;

public class SpellRowAdapter extends RecyclerView.Adapter<SpellRowAdapter.SpellRowHolder> implements Filterable {

    private static final Object sharedLock = new Object();

    // Inner class for holding the spell row views
    public class SpellRowHolder extends RecyclerView.ViewHolder {

        private Spell spell = null;
        private final SpellRowBinding binding;

        // For convenience, we construct the adapter directly from the SpellRowBinding generated from the XML
        public SpellRowHolder(SpellRowBinding b) {
            super(b.getRoot());
            binding = b;
            itemView.setTag(this);
            itemView.setOnClickListener(listener);
            itemView.setOnLongClickListener(longListener);
        }

        public void bind(Spell s) {
            spell = s;
            binding.setSpell(spell);
            binding.executePendingBindings();
        }

        public Spell getSpell() { return spell; }
    }

    // Inner class for filtering the list
    private class SpellFilter extends Filter {

        private CharacterProfile cp;
        String searchText;

        SpellFilter(CharacterProfile cp, String searchText) {
            this.cp = cp;
            this.searchText = searchText;
        }

        boolean filterItem(boolean isClass, boolean isText, Spell s, CasterClass cc, String text) {

            // Get the spell name
            String spname = s.getName().toLowerCase();

            // Filter by class usability, favorite, and search text, and finally sourcebook
            boolean toHide = (isClass && !s.usableByClass(cc));
            toHide = toHide || (cp.filterFavorites() && !cp.isFavorite(s));
            toHide = toHide || (cp.filterKnown() && !cp.isKnown(s));
            toHide = toHide || (cp.filterPrepared() && !cp.isPrepared(s));
            toHide = toHide || (isText && !spname.contains(text));
            toHide = toHide || (!cp.getSourcebookFilter(s.getSourcebook()));
            return toHide;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            //System.out.println("Entering performFiltering");

            synchronized (sharedLock) {
                // Filter the list of spells
                FilterResults filterResults = new FilterResults();
                filteredSpellList = new ArrayList<>();
                CasterClass cc = cp.getFilterClass();
                boolean isClass = (cc != null);
                boolean isText = !searchText.isEmpty();
                for (Spell s : spellList) {
                    if (!filterItem(isClass, isText, s, cc, searchText)) {
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
            //System.out.println("Entering publishResults");
            notifyDataSetChanged();
        }
    }

    // Member values
    // References to the RecyclerView and the MainActivity
    // Also the list of spells, and the click listeners
    private MainActivity main;
    private RecyclerView recyclerView;
    private ArrayList<Spell> spellList;
    private ArrayList<Spell> filteredSpellList;
    private View.OnClickListener listener = (View view) -> {
        SpellRowHolder srh = (SpellRowHolder) view.getTag();
        Spell spell = srh.getSpell();
        main.openSpellWindow(spell);
    };
    private View.OnLongClickListener longListener = (View view) -> {
        SpellRowHolder srh = (SpellRowHolder) view.getTag();
        Spell spell = srh.getSpell();
        main.openSpellPopup(view, spell);
        return true;
    };


    // Constructor from the list of spells
    public SpellRowAdapter(ArrayList<Spell> spells) {
        spellList = spells;
        filteredSpellList = spells;
    }

    // Filterable methods
    public Filter getFilter() {
        //System.out.println("getFilter");
        synchronized (sharedLock) {
            return new SpellFilter(main.getCharacterProfile(), main.searchText());
        }
    }

    // For use from MainActivity
    void filter() {
        synchronized (sharedLock) {
            //System.out.println("Filter");
            getFilter().filter(null);
        }
    }
    void singleSort(SortField sf1, boolean reverse) {
        //System.out.println("singleSort");
        synchronized (sharedLock) {
            Collections.sort(spellList, new SpellOneFieldComparator(sf1, reverse));
            filter();
            notifyDataSetChanged();

//            Collections.sort(filteredSpellList, new SpellOneFieldComparator(sf1, reverse));
//            notifyDataSetChanged();
        }
    }
    void doubleSort(SortField sf1, SortField sf2, boolean reverse1, boolean reverse2) {
        //System.out.println("doubleSort");
        synchronized (sharedLock) {
//            Collections.sort(filteredSpellList, new SpellTwoFieldComparator(sf1, sf2, reverse1, reverse2));
//            notifyDataSetChanged();

            Collections.sort(spellList, new SpellTwoFieldComparator(sf1,  sf2, reverse1, reverse2));
            filter();
            notifyDataSetChanged();
        }
    }

    // ViewHolder methods
    public SpellRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpellRowBinding binding = SpellRowBinding.inflate(inflater, parent, false);
        return new SpellRowHolder(binding);
    }

    public void onBindViewHolder(SpellRowHolder holder, int position) {
        Spell spell = filteredSpellList.get(position);
        holder.bind(spell);
    }

    public int getItemCount() {
        return filteredSpellList.size();
    }

    // When attached to a recycler view, set the relevant values
    @Override
    public void onAttachedToRecyclerView(RecyclerView rv) {
        super.onAttachedToRecyclerView(rv);
        recyclerView = rv;
        main = (MainActivity) recyclerView.getContext();
    }
}
