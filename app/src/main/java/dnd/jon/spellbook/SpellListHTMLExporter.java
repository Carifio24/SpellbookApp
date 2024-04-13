package dnd.jon.spellbook;

import android.content.Context;

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
        builder.append(String.format("<strong>%s:</strong>%s %s", prompt, line, text));
    }

    void addPromptText(String prompt, String text) {
        addPromptText(prompt, text, false);
    }

    void addOrdinalSchoolText(int level, School school, boolean ritual) {
        final String ordinal = level + DisplayUtils.ordinalString(context, level);
        final String schoolName = DisplayUtils.getDisplayName(context, school);
        String text = context.getString(R.string.ordinal_school, ordinal, schoolName.toLowerCase());
        if (ritual) {
            final String ritualString = context.getString(R.string.ritual);
            text += String.format(" (%s)", ritualString);
        }
        builder.append(String.format("<h4>%s</h4>", text));
    }

    @Override void addLineBreak() {}
}
