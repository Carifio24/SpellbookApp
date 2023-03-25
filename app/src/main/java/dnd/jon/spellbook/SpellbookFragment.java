package dnd.jon.spellbook;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

public abstract class SpellbookFragment<VB extends ViewBinding> extends Fragment {

    Context context;
    VB binding;
    SpellbookViewModel viewModel;

    SpellbookFragment() { super(); }
    SpellbookFragment(int layoutID) { super(layoutID); }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        final FragmentActivity activity = requireActivity();
        viewModel = new ViewModelProvider(activity).get(SpellbookViewModel.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}