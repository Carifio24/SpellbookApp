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

    static final String SPELLS_CHANGED_KEY = "spells_changed";
    static final String SOURCES_CHANGED_KEY = "sources_changed";
    private static final String CREATION_CHOOSER_TAG = "creation_chooser";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        returnIntent = new Intent(CreationManagementActivity.this, MainActivity.class);
        returnIntent.putExtra(SPELLS_CHANGED_KEY, false);
        returnIntent.putExtra(SOURCES_CHANGED_KEY, false);

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

        // Floating action button
        binding.newItemFab.setOnClickListener((v) -> openCreationChooserDialog());

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Spell spell;
            Source source;
            int[] spellClassIDs;
            switch (requestCode) {
                case RequestCodes.SPELL_CREATION_REQUEST:
                    spell = data.getParcelableExtra(SpellCreationActivity.SPELL_KEY);
                    spellClassIDs = data.getIntArrayExtra(SpellCreationActivity.CLASS_IDS_KEY);
                    viewModel.addNew(spell, spellClassIDs);
                    returnIntent.putExtra(SPELLS_CHANGED_KEY, true);
                    return;
                case RequestCodes.SPELL_MODIFICATION_REQUEST:
                    spell = data.getParcelableExtra(SpellCreationActivity.SPELL_KEY);
                    viewModel.update(spell);

                    returnIntent.putExtra(SPELLS_CHANGED_KEY, true);
                    return;
                case RequestCodes.SOURCE_CREATION_REQUEST:
                    source = data.getParcelableExtra(SourceCreationActivity.SOURCE_KEY);
                    viewModel.addNew(source);
                    returnIntent.putExtra(SOURCES_CHANGED_KEY, true);
                    return;
                case RequestCodes.SOURCE_MODIFICATION_REQUEST:
                    source = data.getParcelableExtra(SourceCreationActivity.SOURCE_KEY);
                    viewModel.update(source);
                    returnIntent.putExtra(SOURCES_CHANGED_KEY, true);
            }
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

    private void openCreationChooserDialog() {
        final CreationChooserDialog dialog = new CreationChooserDialog();
        dialog.show(getSupportFragmentManager(), CREATION_CHOOSER_TAG);
    }








}
