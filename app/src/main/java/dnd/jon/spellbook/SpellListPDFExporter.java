package dnd.jon.spellbook;

import android.content.Context;
import android.print.PdfConverter;

import java.io.File;
import java.io.OutputStream;


public class SpellListPDFExporter extends SpellListHTMLExporter {


    SpellListPDFExporter(Context context, boolean expanded) {
        super(context, expanded);
    }

    public boolean export(OutputStream stream) {
        addTitleText(title);
        addLineBreak();
        spells.forEach(this::addTextForSpell);
        final String html = builder.toString();
        try {
            final PdfConverter converter = new PdfConverter(context);
            converter.convert(context, html, stream);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
