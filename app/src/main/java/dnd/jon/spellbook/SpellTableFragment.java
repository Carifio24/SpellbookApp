package dnd.jon.spellbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import dnd.jon.spellbook.databinding.SpellTableBinding;

public class SpellTableFragment extends Fragment {

    private SpellTableBinding binding;
    private SpellbookViewModel spellbookViewModel;
    private SpellAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SpellTableBinding.inflate(inflater);
        spellbookViewModel = new ViewModelProvider(requireActivity(), new SpellbookViewModelFactory(requireActivity().getApplication())).get(SpellbookViewModel.class);

        adapter = new SpellAdapter(spellbookViewModel);

        spellbookViewModel.getCurrentSpells().observe(this, adapter::setSpells);

        binding.spellRecycler.setAdapter(adapter);
        binding.spellRecycler.setLayoutManager(new LinearLayoutManager(requireActivity()));

        // Set up the swipe refresh layout
        setupSwipeRefreshLayout();

//        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
//            int newVisibility = binding.getRoot().getVisibility();
//            spellbookViewModel.setSpellTableVisible(newVisibility == View.VISIBLE);
//        });

        // Filter and sort when dictated by the view model
        final LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        spellbookViewModel.getSortSignal().observe(lifecycleOwner, (nothing) -> adapter.sort());

        // When the set of current spells changes, update the spells in the adapter
        spellbookViewModel.getCurrentSpells().observe(lifecycleOwner, adapter::setSpells);

        // When the properties of the current spell change, update the appropriate row
        spellbookViewModel.getCurrentSpellChange().observe(lifecycleOwner, (nothing) -> adapter.notifyItemChanged(spellbookViewModel.getCurrentSpellIndex()));

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
            spellbookViewModel.setToFilter();
            swipeLayout.setRefreshing(false);
        });

        // Configure the refreshing colors
        swipeLayout.setColorSchemeResources(R.color.darkBrown, R.color.lightBrown, R.color.black);
    }

    RecyclerView getRecyclerView() { return binding.spellRecycler; }

}
