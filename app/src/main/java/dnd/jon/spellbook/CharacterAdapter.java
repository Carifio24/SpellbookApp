package dnd.jon.spellbook;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import dnd.jon.spellbook.databinding.NameRowBinding;

public class CharacterAdapter extends NamedItemAdapter<CharacterAdapter.CharacterRowHolder> {

    private final NamedItemEventHandler handler;
    CharacterAdapter(FragmentActivity fragmentActivity, NamedItemEventHandler handler) {
        super(fragmentActivity, SpellbookViewModel::currentCharacterNames);
        this.handler = handler;
    }

    // ViewHolder methods
    @NonNull
    public CharacterRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final NameRowBinding binding = NameRowBinding.inflate(inflater, parent, false);
        return new CharacterRowHolder(binding);
    }

    // The RowHolder class
    public class CharacterRowHolder extends NameRowHolder {

        CharacterRowHolder(NameRowBinding binding) { super(binding); }

        public void bind(String name) {
            super.bind(name);

            // Set the buttons to have the appropriate effects
            if (name != null) {

                // Set the listener for the options button
                binding.optionsButton.setOnClickListener((View v) -> {
                    final PopupMenu popupMenu = new PopupMenu(activity, binding.optionsButton);
                    popupMenu.inflate(R.menu.options_menu);
                    final MenuItem updateItem = popupMenu.getMenu().findItem(R.id.options_update);
                    if (updateItem != null) {
                        updateItem.setTitle(R.string.rename);
                    }

                    popupMenu.setOnMenuItemClickListener((menuItem) -> {
                        final int itemID = menuItem.getItemId();
                        final String characterName = binding.getName();
                        if (itemID == R.id.options_update) {
                            handler.onUpdateEvent(characterName);
                        } else if (itemID == R.id.options_duplicate) {
                            handler.onDuplicateEvent(characterName);
                        } else if (itemID == R.id.options_delete) {
                            handler.onDeleteEvent(characterName);
                        } else if (itemID == R.id.options_export) {
                            handler.onExportEvent(characterName);
                        } else if (itemID == R.id.options_copy) {
                            handler.onCopyEvent(characterName);
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
