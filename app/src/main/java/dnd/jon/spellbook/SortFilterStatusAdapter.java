package dnd.jon.spellbook;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
                // TODO: Add the popup menu for the options button

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
