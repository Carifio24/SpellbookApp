package dnd.jon.spellbook;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import dnd.jon.spellbook.databinding.SourceCreationBinding;

public class SourceCreationDialog extends DialogFragment {

    private static final String SOURCE_KEY = "source";
    static final String NAME_KEY = "name";
    private static final String ABBREVIATION_KEY = "abbreviation";

    private Source baseSource;
    private SourceCreationBinding binding;
    private SpellbookViewModel viewModel;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final FragmentActivity activity = requireActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        viewModel = new ViewModelProvider(activity).get(SpellbookViewModel.class);

        binding = SourceCreationBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        // For e.g. coming off a rotation
        if (savedInstanceState != null) {
            final String name = savedInstanceState.getString(NAME_KEY, "");
            final String abbreviation = savedInstanceState.getString(ABBREVIATION_KEY, "");
            binding.nameEntry.setText(name);
            binding.abbreviationEntry.setText(abbreviation);
        }

        // For editing an existing source
        final Bundle args = getArguments();
        if (args != null) {
            final String name = args.getString(NAME_KEY);
            baseSource = viewModel.getCreatedSourceByName(name);
            if (baseSource != null) {
                binding.nameEntry.setText(DisplayUtils.getDisplayName(baseSource, activity));
                binding.abbreviationEntry.setText(DisplayUtils.getCode(baseSource, activity));

                binding.title.setText(R.string.edit_source);
                binding.createSourceButton.setText(R.string.update_source);
            }
        }

        binding.sourceCancelButton.setOnClickListener((v) -> this.dismiss());
        binding.createSourceButton.setOnClickListener((v) -> this.createSourceIfValid());

        return builder.create();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // For handling rotations
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(NAME_KEY, binding.nameEntry.getText().toString());
        outState.putString(ABBREVIATION_KEY, binding.abbreviationEntry.toString());
    }

    private void createSourceIfValid() {
        // Check that the source name and abbreviation are okay
        final String name = binding.nameEntry.getText().toString();
        final boolean checkExisting = baseSource == null;
        String error = viewModel.sourceNameValidator(name, checkExisting);
        if (!error.isEmpty()) {
            setErrorMessage(error);
            return;
        }

        final String abbreviation = binding.abbreviationEntry.getText().toString();
        error = viewModel.sourceAbbreviationValidator(abbreviation);
        if (!error.isEmpty()) {
            setErrorMessage(error);
            return;
        }

        // Create the source
        final Source source = Source.create(name, abbreviation);
        if (baseSource != null) {
            viewModel.updateSourceFile(source, baseSource.getDisplayName(), baseSource.getCode());
        } else {
            viewModel.addCreatedSource(source);
        }
        this.dismiss();
    }

    private void setErrorMessage(String error) {
        final TextView tv = binding.errorText;
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        tv.setTextColor(Color.RED);
        tv.setText(error);
    }
}