package dnd.jon.spellbook;

import android.content.Context;

import java.io.OutputStream;

public class SpellListHTMLExporter extends BaseSpellListExporter {

    SpellListHTMLExporter(Context context, boolean expanded) {
        super(context, expanded);
    }

    void addTitleText(String title) {
        builder.append(String.format("<h1>%s</h1>", title));
    }

    void addSpellNameText(String name) {
        builder.append(String.format("<h2>%s</h2>", name));
    }

    void addPromptText(String prompt, String text, boolean lineBreak) {
        final String line = lineBreak ? "<br>" : "";
        builder.append(String.format("<div><strong>%s:</strong>%s %s</div>", prompt, line, text));
    }

    void addPromptText(String prompt, String text) {
        addPromptText(prompt, text, false);
    }

    void addOrdinalSchoolText(int level, School school, boolean ritual) {
        final String ordinal = level + DisplayUtils.ordinalString(context, level);
        final String schoolName = DisplayUtils.getDisplayName(context, school);
        String text = (level == 0) ?
            context.getString(R.string.school_cantrip, schoolName) :
            context.getString(R.string.ordinal_school, ordinal, schoolName.toLowerCase());
        if (ritual) {
            final String ritualString = context.getString(R.string.ritual);
            text += String.format(" (%s)", ritualString);
        }
        builder.append(String.format("<h4>%s</h4>", text));
    }

    @Override void addLineBreak() {}

    // TODO: Doing this doesn't work
    // Is there a way to use a stylesheet with the PDF converter?

    // private void addStylesheet() {
    //     builder.append("<style>h2, h4 { padding: none; margin: none; color: red; }</style>");
    // }

    // @Override
    // public boolean export(OutputStream stream) {
    //     addStylesheet();
    //     return super.export(stream);
    // }

}
