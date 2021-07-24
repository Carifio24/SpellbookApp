package dnd.jon.spellbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import dnd.jon.spellbook.databinding.SpellTableBinding;

public class SpellTableFragment extends Fragment {

    private SpellTableBinding binding;
    private SpellRowAdapter spellAdapter;

    private final List<Spell> spells;

    private final SpellTableHandler handler;

    interface SpellTableHandler {
        Spell getCurrentSpell();
        SpellStatus getSpellStatus(Spell spell);
        SpellFilterStatus getSpellFilterStatus();
        SortFilterStatus getSortFilterStatus();
        void saveSpellFilterStatus();
        void updateFavorite(Spell spell, boolean favorite);
        void updateKnown(Spell spell, boolean known);
        void updatePrepared(Spell spell, boolean prepared);
        void handleSpellDataUpdate();
        CharSequence getSearchQuery();
        List<Spell> getSpells();
    }

    public SpellTableFragment(SpellTableHandler handler) {
        super(R.layout.spell_table);
        this.handler = handler;
        this.spells = handler.getSpells();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = SpellTableBinding.inflate(inflater);
        setupSpellRecycler();
        setupSwipeRefreshLayout();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    void filter() { spellAdapter.filter(); }
    void sort() { spellAdapter.doubleSort(); }

    private void setupSwipeRefreshLayout() {
        // Set up the 'swipe down to filter' behavior of the RecyclerView
        final SwipeRefreshLayout swipeLayout = binding.swipeRefreshLayout;
        swipeLayout.setOnRefreshListener(() -> {
            filter();
            binding.swipeRefreshLayout.setRefreshing(false);
        });

        // Configure the refreshing colors
        swipeLayout.setColorSchemeResources(R.color.darkBrown, R.color.lightBrown, R.color.black);
    }

    void stopScrolling() {
        binding.spellRecycler.stopScroll();
    }

    void updateSpell(Spell spell) {
        final int index = spellAdapter.getSpellIndex(spell);
        spellAdapter.notifyItemChanged(index);
    }

    private void setupSpellRecycler() {
        final RecyclerView spellRecycler = binding.spellRecycler;
        final RecyclerView.LayoutManager spellLayoutManager = new LinearLayoutManager(requireContext());
        spellAdapter = new SpellRowAdapter(requireContext(), spells, handler);
        spellRecycler.setAdapter(spellAdapter);
        spellRecycler.setLayoutManager(spellLayoutManager);

        // If we're on a tablet, we need to keep track of the index of the currently selected spell when the list changes
        final boolean onTablet = getResources().getBoolean(R.bool.isTablet);
        if (onTablet) {
            spellAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    handler.handleSpellDataUpdate();
                }
            });
        }
    }

}
