package dnd.jon.spellbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dnd.jon.spellbook.databinding.CharacterRowBinding;

public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.CharacterRowHolder>{

    // Member values
    private List<String> characterNames;
    private final SpellbookViewModel spellbookViewModel;
    private final FragmentActivity activity;

    // Constructor
    CharacterAdapter(FragmentActivity activity, SpellbookViewModel spellbookViewModel) {
        this.spellbookViewModel = spellbookViewModel;
        this.activity = activity;
        characterNames = new ArrayList<>();
    }

    void setCharacterNames(List<String> names) {
        characterNames = names;
        notifyDataSetChanged();
    }

    // ViewHolder methods
    @NonNull
    public CharacterRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final CharacterRowBinding binding = CharacterRowBinding.inflate(inflater, parent, false);
        return new CharacterRowHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CharacterAdapter.CharacterRowHolder holder, int position) {

        // Do nothing if the index is out of bounds
        // This shouldn't happen, but it's better than a crash
        if ( (position >= characterNames.size()) || (position < 0) ) { return; }

        // Get the appropriate spell and bind it to the holder
        final String name = characterNames.get(position);
        holder.bind(name);
    }

    public int getItemCount() { return characterNames.size(); }


    // The RowHolder class
    public class CharacterRowHolder extends ItemViewHolder<String, CharacterRowBinding> {

        public CharacterRowHolder(CharacterRowBinding b) {
            super(b, CharacterRowBinding::setName);
            itemView.setTag(this);
        }

        public void bind(String name) {
            super.bind(name);

            // Set the buttons to have the appropriate effect
            if (name != null) {

                // Set the listener for the delete button
                binding.deleteButton.setOnClickListener((v) -> {
                    final Bundle args = new Bundle();
                    args.putString(DeleteCharacterDialog.nameKey, binding.getName());
                    final DeleteCharacterDialog dialog = new DeleteCharacterDialog();
                    dialog.setArguments(args);
                    dialog.show(activity.getSupportFragmentManager(), "confirmDeleteCharacter");
                });

                // Set the listener for the label
                binding.nameLabel.setOnClickListener((v) -> {
                    final String charName = binding.getName();
                    spellbookViewModel.setCharacter(charName);

                    // Show a Toast message after selection
                    Toast.makeText(activity, "Character selected: " + charName, Toast.LENGTH_SHORT).show();
                });
            }
        }

    }

}
