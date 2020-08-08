package dnd.jon.spellbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import dnd.jon.spellbook.databinding.CreationManagementBinding;

public class CreationManagementFragment extends Fragment {

    private CreationManagementBinding binding;
    private SpellbookViewModel spellbookViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = CreationManagementBinding.inflate(inflater);
        spellbookViewModel = new ViewModelProvider(requireActivity(),new SpellbookViewModelFactory(requireActivity().getApplication())).get(SpellbookViewModel.class);

    }



}
