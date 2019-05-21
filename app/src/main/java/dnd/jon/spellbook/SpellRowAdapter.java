package dnd.jon.spellbook;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import dnd.jon.spellbook.databinding.SpellRowBinding;

public class SpellRowAdapter extends RecyclerView.Adapter<SpellRowAdapter.SpellRowHolder> {

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
            System.out.println("Spell name: " + s.getName());
            binding.setSpell(spell);
            binding.executePendingBindings();
        }

        public Spell getSpell() { return spell; }
    }

    // Member values
    // References to the RecyclerView and the MainActivity
    // Also the list of spells, and the click listeners
    private MainActivity main;
    private RecyclerView recyclerView;
    private ArrayList<Spell> spellList;
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
    }

    // ViewHolder methods
    public SpellRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpellRowBinding binding = SpellRowBinding.inflate(inflater, parent, false);
        return new SpellRowHolder(binding);
    }

    public void onBindViewHolder(SpellRowHolder holder, int position) {
        Spell spell = spellList.get(position);
        holder.bind(spell);
    }

    public int getItemCount() {
        return spellList.size();
    }

    // When attached to a recycler view, set the relevant values
    @Override
    public void onAttachedToRecyclerView(RecyclerView rv) {
        super.onAttachedToRecyclerView(rv);
        recyclerView = rv;
        main = (MainActivity) recyclerView.getContext();
    }
}
