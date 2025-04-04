package dnd.jon.spellbook;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewbinding.ViewBinding;

public abstract class SpellbookFragment<VB extends ViewBinding> extends Fragment {

    Context context;
    VB binding;
    SpellbookViewModel viewModel;

    SpellbookFragment() { super(); }
    SpellbookFragment(int layoutID) { super(layoutID); }

    void acquireViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(SpellbookViewModel.class);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        acquireViewModel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewModel == null) {
            acquireViewModel();
        }
    }

}