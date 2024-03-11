package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

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

    @Override
    public void onStart() {
        super.onStart();

        final Dialog dialog = getDialog();
        if (dialog == null) {
            return;
        }

        // Set the maximum height of the dialog to be 80% of the screen height
        final FragmentActivity activity = requireActivity();
        final DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        final Window window = dialog.getWindow();
        params.copyFrom(window.getAttributes());
        params.height = (int) Math.min(params.height, (int) metrics.heightPixels * 0.87f);
        params.width = (int) Math.min(params.width , (int) metrics.widthPixels * 0.9f);
        window.setAttributes(params);
    }

}
