package dnd.jon.spellbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import dnd.jon.spellbook.databinding.SpellTableBinding;

public class SpellTableFragment extends SpellbookFragment<SpellTableBinding> {

    private SpellAdapter spellAdapter;

    public SpellTableFragment() {
        super(R.layout.spell_table);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = SpellTableBinding.inflate(inflater);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //final LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        //viewModel.currentSpell().observe(lifecycleOwner, this::updateSpell);
        setup();
    }

    private void setupSwipeRefreshLayout() {
        // Set up the 'swipe down to filter' behavior of the RecyclerView
        final SwipeRefreshLayout swipeLayout = binding.swipeRefreshLayout;
        swipeLayout.setOnRefreshListener(() -> {
            viewModel.setSortNeeded();
            viewModel.setFilterNeeded();
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

    void updateCurrentSpell() { updateSpell(viewModel.currentSpell().getValue()); }

    private void setupSpellRecycler() {
        final RecyclerView spellRecycler = binding.spellRecycler;
        final RecyclerView.LayoutManager spellLayoutManager = new LinearLayoutManager(requireContext());
        spellAdapter = new SpellAdapter(viewModel);
        spellRecycler.setAdapter(spellAdapter);
        spellRecycler.setLayoutManager(spellLayoutManager);
    }

    private void setup() {
        setupSpellRecycler();
        setupSwipeRefreshLayout();
        //setupBottomNavBar();
        final LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        viewModel.currentSpells().observe(lifecycleOwner,
                filteredSpells -> spellAdapter.setSpells(filteredSpells));
        viewModel.currentSpellFavoriteLD().observe(lifecycleOwner, favorite -> updateCurrentSpell());
        viewModel.currentSpellPreparedLD().observe(lifecycleOwner, prepared -> updateCurrentSpell());
        viewModel.currentSpellKnownLD().observe(lifecycleOwner, known -> updateCurrentSpell());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        viewModel.setSpellTableVisible(!hidden);
        super.onHiddenChanged(hidden);
    }

}
