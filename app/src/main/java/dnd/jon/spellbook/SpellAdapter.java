package dnd.jon.spellbook;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.function.BiConsumer;

import dnd.jon.spellbook.databinding.SpellRowBinding;

public class SpellAdapter extends RecyclerView.Adapter<SpellAdapter.SpellRowHolder> {

    // This is for synchronization between various processes
    // which can otherwise fire simultaneously
    // We don't want multiple threads mutating the spell list at the same time
    private static final Object sharedLock = new Object();

//    enum SpellRowProperty {
//        FAVORITE, PREPARED, KNOWN;
//    }

    // Inner class for holding the spell row views
    public class SpellRowHolder extends RecyclerView.ViewHolder {

        private Spell spell = null;
        private final SpellRowBinding binding;
        private final SpellbookViewModel viewModel;

        // For convenience, we construct the adapter directly from the SpellRowBinding generated from the XML
        public SpellRowHolder(SpellRowBinding binding, SpellbookViewModel viewModel) {
            super(binding.getRoot());
            this.binding = binding;
            this.viewModel = viewModel;
            itemView.setTag(this);
            itemView.setOnClickListener(listener);
            //itemView.setOnLongClickListener(longListener);
        }

        public void bind(Spell s) {
            spell = s;
            binding.setSpell(spell);
            binding.setContext(viewModel.getSpellContext());
            binding.executePendingBindings();

            //Set the buttons to show the appropriate images
            if (spell != null) {
                final SpellFilterStatus spellFilterStatus = viewModel.getSpellFilterStatus();
                if (spellFilterStatus != null) {
                    binding.spellRowFavoriteButton.set(spellFilterStatus.isFavorite(spell));
                    binding.spellRowPreparedButton.set(spellFilterStatus.isPrepared(spell));
                    binding.spellRowKnownButton.set(spellFilterStatus.isKnown(spell));
                }
            }

            // Set button callbacks
//            binding.spellRowFavoriteButton.setOnClickListener((v) -> notifyItemChanged(getAdapterPosition(), SpellRowProperty.FAVORITE));
//            binding.spellRowPreparedButton.setOnClickListener((v) -> notifyItemChanged(getAdapterPosition(), SpellRowProperty.PREPARED));
//            binding.spellRowKnownButton.setOnClickListener((v) -> notifyItemChanged(getAdapterPosition(), SpellRowProperty.KNOWN));
            binding.spellRowFavoriteButton.setOnClickListener((v) -> viewModel.toggleFavorite(spell));
            binding.spellRowPreparedButton.setOnClickListener((v) -> viewModel.togglePrepared(spell));
            binding.spellRowKnownButton.setOnClickListener((v) -> viewModel.toggleKnown(spell));

        }

        public Spell getSpell() { return spell; }
    }

    // Member values
    // References to the RecyclerView and the MainActivity
    // Also the list of spells, and the click listeners
    private List<Spell> spells;
    private final View.OnClickListener listener;
    private final SpellbookViewModel viewModel;
//    private final View.OnLongClickListener longListener = (View view) -> {
//        final SpellRowHolder srh = (SpellRowHolder) view.getTag();
//        final Spell spell = srh.getSpell();
//        main.openSpellPopup(view, spell);
//        return true;
//    };

    // Constructor from the list of spells
    SpellAdapter(SpellbookViewModel viewModel) {
        this.viewModel = viewModel;
        this.spells = viewModel.getAllSpells();
        this.listener = (View view) -> {
            final SpellRowHolder srh = (SpellRowHolder) view.getTag();
            final Spell spell = srh.getSpell();
            this.viewModel.setCurrentSpell(spell);
        };
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
        if ( (position >= spells.size()) || (position < 0) ) { return; }

        // Get the appropriate spell and bind it to the holder
        final Spell spell = spells.get(position);
        holder.bind(spell);
    }

    // The number of spells to be displayed
    public int getItemCount() {
        synchronized (sharedLock) {
            return spells != null ? spells.size() : 0;
        }
    }

    int getSpellIndex(Spell spell) {
        synchronized (sharedLock) {
            return spells.indexOf(spell);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    void setSpells(List<Spell> spells) {
        this.spells = spells;
        notifyDataSetChanged();
    }

    Spell getSpellAtPosition(int position) {
        synchronized (sharedLock) {
            if ((position >= spells.size()) || (position < 0)) {
                return null;
            }
            return spells.get(position);
        }
    }
}
