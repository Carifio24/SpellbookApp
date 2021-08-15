package dnd.jon.spellbook;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import dnd.jon.spellbook.databinding.SpellWindowBinding;

public class SpellWindowFragment extends Fragment {

    static final String SPELL_KEY = "spell";
    //static final String TEXT_SIZE_KEY = "textSize";
    static final String FAVORITE_KEY = "favorite";
    static final String KNOWN_KEY = "known";
    static final String PREPARED_KEY = "prepared";
    static final String USE_EXPANDED_KEY = "use_expanded";
    static final String SPELL_STATUS_KEY = "spell_status";

    private SpellWindowBinding binding;
    private SpellbookViewModel viewModel;

    public SpellWindowFragment() {
        super(R.layout.spell_window);
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
        final Bundle args = getArguments();
        final Spell spell = args != null ? args.getParcelable(SPELL_KEY) : null;
        final boolean useExpanded = args != null && args.getBoolean(USE_EXPANDED_KEY, false);
        binding = SpellWindowBinding.inflate(inflater);
        binding.setSpell(spell);
        binding.setUseExpanded(useExpanded);
        binding.executePendingBindings();
        final FragmentActivity activity = requireActivity();
        this.viewModel = new ViewModelProvider(activity).get(SpellbookViewModel.class);
        final LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        viewModel.currentSpell().observe(lifecycleOwner, this::updateSpell);
        viewModel.getUseExpanded().observe(lifecycleOwner, this::updateUseExpanded);
        setupButtons();
        //setupSwipe();
        handleArguments();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // For handling rotations
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FAVORITE_KEY, binding.favoriteButton.isSet());
        outState.putBoolean(PREPARED_KEY, binding.preparedButton.isSet());
        outState.putBoolean(KNOWN_KEY, binding.knownButton.isSet());
    }

    void updateSpell(Spell spell) {
        if (spell == null) { return; }
        binding.setSpell(spell);
        updateFromStatus(viewModel.getSpellStatus(spell));
        binding.executePendingBindings();
    }

    void updateUseExpanded(boolean useExpanded) {
        binding.setUseExpanded(useExpanded);
        binding.executePendingBindings();
    }

    private void setupButtons() {
        binding.favoriteButton.setOnClickListener( (v) -> viewModel.updateFavorite(binding.getSpell(), binding.favoriteButton.isSet()) );
        binding.knownButton.setOnClickListener( (v) -> viewModel.updateKnown(binding.getSpell(), binding.knownButton.isSet()) );
        binding.preparedButton.setOnClickListener( (v) -> viewModel.updatePrepared(binding.getSpell(), binding.preparedButton.isSet()) );
    }

//    private void setupSwipe() {
//        final ScrollView scroll = binding.spellWindowScroll;
//        scroll.setOnTouchListener(new OnSwipeTouchListener(requireContext()) {
//
//            @Override
//            public void onSwipeRight() {
//                handler.handleSpellWindowClose();
//            }
//        });
//    }

    private void updateFromStatus(SpellStatus status) {
        if (status != null ) {
            binding.favoriteButton.set(status.favorite);
            binding.knownButton.set(status.known);
            binding.preparedButton.set(status.prepared);
        }
    }

    void updateFavorite(boolean favorite) {
        binding.favoriteButton.set(favorite);
    }
    void updateKnown(boolean known) {
        binding.knownButton.set(known);
    }
    void updatePrepared(boolean prepared) {
        binding.preparedButton.set(prepared);
    }

    Spell getSpell() {
        return binding.getSpell();
    }

    private void handleArguments() {
        final Bundle args = getArguments();
        if (args != null) {
            final SpellStatus status = args.getParcelable(SPELL_STATUS_KEY);
            if (status != null) {
                updateFromStatus(status);
            }
        }
    }

    ScrollView getScrollView() {
        return binding.spellWindowScroll;
    }

}
