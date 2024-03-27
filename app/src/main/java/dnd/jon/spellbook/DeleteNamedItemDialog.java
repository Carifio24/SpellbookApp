package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.function.BiFunction;

import dnd.jon.spellbook.databinding.YesNoBinding;

class DeleteNamedItemDialog extends DialogFragment {

    private String name;
    private FragmentActivity activity;
    private SpellbookViewModel viewModel;
    private final int typeNameID;
    private final BiFunction<SpellbookViewModel, String, Boolean> deleter;
    private Runnable onCancel = null;
    private Runnable onConfirm = null;

    static final String NAME_KEY = "name";

    public DeleteNamedItemDialog(int typeNameID,
                                 BiFunction<SpellbookViewModel, String, Boolean> deleter) {
        this.typeNameID = typeNameID;
        this.deleter = deleter;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // The character name
        name = getArguments() != null ? getArguments().getString(NAME_KEY) : "";

        // The activity and view model
        activity = requireActivity();
        viewModel = new ViewModelProvider(activity, activity.getDefaultViewModelProviderFactory())
                .get(SpellbookViewModel.class);

        // Create the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Inflate the view and set the builder to use this view
        final YesNoBinding binding = YesNoBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        // Set the title
        final TextView title = binding.yesNoTitle;
        final String titleText = activity.getString(R.string.confirm);
        title.setText(titleText);

        // Set the message
        final TextView message = binding.yesNoMessage;
        final String messageText = activity.getString(R.string.delete_item_confirm, name);
        message.setText(messageText);

        // The listener to delete; for the yes button
        final View.OnClickListener yesListener = (v) -> {
            final boolean deleted = deleter.apply(viewModel, name);
            String toastMessage;
            if (deleted) {
                final String typeName = activity.getString(typeNameID);
                toastMessage = activity.getString(R.string.item_deleted, typeName, name);
            } else {
                toastMessage = activity.getString(R.string.error_deleting, name);
            }
            Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show();
            this.dismiss();
            if (this.onConfirm != null) {
                this.onConfirm.run();
            }
        };

        // The listener to cancel; for the no button
        final View.OnClickListener noListener = (v) -> {
            if (this.onCancel != null) {
                this.onCancel.run();
            }
            this.dismiss();
        };

        // Set the button listeners
        binding.yesButton.setOnClickListener(yesListener);
        binding.noButton.setOnClickListener(noListener);

        // Return the dialog
        return builder.create();
    }

    void setOnCancel(Runnable runnable) {
        this.onCancel = runnable;
    }

    void setOnConfirm(Runnable runnable) {
        this.onConfirm = runnable;
    }

    SpellbookViewModel getViewModel() {
        return viewModel;
    }
}

class DeleteStatusDialog extends DeleteNamedItemDialog {
    public DeleteStatusDialog() {
        super(R.string.configuration, SpellbookViewModel::deleteSortFilterStatusByName);
    }
}
