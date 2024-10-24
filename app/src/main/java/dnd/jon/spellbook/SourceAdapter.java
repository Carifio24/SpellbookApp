package dnd.jon.spellbook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONException;

import java.util.Collection;

import dnd.jon.spellbook.databinding.NameRowBinding;

public class SourceAdapter extends NamedItemAdapter<SourceAdapter.SourceRowHolder> {
    static private final String TAG = "SOURCE_ADAPTER";

    private static final String confirmDeleteTag = "confirmDeleteSource";
    private static final String duplicateTag = "duplicateSource";
    private static final String updateSourceTag = "updateSource";

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

                    // "Duplicate" is kind of a pointless option
                    // for an item with two text fields, neither of which
                    // one would really want to reuse
                    final MenuItem duplicateItem = popupMenu.getMenu().findItem(R.id.options_duplicate);
                    if (duplicateItem != null) {
                        duplicateItem.setVisible(false);
                    }

                    popupMenu.setOnMenuItemClickListener((menuItem) -> {
                        final int itemID = menuItem.getItemId();
                        if (itemID == R.id.options_update) {
                            final Bundle args = new Bundle();
                            args.putString(SourceCreationDialog.NAME_KEY, binding.getName());
                            final SourceCreationDialog dialog = new SourceCreationDialog();
                            dialog.setArguments(args);
                            dialog.show(activity.getSupportFragmentManager(), updateSourceTag);

                        // In case the duplicate option somehow is displayed,
                        // we may as well do something sensible
                        } else if (itemID == R.id.options_duplicate) {
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
                                final Collection<Spell> spells = viewModel.getCreatedSpellsForSource(source);
                                final String json = JSONUtils.asJSON(source, activity, spells).toString();
                                final Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, json);
                                sendIntent.setType("application/json");

                                final Intent shareIntent = Intent.createChooser(sendIntent, null);
                                activity.startActivity(shareIntent);
                            } catch (JSONException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        } else if (itemID == R.id.options_copy) {
                            final Context context = v.getContext();
                            String message;
                            try {
                                final Source source = viewModel.getCreatedSourceByName(name);
                                final Collection<Spell> spells = viewModel.getCreatedSpellsForSource(source);
                                final String json = JSONUtils.asJSON(source, activity, spells).toString();
                                final String jsonString = json.toString();
                                final String label = name + " JSON";
                                AndroidUtils.copyToClipboard(context, jsonString, label);
                                message = context.getString(R.string.item_json_copied, name);
                            } catch (JSONException e) {
                                Log.e(TAG, e.getMessage());
                                message = context.getString(R.string.json_import_error);
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    });
                    popupMenu.show();
                });

                // Set the listener for the label
                binding.nameLabel.setOnClickListener((v) -> {
                    final String sourceName = binding.getName();
                    final SourceCreationDialog dialog = new SourceCreationDialog();
                    final Bundle args = new Bundle();
                    args.putString(SourceCreationDialog.NAME_KEY, sourceName);
                    dialog.setArguments(args);
                    dialog.show(activity.getSupportFragmentManager(), updateSourceTag);
                });
            }
        }
    }
}
