package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.function.BiFunction;

import dnd.jon.spellbook.databinding.NameChangeBinding;

class NameChangeDialog<T extends Named> extends DialogFragment {

    private FragmentActivity activity;
    private String originalName;
    private EditText editText;
    private NameChangeBinding binding;
    private SpellbookViewModel viewModel;

    private final BiFunction<SpellbookViewModel,String,String> nameValidator;
    private final BiFunction<SpellbookViewModel,String,Boolean> deleteByName;
    private final BiFunction<SpellbookViewModel,String,T> nameGetter;
    private final BiFunction<SpellbookViewModel,T,Boolean> saver;
    private final int itemTypeID;

    static final String nameKey = "name";

    NameChangeDialog(BiFunction<SpellbookViewModel,String,String> nameValidator,
                     BiFunction<SpellbookViewModel,String,Boolean> deleteByName,
                     BiFunction<SpellbookViewModel,String,T> nameGetter,
                     BiFunction<SpellbookViewModel,T,Boolean> saver,
                     int itemTypeID
    ) {
        this.nameValidator = nameValidator;
        this.deleteByName = deleteByName;
        this.nameGetter = nameGetter;
        this.saver = saver;
        this.itemTypeID = itemTypeID;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        // The character name
        final Bundle args = getArguments();
        if (args != null) {
            originalName = args.getString(nameKey);
        } else {
            // TODO: Add a Toast error message
            this.dismiss();
        }

        // The activity and the view model
        activity = requireActivity();
        viewModel = new ViewModelProvider(activity, activity.getDefaultViewModelProviderFactory())
                .get(SpellbookViewModel.class);

        // Create the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Inflate the view and set the builder to use this view
        binding = NameChangeBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        // We want to start with the original name in the text field
        // When it gets focus, everything is selected
        editText = binding.nameChangeEditText;
        editText.setText(originalName);
        editText.setSelectAllOnFocus(true);

        // Create the name change listener
        View.OnClickListener changeListener = (View v) -> {

            // Get the newly-entered name
            final String newName = editText.getText().toString();

            // Is this a valid name?
            final String error = nameValidator.apply(viewModel, newName);
            if (!error.isEmpty()) {
                setErrorMessage(error);
                return;
            }

            // If it's the same as the current name
            if (newName.equals(originalName)) {
                final String itemType = activity.getString(itemTypeID);
                setErrorMessage(activity.getString(R.string.same_name, itemType));
                return;
            }

            // Otherwise, change the character profile
            // Save the new one, and delete the old
            final T item = nameGetter.apply(viewModel, originalName);
            if (item != null) {
                item.setName(newName);
                final boolean saved = saver.apply(viewModel, item);
                final boolean deleted = saved && deleteByName.apply(viewModel, originalName);
                if (!(saved && deleted)) {
                    Toast.makeText(activity, activity.getString(R.string.name_change_error), Toast.LENGTH_SHORT).show();
                }
            } else {
                // TODO: Toast error message
            }
            this.dismiss();

        };

        // Create the cancel listener
        View.OnClickListener cancelListener = (View view) -> this.dismiss();

        // Set the button listeners
        binding.nameChangeApproveButton.setOnClickListener(changeListener);
        binding.nameChangeCancelButton.setOnClickListener(cancelListener);

        // Return the dialog
        return builder.create();

    }

    private void setErrorMessage(String error) {
        final TextView tv = binding.nameChangeMessage;
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        tv.setTextColor(Color.RED);
        tv.setText(error);
    }

}

class CharacterNameChangeDialog extends NameChangeDialog<CharacterProfile> {

    CharacterNameChangeDialog() {
        super(SpellbookViewModel::characterNameValidator,
              SpellbookViewModel::deleteProfileByName,
              SpellbookViewModel::getProfileByName,
              SpellbookViewModel::saveProfile,
              R.string.name_lowercase);
    }

}

class StatusNameChangeDialog extends NameChangeDialog<SortFilterStatus> {

    StatusNameChangeDialog() {
        super(SpellbookViewModel::statusNameValidator,
              SpellbookViewModel::deleteSortFilterStatusByName,
              SpellbookViewModel::getSortFilterStatusByName,
              SpellbookViewModel::saveSortFilterStatus,
              R.string.status_lowercase);
    }

}
