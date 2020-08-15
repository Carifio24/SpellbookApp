package dnd.jon.spellbook;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import dnd.jon.spellbook.databinding.CreationManagementBinding;

public class CreationManagementActivity extends AppCompatActivity {

    private CreationManagementBinding binding;
    private CreationManagementViewModel viewModel;
    private CreatedItemsAdapter adapter;
    private Intent returnIntent;

    private static final String CHANGED_KEY = "changed";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        returnIntent = new Intent(CreationManagementActivity.this, MainActivity.class);
        returnIntent.putExtra(CHANGED_KEY, false);

        binding = CreationManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new CreationManagementViewModel(getApplication());
        adapter = new CreatedItemsAdapter(this);
        binding.createdItemsEl.setAdapter(adapter);

        // Set up the adapter to open the spell editing window when a child is clicked
        binding.createdItemsEl.setOnChildClickListener((elView, view, gp, cp, id) -> {
            final CreatedItemsAdapter adapter = (CreatedItemsAdapter) elView.getAdapter();
            final Spell spell = (Spell) adapter.getChild(gp, cp);
            openSpellEditor(spell);
            return true;
        });

        // Observers
        viewModel.createdSources().observe(this, sources -> adapter.setData(sources, viewModel.getSpellsForSources(sources)));

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.SPELL_MODIFICATION_REQUEST && resultCode == Activity.RESULT_OK) {
            final Spell spell = data.getParcelableExtra(SpellCreationActivity.SPELL_KEY);
            viewModel.update(spell);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.identity, R.anim.left_to_right_exit);
        setResult(Activity.RESULT_OK, returnIntent);
    }


    private void openSpellEditor(Spell spell) {
        final Intent intent = new Intent(this, SpellCreationActivity.class);
        Bundle options = ActivityOptions.makeCustomAnimation(this, R.anim.identity, android.R.anim.slide_in_left).toBundle();
        options.putParcelable(SpellCreationActivity.SPELL_KEY, spell);
        startActivityForResult(intent, RequestCodes.SPELL_MODIFICATION_REQUEST, options);
    }








}
