package dnd.jon.spellbook;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dnd.jon.spellbook.databinding.NameRowBinding;

public class SourceAdapter extends NamedItemAdapter<SourceAdapter.SourceRowHolder> {

    private static final String confirmDeleteTag = "confirmDeleteSource";
    private static final String duplicateTag = "duplicateSource";

    SourceAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity, SpellbookViewModel::currentCreatedSourceNames);
    }

    @NonNull
    public SourceRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final NameRowBinding binding = NameRowBinding.inflate(inflater, parent, false);
       return new SourceRowHolder(binding);
    }


    public class SourceRowHolder extends NameRowHolder {
        SourceRowHolder(NameRowBinding binding) {
            super(binding);
        }

        public void bind(String name) {
            super.bind(name);

            if (name != null) {
                binding.optionsButton.setOnClickListener((View v) -> {
                    final PopupMenu popupMenu = new PopupMenu(activity, binding.optionsButton);
                    popupMenu.inflate(R.menu.options_menu);

                    // TODO: Do we want to allow renaming sources?
                    // Would also need to rename codes as well
                    // Should do this, but let's worry about that later
                    final int[] idsToHide = {
                            R.id.options_rename,
                            R.id.options_copy
                    };
                    for (int menuID : idsToHide) {
                        final MenuItem item = popupMenu.getMenu().findItem(menuID);
                        if (item != null) {
                            item.setVisible(false);
                        }
                    }
                    popupMenu.setOnMenuItemClickListener((menuItem) -> {
                        final int itemID = menuItem.getItemId();
                        if (itemID == R.id.options_duplicate) {
                            final Bundle args = new Bundle();
                            args.putString(SourceCreationDialog.NAME_KEY, binding.getName());
                            final SourceCreationDialog dialog = new SourceCreationDialog();
                            dialog.setArguments(args);
                            dialog.show(activity.getSupportFragmentManager(), duplicateTag);
                        } else if (itemID == R.id.options_delete) {
                            final Bundle args = new Bundle();
                            args.putString(DeleteSourceDialog.NAME_KEY,  binding.getName());
                            final DeleteSourceDialog dialog = new DeleteSourceDialog();
                            dialog.setArguments(args);
                            dialog.show(activity.getSupportFragmentManager(), confirmDeleteTag);
                        } else if (itemID == R.id.options_export) {
                            try {
                                final Source source = viewModel.getCreatedSourceByName(name);
                                final String json = source.toJSON().toString();
                                final Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, json);
                                sendIntent.setType("application/json");

                                final Intent shareIntent = Intent.createChooser(sendIntent, null);
                                activity.startActivity(shareIntent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        return false;
                    });
                });
            }
        }
    }
}
