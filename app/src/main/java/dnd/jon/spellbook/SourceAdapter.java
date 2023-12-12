package dnd.jon.spellbook;

import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dnd.jon.spellbook.databinding.NameRowBinding;

public class SourceAdapter extends NamedItemAdapter<SourceAdapter> {

    SourceAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity, SpellbookViewModel::currentCreatedSourceNames);
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
                    popupMenu.setOnMenuItemClickListener((menuItem) -> {
                        final int itemID = menuItem.getItemId();
                        if (itemID == R.id.options_rename) {
                            final Bundle args = new Bundle();

                        } else if (itemID == R.id.options_duplicate) {
                            final Bundle args = new Bundle();
                            args.putString(SourceCreationDialog.NAME_KEY, binding.getName());
                            final SourceCreationDialog dialog = new SourceCreationDialog();
                            dialog.setArguments(args);
                            dialog.show(activity.getSupportFragmentManager(), "duplicateSource");
                        }
                    });
                });
            }
        }
    }
}
