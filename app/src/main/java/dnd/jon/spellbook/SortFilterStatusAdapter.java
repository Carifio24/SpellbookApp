package dnd.jon.spellbook;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import dnd.jon.spellbook.databinding.NameRowBinding;

public class SortFilterStatusAdapter extends NamedItemAdapter<SortFilterStatusAdapter.SortFilterStatusRowHolder> {

    SortFilterStatusAdapter(FragmentActivity activity) {
        super(activity, SpellbookViewModel::currentStatusNames);
    }

    @NonNull
    @Override
    public SortFilterStatusAdapter.SortFilterStatusRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final NameRowBinding binding = NameRowBinding.inflate(inflater, parent, false);
        return new SortFilterStatusRowHolder(binding);
    }

    // The RowHolder class
    public class SortFilterStatusRowHolder extends NameRowHolder {

        public SortFilterStatusRowHolder(NameRowBinding binding) { super(binding); }

        public void bind(String name) {
            super.bind(name);

            // Set the buttons to have the appropriate effects
            if (name != null) {

                // Set the listener for the options button
                binding.optionsButton.setOnClickListener((v) -> {
                    final PopupMenu popupMenu = new PopupMenu(activity, binding.optionsButton);
                    popupMenu.inflate(R.menu.options_menu);
                    popupMenu.setOnMenuItemClickListener((menuItem) -> {
                        final int itemID = menuItem.getItemId();
                        if (itemID == R.id.options_rename) {
                            final Bundle args = new Bundle();
                            args.putString(NameChangeDialog.nameKey, binding.getName());
                            final StatusNameChangeDialog dialog = new StatusNameChangeDialog();
                            dialog.setArguments(args);
                            dialog.show(activity.getSupportFragmentManager(), "changeSortFilterStatusName");
                        } else if (itemID == R.id.options_duplicate) {
                            final Bundle args = new Bundle();
                            args.putParcelable(SaveSortFilterStatusDialog.STATUS_KEY, viewModel.getSortFilterStatusByName(binding.getName()));
                            final SaveSortFilterStatusDialog dialog = new SaveSortFilterStatusDialog();
                            dialog.setArguments(args);
                            dialog.show(activity.getSupportFragmentManager(), "duplicateSortFilterStatus");
                        } else if (itemID == R.id.options_delete) {
                            final Bundle args = new Bundle();
                            args.putString(DeleteStatusDialog.NAME_KEY, binding.getName());
                            final DeleteStatusDialog dialog = new DeleteStatusDialog();
                            dialog.setArguments(args);
                            dialog.show(activity.getSupportFragmentManager(), "confirmDeleteStatus");
                        } else if (itemID == R.id.options_export) {
                            String permissionNeeded;
                            if (GlobalInfo.ANDROID_VERSION >= Build.VERSION_CODES.R) {
                                permissionNeeded = Manifest.permission.MANAGE_EXTERNAL_STORAGE;
                            } else {
                                permissionNeeded = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                            }
                            final int havePermission = ContextCompat.checkSelfPermission(activity, permissionNeeded);
                            if (havePermission == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                //TODO: Implement this saving
                            }
                        }
                        return false;
                    });
                    popupMenu.show();
                });

                // Set the listener for the label
               binding.nameLabel.setOnClickListener((v) -> {
                   final String statusName = binding.getName();
                    viewModel.setSortFilterStatusByName(statusName);

                   // Show a Toast message after selection
                   Toast.makeText(activity, activity.getString(R.string.status_selected_toast, statusName), Toast.LENGTH_SHORT).show();
               });
            }
        }
    }
}
