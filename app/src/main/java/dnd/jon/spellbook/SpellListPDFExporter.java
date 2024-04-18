package dnd.jon.spellbook;

import android.content.Context;
import android.print.PdfConverter;

import java.io.File;


public class SpellListPDFExporter extends SpellListHTMLExporter {


    SpellListPDFExporter(Context context, boolean expanded) {
        super(context, expanded);
    }

    public boolean export(File filepath) {
        addTitleText(title);
        addLineBreak();
        spells.forEach(this::addTextForSpell);
        final String html = builder.toString();
        try {
            final PdfConverter converter = PdfConverter.getInstance();
            converter.convert(context, html, filepath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
