package dnd.jon.spellbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import dnd.jon.spellbook.databinding.SpellCreationBinding;

public final class SpellCreationFragment extends SpellbookFragment<SpellCreationBinding> {
    private static final String TAG = "SpellCreationFragment";
    private SpellCreationHandler handler;

    public SpellCreationFragment() {
        super(R.layout.spell_creation);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = SpellCreationBinding.inflate(inflater);
        handler = new SpellCreationHandler(requireActivity(), binding, TAG);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handler.setup();
    }

    @Override
    public void onStop() {
        viewModel.setCurrentEditingSpell(null);
        super.onStop();
    }

}
