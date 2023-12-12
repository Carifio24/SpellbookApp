package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import dnd.jon.spellbook.databinding.SourceManagementBinding;

public class SourceManagementDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        final FragmentActivity activity = requireActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final LayoutInflater inflater = getLayoutInflater();
        final SourceManagementBinding binding = SourceManagementBinding.inflate(inflater);
        builder.setView(binding.getRoot());

        return builder.create();
    }

}

