package dnd.jon.spellbook;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import dnd.jon.spellbook.databinding.SpellTableBinding;

public class SpellTableFragment extends Fragment {

    private SpellTableBinding binding;
    private SpellAdapter spellAdapter;
    private SpellbookViewModel viewModel;

    public SpellTableFragment() {
        super(R.layout.spell_table);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = SpellTableBinding.inflate(inflater);
        final FragmentActivity activity = requireActivity();
        this.viewModel = new ViewModelProvider(activity, activity.getDefaultViewModelProviderFactory()).get(SpellbookViewModel.class);
        final LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        //viewModel.currentSpell().observe(lifecycleOwner, this::updateSpell);
        setup();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupSwipeRefreshLayout() {
        // Set up the 'swipe down to filter' behavior of the RecyclerView
        final SwipeRefreshLayout swipeLayout = binding.swipeRefreshLayout;
        swipeLayout.setOnRefreshListener(() -> {
            viewModel.setFilterNeeded(true);
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

        spellAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                if (itemCount != 1 || !(payload instanceof SpellAdapter.SpellRowProperty)) { return; }
                final SpellAdapter.SpellRowProperty property = (SpellAdapter.SpellRowProperty) payload;
                final Spell spell = spellAdapter.getSpellAtPosition(positionStart);
                switch (property) {
                    case FAVORITE:
                        viewModel.toggleFavorite(spell);
                        break;
                    case PREPARED:
                        viewModel.togglePrepared(spell);
                        break;
                    case KNOWN:
                        viewModel.toggleKnown(spell);
                }
            }
        });
    }

    private void setup() {
        setupSpellRecycler();
        setupSwipeRefreshLayout();
        final LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        viewModel.currentSpells().observe(lifecycleOwner,
                filteredSpells -> spellAdapter.setSpells(filteredSpells));
        //viewModel.currentSpellFavoriteLD().observe(lifecycleOwner, favorite -> updateCurrentSpell());
        //viewModel.currentSpellPreparedLD().observe(lifecycleOwner, prepared -> updateCurrentSpell());
        //viewModel.currentSpellKnownLD().observe(lifecycleOwner, known -> updateCurrentSpell());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        viewModel.setSpellTableVisible(!hidden);
    }

}
