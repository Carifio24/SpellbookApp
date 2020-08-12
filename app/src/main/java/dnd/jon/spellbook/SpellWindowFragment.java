package dnd.jon.spellbook;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import dnd.jon.spellbook.databinding.SpellWindowBinding;

public class SpellWindowFragment extends Fragment {

    private SpellWindowBinding binding;
    private SpellbookViewModel spellbookViewModel;
    private LifecycleOwner lifecycleOwner;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { close(); }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SpellWindowBinding.inflate(inflater);
        spellbookViewModel = new ViewModelProvider(requireActivity(),new SpellbookViewModelFactory(requireActivity().getApplication())).get(SpellbookViewModel.class);

        lifecycleOwner = getViewLifecycleOwner();

        // Set up the buttons
        setUpButtons();

        System.out.println("Creating view");

        // Set the current spell when it changes
        spellbookViewModel.getCurrentSpell().observe(getViewLifecycleOwner(), this::setSpell);

        // Dismiss on a swipe to the right, if we're not on a tablet
        // This listener is set on the ScrollView rather than the root view because the scroll view fills the entire fragment
        // If we set it on the root view, it will never trigger because the ScrollView is blocking it
        final ScrollView sv = binding.spellWindowScroll;
        sv.setOnTouchListener(new OnSwipeTouchListener(requireActivity()) {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                System.out.println("onTouch");
                v.performClick();
                return super.onTouch(v, event);
            }

            @Override
            public void onSwipeRight() {
                System.out.println("Swipe right detected");
                if (!spellbookViewModel.areOnTablet()) {
                    close();
                }
            }
        });


        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void setSpell(Spell spell) {
        binding.setSpell(spell);
        binding.executePendingBindings();
        BindingAdapterUtils.promptFormat(binding.spellLocation, getString(R.string.location), spellbookViewModel.getLocationString(spell));
    }

    private void setUpButtons() {

        // The favorite button
        binding.favoriteButton.setOnClickListener( (v) -> spellbookViewModel.toggleFavorite(binding.getSpell()));
        spellbookViewModel.isCurrentSpellFavorite().observe(lifecycleOwner, (b) -> binding.favoriteButton.set(b));

        // The known button
        binding.knownButton.setOnClickListener( (v) -> spellbookViewModel.toggleKnown(binding.getSpell()));
        spellbookViewModel.isCurrentSpellKnown().observe(lifecycleOwner, (b) -> binding.knownButton.set(b));

        // The prepared button
        binding.preparedButton.setOnClickListener( (v) -> spellbookViewModel.togglePrepared(binding.getSpell()));
        spellbookViewModel.isCurrentSpellPrepared().observe(lifecycleOwner, (b) -> binding.preparedButton.set(b));

    }

    // Close the spell window fragment
    // Only for use on a phone
    void close() {
        requireActivity().getSupportFragmentManager().popBackStack("spell_window", FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
