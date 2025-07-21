package dnd.jon.spellbook;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import java.util.function.BiConsumer;

import dnd.jon.spellbook.databinding.SpellWindowBinding;

public class SpellWindowFragment extends SpellbookFragment<SpellWindowBinding>
                                 implements SharedPreferences.OnSharedPreferenceChangeListener
{

    static final String SPELL_KEY = "spell";
    static final String USE_EXPANDED_KEY = "use_expanded";
    static final String SPELL_STATUS_KEY = "spell_status";
    static final String CAST_SPELL_TAG = "cast_spell";
    static final String USE_NEXT_AVAILABLE_TAG = "use_next_available_tag";
    static final String defaultTextSizeString = Integer.toString(14);

    private SpellStatus spellStatus;
    private boolean onTablet;

    public SpellWindowFragment() {
        super(R.layout.spell_window);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    private void inflateBinding(LayoutInflater inflater) {
        if (binding == null) {
            binding = SpellWindowBinding.inflate(inflater);
        }
    }

    private void inflateBinding() {
        final LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflateBinding(inflater);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        inflateBinding(inflater);

        final View rootView = binding.getRoot();

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, windowInsets) -> {
            final Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            final boolean onTablet = getResources().getBoolean(R.bool.isTablet);
            if (!onTablet) {
                final ScrollView.LayoutParams params = (ScrollView.LayoutParams) binding.spellWindowScroll.getLayoutParams();
                params.topMargin = insets.top + 10;
            }

            return WindowInsetsCompat.CONSUMED;
        });

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        viewModel.currentSpell().observe(lifecycleOwner, this::updateSpell);
        viewModel.currentUseExpanded().observe(lifecycleOwner, this::updateUseExpanded);

        final FragmentActivity activity = requireActivity();
        PreferenceManager.getDefaultSharedPreferences(activity).registerOnSharedPreferenceChangeListener(this);

        onTablet = activity.getResources().getBoolean(R.bool.isTablet);

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
        //binding.getRoot().setVisibility(spell == null ? View.GONE : View.VISIBLE);
        binding.setUseExpanded(useExpanded);
        binding.setTextSize(textSize);
        binding.setTextColor(textColor);
        binding.setContext(viewModel.getSpellContext());
        binding.executePendingBindings();

        viewModel.currentSpellFavoriteLD().observe(lifecycleOwner, binding.favoriteButton::set);
        viewModel.currentSpellPreparedLD().observe(lifecycleOwner, binding.preparedButton::set);
        viewModel.currentSpellKnownLD().observe(lifecycleOwner, binding.knownButton::set);
        viewModel.currentSpellsContext().observe(lifecycleOwner, binding::setContext);

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
    }

    // For handling rotations
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SPELL_KEY, viewModel.currentSpell().getValue());
        outState.putBoolean(USE_EXPANDED_KEY, SpellbookUtils.coalesce(viewModel.currentUseExpanded().getValue(), false));
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //if (!hidden) {

            inflateBinding();
            final int visibility = hidden || getSpell() == null ? View.GONE : View.VISIBLE;
            updateViewVisibilities(visibility);

        //}
    }

    public void updateViewVisibilities(int visibility) {
        // For some reason, we need to explicitly set the visibility of every text view
        // inside the spell window constraint layout
        // TODO: Figure out why this is the case
        binding.spellWindowConstraint.setVisibility(visibility);
        final int count = binding.spellWindowConstraint.getChildCount();
        for (int i = 0; i < count; i++) {
            binding.spellWindowConstraint.getChildAt(i).setVisibility(visibility);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        final int visibility = getSpell() == null ? View.GONE : View.VISIBLE;
        binding.getRoot().setVisibility(visibility);
    }

    @Override
    public void onResume() {
        super.onResume();
        final int visibility = getSpell() == null ? View.GONE : View.VISIBLE;
        binding.getRoot().setVisibility(visibility);
    }

    void updateSpell(Spell spell, boolean forceHide) {
        if (binding == null) { return; }
        final int visibility = forceHide || spell == null ? View.GONE : View.VISIBLE;
        updateViewVisibilities(visibility);
        if (spell == null) { return; }
        binding.setSpell(spell);
        spellStatus = viewModel.getSpellStatus(spell);
        updateFromStatus();
        binding.executePendingBindings();
    }

    void updateSpell(Spell spell) { updateSpell(spell, false); }

    void updateUseExpanded(boolean useExpanded) {
        binding.setUseExpanded(useExpanded);
        binding.executePendingBindings();
    }

    private void setupButtons() {
        binding.favoriteButton.setOnClickListener( (v) -> buttonListener(viewModel::setFavorite, binding.favoriteButton) );
        binding.knownButton.setOnClickListener( (v) -> buttonListener(viewModel::setKnown, binding.knownButton) );
        binding.preparedButton.setOnClickListener( (v) -> buttonListener(viewModel::setPrepared, binding.preparedButton) );
        binding.castButton.setOnClickListener( (v) -> onCastClicked() );
    }

    private void buttonListener(BiConsumer<Spell,Boolean> setter,
                                ToggleButton button) {
        setter.accept(binding.getSpell(), button.isSet());

        // TODO: Is there a cleaner way to do this?
        if (onTablet) {
            viewModel.setFilterNeeded();
        }
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

    void onCastClicked() {
        final FragmentActivity activity = requireActivity();
        final Spell spell = getSpell();
        final int level = spell.getLevel();
        final SpellSlotStatus status = viewModel.getSpellSlotStatus();
        if (level > status.maxLevelWithAvailableSlots()) {
            viewModel.castSpell(spell);
        } else if (!spell.getHigherLevel().isEmpty() && level == status.maxLevelWithAvailableSlots()) {
            viewModel.castSpell(spell, level);
        } else if (spell.getHigherLevel().isEmpty()) {
            if (status.getAvailableSlots(level) == 0 && status.maxLevelWithAvailableSlots() > level) {
                final int levelToUse = status.nextAvailableSlotLevel(level);
                final Bundle args = new Bundle();
                args.putInt(ConfirmNextAvailableCastDialog.LEVEL_KEY, levelToUse);
                args.putParcelable(ConfirmNextAvailableCastDialog.SPELL_KEY, spell);
                final DialogFragment confirmCastDialog = new ConfirmNextAvailableCastDialog();
                confirmCastDialog.setArguments(args);
                confirmCastDialog.show(activity.getSupportFragmentManager(), USE_NEXT_AVAILABLE_TAG);
            } else {
                viewModel.castSpell(spell);
            }
        } else {
            final Bundle args = new Bundle();
            args.putParcelable(HigherLevelSlotDialog.SPELL_KEY, spell);
            final HigherLevelSlotDialog dialog = new HigherLevelSlotDialog();
            dialog.setArguments(args);
            dialog.show(activity.getSupportFragmentManager(), CAST_SPELL_TAG);
        }
    }

}