package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dnd.jon.spellbook.databinding.SortFilterStatusSelectionBinding;

public class SortFilterStatusSelectionDialog extends DialogFragment {

    private FragmentActivity activity;
    private SortFilterStatusAdapter adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Get the activity
        activity = requireActivity();

        // Create the dialog builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Inflate the view and set the builder to use this view
        final SortFilterStatusSelectionBinding binding = SortFilterStatusSelectionBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        // Set the adapter for the status table
        adapter = new SortFilterStatusAdapter(activity);
        final RecyclerView recyclerView = binding.sortFilterSelectionRecyclerView;
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        // Create the dialog and set a few options
        final AlertDialog dialog = builder.create();
        dialog.setOnCancelListener( (DialogInterface di) -> this.dismiss() );
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

}
