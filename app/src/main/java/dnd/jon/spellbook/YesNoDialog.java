package dnd.jon.spellbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import dnd.jon.spellbook.databinding.YesNoBinding;

public class YesNoDialog extends DialogFragment {
    private final String title;
    private final String message;
    private final Runnable onYes;
    private final Runnable onNo;

    public YesNoDialog(String title, String message, Runnable onYes, Runnable onNo) {
        this.title = title;
        this.message = message;
        this.onYes = onYes;
        this.onNo = onNo;
    }

    public YesNoDialog(String title, String message, Runnable onYes) {
        this(title, message, onYes, null);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        final Activity activity = requireActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final YesNoBinding binding = YesNoBinding.inflate(activity.getLayoutInflater());
        binding.yesButton.setOnClickListener((v) -> {
            if (onYes != null) {
                onYes.run();
            }
            dismiss();
        });
        binding.noButton.setOnClickListener((v) -> {
            if (onNo != null) {
                onNo.run();
            }
            dismiss();
        });
        binding.yesNoTitle.setText(title);
        binding.yesNoMessage.setText(message);

        builder.setView(binding.getRoot());
        return builder.create();
    }
}
