package dnd.jon.spellbook;

import android.content.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class SpellListPDFExporter extends SpellListHTMLExporter {


    SpellListPDFExporter(Context context, boolean expanded) {
        super(context, expanded);
    }

    public boolean export(File filepath) {
        addTitleText(title);
        spells.forEach(this::addTextForSpell);
        try (final OutputStream os = new FileOutputStream(filepath)) {
            final String html = builder.toString();
            final PdfRendererBuilder pdfBuilder = new PdfRendererBuilder();
            pdfBuilder.withHtmlContent(html, null).toStream(os).run();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
