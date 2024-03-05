package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import dnd.jon.spellbook.databinding.HomebrewInformationBinding;

public class HomebrewInformationDialog extends DialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        final FragmentActivity activity = requireActivity();
        final HomebrewInformationBinding binding = HomebrewInformationBinding.inflate(activity.getLayoutInflater());

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(binding.getRoot());
        return builder.create();
    }
}
