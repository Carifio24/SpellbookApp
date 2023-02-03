package dnd.jon.spellbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import dnd.jon.spellbook.databinding.CreationChooserBinding;

public class CreationChooserDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get the activity and create the dialog builder
        final Activity activity = requireActivity();
        final AlertDialog.Builder b = new AlertDialog.Builder(activity);

        // Get the binding and set the root view
        final CreationChooserBinding binding = CreationChooserBinding.inflate(activity.getLayoutInflater());
        b.setView(binding.getRoot());

        // Set up the button listeners
        //binding.newSourceButton.setOnClickListener((v) -> openSourceFragment());
        binding.newSpellButton.setOnClickListener((v) -> openSpellFragment());

        // Create and return the dialog
        return b.create();
    }

    private <F extends Fragment> void openFragment(Class<F> fragmentClass, String tag) {
        final FragmentActivity activity = requireActivity();
        final boolean onTablet = activity.getResources().getBoolean(R.bool.isTablet);
        final int containerID = onTablet ? R.id.tablet_full_width_container : R.id.phone_fullscreen_fragment_container;
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(containerID, fragmentClass, null, tag)
                .commit();
        activity.getSupportFragmentManager().executePendingTransactions();
    }

    private void openSpellFragment() { openFragment(SpellCreationFragment.class, "SpellCreationFragment"); }
}
