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
    private SpellStatus spellStatus;

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

        binding = SpellWindowBinding.inflate(inflater);

        final FragmentActivity activity = requireActivity();
        this.viewModel = new ViewModelProvider(activity).get(SpellbookViewModel.class);
        final LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        viewModel.currentSpell().observe(lifecycleOwner, this::updateSpell);
        viewModel.currentUseExpanded().observe(lifecycleOwner, this::updateUseExpanded);

        final Bundle args = getArguments();
        Spell spell = null;
        boolean useExpanded = false;
        if (args != null) {
            spell = args.getParcelable(SPELL_KEY);
            useExpanded = args.getBoolean(USE_EXPANDED_KEY, false);
            spellStatus = viewModel.getSpellStatus(spell);
            updateFromStatus();
        }

        binding.setSpell(spell);
        binding.setUseExpanded(useExpanded);
        binding.executePendingBindings();

        viewModel.currentSpellFavoriteLD().observe(lifecycleOwner, binding.favoriteButton::set);
        viewModel.currentSpellPreparedLD().observe(lifecycleOwner, binding.preparedButton::set);
        viewModel.currentSpellKnownLD().observe(lifecycleOwner, binding.knownButton::set);

        //        spellStatus.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
//            @Override
//            public void onPropertyChanged(Observable sender, int propertyId) {
//                if (sender != spellStatus) { return; }
//                if (propertyId == BR.favorite) {
//                    binding.favoriteButton.set(spellStatus.getFavorite());
//                } else if (propertyId == BR.prepared) {
//                    binding.preparedButton.set(spellStatus.getPrepared());
//                } else if (propertyId == BR.known) {
//                    binding.knownButton.set(spellStatus.getKnown());
//                }
//            }
//        });

        setupButtons();

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
        outState.putParcelable(SPELL_KEY, viewModel.currentSpell().getValue());
        outState.putBoolean(USE_EXPANDED_KEY, SpellbookUtils.coalesce(viewModel.currentUseExpanded().getValue(), false));
    }

    void updateSpell(Spell spell) {
        if (spell == null) { return; }
        binding.setSpell(spell);
        spellStatus = viewModel.getSpellStatus(spell);
        updateFromStatus();
        binding.executePendingBindings();
    }

    void updateUseExpanded(boolean useExpanded) {
        binding.setUseExpanded(useExpanded);
        binding.executePendingBindings();
    }

    private void setupButtons() {
        binding.favoriteButton.setOnClickListener( (v) -> viewModel.setFavorite(binding.getSpell(), binding.favoriteButton.isSet()) );
        binding.knownButton.setOnClickListener( (v) -> viewModel.setKnown(binding.getSpell(), binding.knownButton.isSet()) );
        binding.preparedButton.setOnClickListener( (v) -> viewModel.setPrepared(binding.getSpell(), binding.preparedButton.isSet()) );
    }

    private void updateFromStatus() {
        if (spellStatus != null ) {
            binding.favoriteButton.set(spellStatus.favorite);
            binding.knownButton.set(spellStatus.known);
            binding.preparedButton.set(spellStatus.prepared);
        } else {
            binding.favoriteButton.set(false);
            binding.knownButton.set(false);
            binding.preparedButton.set(false);
        }
    }

    Spell getSpell() {
        return binding.getSpell();
    }

    ScrollView getScrollView() {
        return binding != null ? binding.spellWindowScroll : null;
    }

}
