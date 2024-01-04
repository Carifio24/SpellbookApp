package dnd.jon.spellbook;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONException;

import dnd.jon.spellbook.databinding.NameRowBinding;

public class CharacterAdapter extends NamedItemAdapter<CharacterAdapter.CharacterRowHolder> {

    private static final String confirmDeleteTag = "confirmDeleteCharacter";
    private static final String duplicateTag = "duplicateCharacter";
    private static final String renameTag = "changeCharacterName";

    CharacterAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity, SpellbookViewModel::currentCharacterNames);
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
                        if (itemID == R.id.options_update) {
                            final Bundle args = new Bundle();
                            args.putString(NameChangeDialog.nameKey, binding.getName());
                            final CharacterNameChangeDialog dialog = new CharacterNameChangeDialog();
                            dialog.setArguments(args);
                            dialog.show(activity.getSupportFragmentManager(), renameTag);
                        } else if (itemID == R.id.options_duplicate) {
                            final Bundle args = new Bundle();
                            args.putParcelable(CreateCharacterDialog.PROFILE_KEY, viewModel.getProfileByName(binding.getName()));
                            final CreateCharacterDialog dialog = new CreateCharacterDialog();
                            dialog.setArguments(args);
                            dialog.show(activity.getSupportFragmentManager(), duplicateTag);
                        } else if (itemID == R.id.options_delete) {
                            final Bundle args = new Bundle();
                            args.putString(DeleteCharacterDialog.NAME_KEY, binding.getName());
                            final DeleteCharacterDialog dialog = new DeleteCharacterDialog();
                            dialog.setArguments(args);
                            dialog.show(activity.getSupportFragmentManager(), confirmDeleteTag);
                        } else if (itemID == R.id.options_export) {
//                            String permissionNeeded;
//                            if (GlobalInfo.ANDROID_VERSION >= Build.VERSION_CODES.R) {
//                                permissionNeeded = Manifest.permission.MANAGE_EXTERNAL_STORAGE;
//                            } else {
//                                permissionNeeded = Manifest.permission.WRITE_EXTERNAL_STORAGE;
//                            }
//                            final int havePermission = ContextCompat.checkSelfPermission(activity, permissionNeeded);
//                            if (havePermission == android.content.pm.PackageManager.PERMISSION_GRANTED) {
//                                //TODO: Implement this saving
//                            }
                            try {
                                final CharacterProfile profile = viewModel.getProfileByName(name);
                                final String json = profile.toJSON().toString();
                                final Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, json);
                                sendIntent.setType("application/json");

                                final Intent shareIntent = Intent.createChooser(sendIntent, null);
                                activity.startActivity(shareIntent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else if (itemID == R.id.options_copy) {
                            final CharacterProfile profile = viewModel.getProfileByName(name);
                            final Context context = v.getContext();
                            final ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            String message;
                            try {
                                final String json = profile.toJSON().toString();
                                final ClipData clipData = ClipData.newPlainText(name + " JSON", json);
                                clipboardManager.setPrimaryClip(clipData);
                                message = context.getString(R.string.profile_json_copied, name);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                message = context.getString(R.string.error_copying_profile_json, name);
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    });
                    popupMenu.show();
                });

                // Set the listener for the label
                binding.nameLabel.setOnClickListener((v) -> {
                    final String charName = binding.getName();
                    viewModel.setProfileByName(charName);

                    // Show a Toast message after selection
                    Toast.makeText(activity, activity.getString(R.string.character_selected_toast, charName), Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

}
