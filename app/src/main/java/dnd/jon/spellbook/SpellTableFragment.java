package dnd.jon.spellbook;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

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
        final FragmentActivity activity = requireActivity();
        this.viewModel = new ViewModelProvider(activity, activity.getDefaultViewModelProviderFactory()).get(SpellbookViewModel.class);
        viewModel.getCurrentSpell().observe(getViewLifecycleOwner(), this::updateSpell);
        setup();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = SpellTableBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    void filter() { spellAdapter.filter(viewModel.getSearchQuery().getValue()); }
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
        spellAdapter = new SpellAdapter(requireContext(), viewModel);
        spellRecycler.setAdapter(spellAdapter);
        spellRecycler.setLayoutManager(spellLayoutManager);

        // If we're on a tablet, we need to keep track of the index of the currently selected spell when the list changes
        final boolean onTablet = getResources().getBoolean(R.bool.isTablet);
        if (onTablet) {
            spellAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    //viewModel.setCurrentSpell();
                }
            });
        }
    }

    private void setup() {
        setupSpellRecycler();
        setupSwipeRefreshLayout();
        viewModel.getSearchQuery().observe(getViewLifecycleOwner(), (query) ->  {
            this.stopScrolling();
            spellAdapter.filter(query);
        });
    }

}
