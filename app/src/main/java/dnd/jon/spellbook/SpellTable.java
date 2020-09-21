package dnd.jon.spellbook;

class SpellTable {

    static private final int MAX_TABLE_LEVEL = 20;
    static private final int MAX_SPELL_LEVEL = Spellbook.MAX_SPELL_LEVEL;

    static private final String tableStart = "<table>";
    static private final String tableEnd = "</table>";
    static private final String cellStart = "<td>";
    static private final String cellEnd = "</td>";
    static private final String rowStart = "<tr>";
    static private final String rowEnd = "</tr>";

    static private final String headerRow;
    static {
        final String[] twoRowHeaders = new String[]{ "Level", "Proficiency Bonus", "Features", "Cantrips Known", };
        final StringBuilder builder = new StringBuilder(rowStart);
        final String boldStart = "<b>";
        final String boldEnd = "</b>";
        for (String header : twoRowHeaders) {
            builder.append("<td rowspan=\"2\">").append(boldStart).append(header).append(boldEnd).append(cellEnd);
        }
        builder.append(cellStart).append(boldStart).append("Spell Slots per Spell Level").append(boldEnd).append(cellEnd).append(rowEnd).append(rowStart);
        for (int i = 1; i <= Spellbook.MAX_SPELL_LEVEL; ++i) {
            builder.append(cellStart).append(boldStart).append(i).append(boldEnd).append(cellEnd);
        }
        builder.append(rowEnd);
        headerRow = builder.toString();
    }

    final private SpellTableRow[] tableRows = new SpellTableRow[MAX_TABLE_LEVEL];
    final private String title;

    SpellTable(String title) {
        this.title = title;
    }

    private static class SpellTableRow {
        final int level;
        final int proficiencyBonus;
        final String features;
        final int cantripsKnown;
        final int[] spellsKnown = new int[MAX_SPELL_LEVEL];

        SpellTableRow(int level, int proficiencyBonus, String features, int cantripsKnown, int[] spellsKnown) {
            this.level = level;
            this.proficiencyBonus = proficiencyBonus;
            this.features = features;
            this.cantripsKnown = cantripsKnown;
            System.arraycopy(spellsKnown, 0, this.spellsKnown, 0, Math.min(spellsKnown.length, MAX_SPELL_LEVEL));
        }
    }

    private static void addIntCell(StringBuilder builder, int value) {
        builder.append(cellStart).append(value).append(cellEnd);
    }

    private static void addStringCell(StringBuilder builder, String value) {
        builder.append(cellStart).append(value).append(cellEnd);
    }

    static private void addRowHTML(StringBuilder builder, SpellTableRow row) {
        builder.append(rowStart);
        addIntCell(builder, row.level);
        addIntCell(builder, row.proficiencyBonus);
        addStringCell(builder, row.features);
        addIntCell(builder, row.cantripsKnown);
        for (int i : row.spellsKnown) {
            if (i != 0) {
                addIntCell(builder, i);
            } else {
                addStringCell(builder, "-");
            }
        }
        builder.append(rowEnd);
    }

    void addTableRow(int level, int proficiencyBonus, String features, int cantripsKnown, int[] spellsKnown) {
        tableRows[level-1] = new SpellTableRow(level, proficiencyBonus, features, cantripsKnown, spellsKnown);
    }

    String asHTML() {
        final StringBuilder builder = new StringBuilder(tableStart);
        builder.append("<caption>").append("<b>").append(title).append("</b>").append("</caption>");
        builder.append(headerRow);
        for (SpellTableRow row : tableRows) {
            addRowHTML(builder, row);
        }
        builder.append(tableEnd);
        return builder.toString();
    }

}
