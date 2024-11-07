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
    private final NamedItemEventHandler handler;

    SourceAdapter(FragmentActivity fragmentActivity, NamedItemEventHandler handler) {
        super(fragmentActivity, SpellbookViewModel::currentCreatedSourceNames);
        this.handler = handler;
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
                        final String sourceName = binding.getName();
                        if (itemID == R.id.options_update) {
                            handler.onUpdateEvent(sourceName);
                        } else if (itemID == R.id.options_duplicate) {
                            handler.onDuplicateEvent(sourceName);
                        } else if (itemID == R.id.options_delete) {
                            handler.onDeleteEvent(sourceName);
                        } else if (itemID == R.id.options_export) {
                            handler.onExportEvent(sourceName);
                        } else if (itemID == R.id.options_copy) {
                            handler.onCopyEvent(sourceName);
                        }
                        return false;
                    });
                    popupMenu.show();
                });

                // Set the listener for the label
                binding.nameLabel.setOnClickListener((v) -> handler.onSelectionEvent(binding.getName()));
            }
        }
    }
}
