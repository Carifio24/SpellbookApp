package dnd.jon.spellbook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Collection;

import dnd.jon.spellbook.databinding.SourceCreationBinding;

public class SourceCreationActivity extends AppCompatActivity {

    static final String SOURCE_KEY = "source";

    private SourceCreationBinding binding;
    private Intent returnIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the binding and set the content view as its root view
        binding = SourceCreationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set the toolbar as the app bar for the activity
        final Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.source_creation);

        // Set up the back arrow on the navigation bar
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener((v) -> this.exit());

        // Determine whether we're modifying a source or creating a new one
        final Intent intent = getIntent();
        final boolean newSource = intent.hasExtra(SOURCE_KEY);
        if (newSource) {
            final Source source = intent.getParcelableExtra(SOURCE_KEY);
            if (source != null) {
                binding.setSource(source);
            }
        }

        // Set up the create source button
        binding.createSourceButton.setOnClickListener((v) -> createSource());

        // Create the return intent
        returnIntent = new Intent(SourceCreationActivity.this, CreationManagementActivity.class);

    }

    private void exit() {
        setResult(Activity.RESULT_CANCELED, returnIntent);
        this.finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.identity, android.R.anim.slide_out_right);
    }

    private void showErrorMessage(String text) {
        binding.errorText.setText(text);
    }

    private void createSource() {

        // Check that the source name is nonempty and doesn't have any illegal characters
        final String name = binding.nameEntry.getText().toString();
        if (name.isEmpty()) { showErrorMessage("The source name is empty"); return; }
        final Collection<Character> illegalNameChars = SpellbookUtils.illegalCharactersCheck(name);
        if (illegalNameChars.size() > 0) {
            final String illegalCharsString = TextUtils.join(", ", illegalNameChars);
            showErrorMessage(getString(R.string.illegal_character, "source name", illegalCharsString));
            return;
        }

        // Get the abbreviation
        // If it's nonempty, check that it doesn't have any illegal characters
        String abbreviation = binding.abbreviationEntry.getText().toString();
        if (abbreviation.isEmpty()) {
            abbreviation = null;
        } else {
            final Collection<Character> illegalAbbrChars = SpellbookUtils.illegalCharactersCheck(abbreviation);
            if (illegalAbbrChars.size() > 0) {
                final String illegalCharsString = TextUtils.join(", ", illegalAbbrChars);
                showErrorMessage(getString(R.string.illegal_character, "abbreviation", illegalCharsString));
                return;
            }
        }

        // Build the source and add it to the return intent
        // Then finish the activity
        final Source source = new Source(0, name, abbreviation, true);
        returnIntent.putExtra(SOURCE_KEY, source);
        setResult(Activity.RESULT_OK, returnIntent);

    }



}
