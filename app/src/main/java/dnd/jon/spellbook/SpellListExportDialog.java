package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import dnd.jon.spellbook.databinding.SpellListExportBinding;

enum ExportFormat {

    PDF("pdf", "PDF", "application/pdf", SpellListPDFExporter::new),
    MARKDOWN("md", "Markdown", "text/markdown", SpellListMarkdownExporter::new),
    HTML("html", "HTML", "text/html", SpellListHTMLExporter::new);

    final String extension;
    final String name;
    final String mimeType;
    final BiFunction<Context,Boolean,SpellListExporter> exporterCreator;

    ExportFormat(String extension, String name, String mimeType,
                 BiFunction<Context,Boolean,SpellListExporter> exporterCreator) {
        this.extension = extension;
        this.name = name;
        this.mimeType = mimeType;
        this.exporterCreator = exporterCreator;
    }
}


public class SpellListExportDialog extends DialogFragment {

    public static final String TAG = "SPELL_LIST_EXPORT_DIALOG";

    private SpellListExportBinding binding;
    private SpellbookViewModel viewModel;
    private Map<ExportFormat,ActivityResultLauncher<String>> filepathChoosers = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final FragmentActivity activity = requireActivity();

        // It's annoying to have to do this, but we have to register for an activity result
        // inside one of the "creation-y" lifecycle hooks (e.g. onCreate, onAttach).
        // Each document creation contract can only handle one MIME type, so in order to support
        // multiple types we need to register multiple contracts
        for (ExportFormat format: ExportFormat.values()) {
            final ActivityResultLauncher<String> chooser = registerForActivityResult(new ActivityResultContracts.CreateDocument(format.mimeType), uri -> {
                if (uri == null || uri.getPath() == null) {
                    Toast.makeText(activity, R.string.error_exporting_list, Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    // We write to a temporary file and then copy
                    // In particular, we really need this for the PDF exporter
                    // (which needs to write to a File object)
                    OutputStream outStream = activity.getContentResolver().openOutputStream(uri);
                    exportSpellList(format, outStream);
                    dismiss();
                } catch (FileNotFoundException e) {
                    Log.e(TAG, e.getMessage());
                }
            });
            filepathChoosers.put(format, chooser);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final FragmentActivity activity = requireActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        viewModel = new ViewModelProvider(activity).get(SpellbookViewModel.class);

        binding = SpellListExportBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        // We don't use one of the enum adapters because we don't want to offer "All" as an option
        final DefaultSpinnerAdapter<StatusFilterField> spellListAdapter = new DefaultSpinnerAdapter<>(
                activity,
                new StatusFilterField[]{StatusFilterField.FAVORITES, StatusFilterField.PREPARED, StatusFilterField.KNOWN},
                DisplayUtils::getDisplayName
        );
        binding.exportList.setAdapter(spellListAdapter);

        final NamedEnumSpinnerAdapter<ExportFormat> formatAdapter = new NamedEnumSpinnerAdapter<>(
                activity,
                ExportFormat.class,
                (context, format) -> format.name,
                16
        );
        binding.exportListFormat.setAdapter(formatAdapter);

        binding.exportCancelButton.setOnClickListener((v) -> this.dismiss());
        binding.exportListButton.setOnClickListener((v) -> {
            final ExportFormat format = (ExportFormat) binding.exportListFormat.getSelectedItem();
            final StatusFilterField statusFilterField = (StatusFilterField) binding.exportList.getSelectedItem();
            final String name = DisplayUtils.getDisplayName(activity, statusFilterField);
            filepathChoosers.get(format).launch(String.format("%s.%s", name, format.extension));
        });

        return builder.create();

    }


    private void exportSpellList(ExportFormat format, OutputStream outStream) {
        final FragmentActivity activity = requireActivity();
        final SpellListExporter exporter = format.exporterCreator.apply(activity, binding.exportListExpanded.isChecked());
        final StatusFilterField statusFilterField = (StatusFilterField) binding.exportList.getSelectedItem();

        // We're only offering this option for the actual spell lists
        if (statusFilterField == StatusFilterField.ALL) {
            return;
        }

        Collection<Integer> spellIDs;
        final SpellFilterStatus status = viewModel.getSpellFilterStatus();
        switch (statusFilterField) {
            case FAVORITES:
                spellIDs = status.favoriteSpellIDs();
                break;
            case PREPARED:
                spellIDs = status.preparedSpellIDs();
                break;
            case KNOWN:
                spellIDs = status.knownSpellIDs();
                break;
            default:
                // Won't happen - we already early-returned if the status is ALL
                spellIDs = new ArrayList<>();
        }

        final List<Spell> spellList = viewModel.getAllSpells()
                .stream()
                .filter(spell -> spellIDs.contains(spell.getID()))
                .collect(Collectors.toList());

        final String listName = DisplayUtils.getDisplayName(activity, statusFilterField);
        exporter.setTitle(listName);
        exporter.addSpells(spellList);
        exporter.export(outStream);
    }
}
