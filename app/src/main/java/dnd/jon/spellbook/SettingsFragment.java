package dnd.jon.spellbook;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.LocaleList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.kizitonwose.colorpreferencecompat.ColorPreferenceCompat;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.Locale;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String EXPORT_CONTENT_DIALOG_TAG = "EXPORT_CONTENT_DIALOG";
    private static final String IMPORT_CONTENT_DIALOG_TAG = "IMPORT_CONTENT_DIALOG";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_screen, rootKey);

        final String textColorKey = getString(R.string.text_color);
        final Preference colorPreference = findPreference(textColorKey);
        if (colorPreference != null) {
            colorPreference.setOnPreferenceClickListener(pref -> {
                showColorDialog(pref);
                return true;
            });
        }

        // Enable the ability to change the spell language, if applicable
        final boolean hasPortuguese = LocalizationUtils.hasPortugueseInstalled();
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        final Preference languagePreference = preferenceScreen.findPreference(getString(R.string.spell_language_key));
        if (languagePreference != null) {
            if (hasPortuguese) {
                languagePreference.setVisible(true);
                languagePreference.setEnabled(true);
            } else {
                final PreferenceCategory preferenceCategory = preferenceScreen.findPreference(getString(R.string.general_layout_preferences));
                if (preferenceCategory != null) {
                    preferenceCategory.removePreference(languagePreference);
                }
            }
        }

        final Preference exportAllPreference = findPreference(getString(R.string.export_all));
        if (exportAllPreference != null) {
            exportAllPreference.setOnPreferenceClickListener(preference -> {
                final ExportAllContentDialog dialog = new ExportAllContentDialog();
                dialog.show(requireActivity().getSupportFragmentManager(), EXPORT_CONTENT_DIALOG_TAG);
                return false;
            });
        }

        final Preference importContentPreference = findPreference(getString(R.string.import_content));
        if (importContentPreference != null) {
            importContentPreference.setOnPreferenceClickListener(preference -> {
                final ImportContentDialog dialog = new ImportContentDialog();
                dialog.show(requireActivity().getSupportFragmentManager(), IMPORT_CONTENT_DIALOG_TAG);
                return false;
            });
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        final Resources.Theme theme = getResources().newTheme();
        theme.applyStyle(R.style.PreferencesTheme, true);
        return view;
    }

    private void showColorDialog(Preference preference) {
        final ColorPreferenceCompat colorPreference = (ColorPreferenceCompat) preference;
        final int currentColor = colorPreference.getValue();
        final ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(getActivity())
            .setTitle(R.string.spell_text_color)
            .setPositiveButton(R.string.confirm, (ColorEnvelopeListener) (envelope, fromUser) -> colorPreference.setValue(envelope.getColor()))
            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
        final ColorPickerView colorPickerView = builder.getColorPickerView();
        colorPickerView.setInitialColor(currentColor);
        builder.setNeutralButton(R.string.default_str,
            ((dialogInterface, i) -> {
                colorPreference.setValue(SpellbookUtils.defaultColor);
                dialogInterface.dismiss();
            }))
            .show();
    }

}
