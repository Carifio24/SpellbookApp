package dnd.jon.spellbook;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import dnd.jon.spellbook.databinding.SpellWindowBinding;

public class SpellWindowFragment extends Fragment
                                 implements SharedPreferences.OnSharedPreferenceChangeListener
{

    static final String SPELL_KEY = "spell";
    //static final String TEXT_SIZE_KEY = "textSize";
    //static final String FAVORITE_KEY = "favorite";
    //static final String KNOWN_KEY = "known";
    //static final String PREPARED_KEY = "prepared";
    static final String USE_EXPANDED_KEY = "use_expanded";
    static final String SPELL_STATUS_KEY = "spell_status";
    static final String defaultTextSizeString = Integer.toString(14);

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

        PreferenceManager.getDefaultSharedPreferences(activity).registerOnSharedPreferenceChangeListener(this);

        final Bundle args = getArguments();
        Spell spell = null;
        boolean useExpanded = false;
        if (args != null) {
            spell = args.getParcelable(SPELL_KEY);
            useExpanded = args.getBoolean(USE_EXPANDED_KEY, false);
            spellStatus = viewModel.getSpellStatus(spell);
            updateFromStatus();
        }

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        final String fontSizeKey = getString(R.string.text_font_size);
        final String textSizeString = preferences.getString(fontSizeKey, defaultTextSizeString);
        final String textColorKey = getString(R.string.text_color);
        final int textColor = preferences.getInt(textColorKey, SpellbookUtils.defaultColor);
        final int textSize = Integer.parseInt(textSizeString);

        binding.setSpell(spell);
        binding.spellWindowConstraint.setVisibility(spell == null ? View.GONE : View.VISIBLE);
        binding.setUseExpanded(useExpanded);
        binding.executePendingBindings();
        binding.setTextSize(textSize);
        binding.setTextColor(textColor);

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
        if (binding == null) { return; }
        binding.spellWindowConstraint.setVisibility(spell == null ? View.GONE : View.VISIBLE);
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

    private void changeTextSize(float size) {
        if (binding == null) { return; }
//        final ConstraintLayout layout = binding.spellWindowInnerConstraint;

        // We would have to do something recursive if we had any nested TextViews
        // But, we don't, so this is good enough until that changes
//        for (int i = 0; i < layout.getChildCount(); i++) {
//            final View view = layout.getChildAt(i);
//            if (view instanceof TextView) {
//                final TextView tv = (TextView) view;
//                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
//            }
//        }
        System.out.println("Changing binding size");
        binding.setTextSize(size);
    }

    private void changeTextColor(int color) {
        if (binding == null) { return; }
        binding.setTextColor(color);
    }

    Spell getSpell() {
        return binding.getSpell();
    }

    ScrollView getScrollView() {
        return binding != null ? binding.spellWindowScroll : null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getActivity() == null) { return; }
        if (key.equals(getString(R.string.spell_text_font_size))) {
            final String sizeString = sharedPreferences.getString(key, "14");
            int size;
            try {
                size = Integer.parseInt(sizeString);
            } catch (NumberFormatException exc) {
                size = 14;
            }
            changeTextSize(size);
        } else if (key.equals(getString(R.string.text_color))) {
            final int color = sharedPreferences.getInt(key, SpellbookUtils.defaultColor);
            changeTextColor(color);
        }
    }

    void setBackground(Drawable drawable) {
        binding.spellWindowConstraint.setBackground(drawable);
    }
}
