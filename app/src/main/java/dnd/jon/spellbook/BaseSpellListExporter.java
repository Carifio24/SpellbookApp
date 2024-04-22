package dnd.jon.spellbook;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseSpellListExporter implements SpellListExporter {
    String title = null;
    List<Spell> spells = new ArrayList<>();
    final StringBuilder builder = new StringBuilder();
    final Context context;

    abstract void addTitleText(String title);
    abstract void addSpellNameText(String name);
    abstract void addPromptText(String prompt, String text, boolean lineBreak);
    abstract void addPromptText(String prompt, String text);
    abstract void addOrdinalSchoolText(int level, School school, boolean ritual);

    private final boolean expanded;

    public BaseSpellListExporter(Context context, boolean expanded) {
        this.context = context;
        this.expanded = expanded;
    }

    public BaseSpellListExporter setTitle(String title) {
        this.title = title;
        return this;
    }

    public BaseSpellListExporter addSpell(Spell spell) {
        this.spells.add(spell);
        return this;
    }

    public BaseSpellListExporter addSpells(Collection<Spell> spells) {
        this.spells.addAll(spells);
        return this;
    }

    void addLineBreak() {
        builder.append("\n");
    }

    void addTextForSpell(Spell spell) {
        addLineBreak();
        addSpellNameText(spell.getName());
        addLineBreak();
        addOrdinalSchoolText(spell.getLevel(), spell.getSchool(), spell.getRitual());
        if (expanded) {
            addLineBreak();
            final String locationPrompt = DisplayUtils.locationPrompt(context, spell.numberOfLocations());
            final String locationText = DisplayUtils.locationString(context, spell);
            addPromptText(locationPrompt, locationText);
            addLineBreak();
            final String concentrationPrompt = DisplayUtils.concentrationPrompt(context);
            final String concentrationText = DisplayUtils.boolString(context, spell.getConcentration());
            addPromptText(concentrationPrompt, concentrationText);
            addLineBreak();
            final String castingTimePrompt = DisplayUtils.castingTimePrompt(context);
            final String castingTimeText = DisplayUtils.string(context, spell.getCastingTime());
            addPromptText(castingTimePrompt, castingTimeText);
            addLineBreak();
            final String rangePrompt = DisplayUtils.rangePrompt(context);
            final String rangeText = DisplayUtils.string(context, spell.getRange());
            addPromptText(rangePrompt, rangeText);
            addLineBreak();
            final String componentsPrompt = DisplayUtils.componentsPrompt(context);
            addPromptText(componentsPrompt, spell.componentsString());
            addLineBreak();
            addPromptText(DisplayUtils.materialsPrompt(context), spell.getMaterial());
            addLineBreak();
            addPromptText(DisplayUtils.rangePrompt(context), spell.getRoyalty());
            addLineBreak();
            final String durationPrompt = DisplayUtils.durationPrompt(context);
            final String durationText = DisplayUtils.string(context, spell.getDuration());
            addPromptText(durationPrompt, durationText);
            addLineBreak();
            final String classesPrompt = DisplayUtils.classesPrompt(context);
            final String classesString = DisplayUtils.classesString(context, spell);
            addPromptText(classesPrompt, classesString);
            addLineBreak();
            final String tashasPrompt = DisplayUtils.tceExpandedClassesPrompt(context);
            final String tashasText = DisplayUtils.tashasExpandedClassesString(context, spell);
            addPromptText(tashasPrompt, tashasText);
            addLineBreak();
            final String descriptionPrompt = DisplayUtils.descriptionPrompt(context);
            addPromptText(descriptionPrompt, spell.getDescription(), true);
            if (!spell.getHigherLevel().isEmpty()) {
                addLineBreak();
                final String higherLevelPrompt = DisplayUtils.higherLevelsPrompt(context);
                addPromptText(higherLevelPrompt, spell.getHigherLevel(), true);
            }
        }
        addLineBreak();
    }

    public boolean export(OutputStream stream) {
        addTitleText(title);
        addLineBreak();
        spells.forEach(this::addTextForSpell);

        try {
            final byte[] bytes = builder.toString().getBytes(StandardCharsets.UTF_8);
            stream.write(bytes);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
