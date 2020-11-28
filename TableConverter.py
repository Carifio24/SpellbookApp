import os
import sys
from bs4 import BeautifulSoup

# This script converts the XML tables into Android GridLayouts - the resulting files are stored in the layout directory
# Most of the formatting is done in the CasterTable and CasterTableCell styles in the project, so this just needs to assemble the basic layouts

# number_names = {
#     "1" : "one",
#     "2" : "two",
#     "3" : "three",
#     "4" : "four",
#     "5" : "five",
#     "6" : "six",
#     "7" : "seven",
#     "8" : "eight",
#     "9" : "nine"
# }

def column_span(row, header=False):
    cell_tag = "th" if header else "td"
    count = 0
    cells = row.find_all(cell_tag)
    for cell in cells:
        if cell.has_attr("colspan"):
            count += int(cell["colspan"])
        else:
            count += 1
    return count


def convert_cell(cell, header=False):
    line_sep = "\n\t\t"
    lines = [ "\t\t<TextView", "style=\"@style/CasterTableCell\"" ]
    if cell.has_attr("rowspan"):
        lines.append("android:layout_rowSpan=\"%d\"" % int(cell["rowspan"]))
    if cell.has_attr("colspan"):
        lines.append("android:layout_columnSpan=\"%d\"" % int(cell["colspan"]))
    # text_id = cell.text.replace(" ", "_").lower()
    # if text_id in number_names.keys():
    #     text_id = number_names[text_id]
    # converted += "\t\tandroid:text=\"@string/%s\"\n" % text_id
    if header:
        lines.append("android:textStyle=\"bold\"")
    lines.append("android:text=\"%s\"" % cell.text.replace(", ", ",\\n"))
    lines.append("/>\n\n")
    return line_sep.join(lines)


def convert_table(table, name):

    line_sep = "\n\t"
    
    header = table.find("thead")
    header_rows = header.find_all("tr")
    col_span = column_span(header_rows[0], header=True)

    lines = [ "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<HorizontalScrollView" ]
    lines.append("xmlns:android=\"http://schemas.android.com/apk/res/android\"")
    lines.append("android:layout_width=\"match_parent\"")
    lines.append("android:layout_height=\"wrap_content\"")
    lines.append(">\n")
    lines.append("<GridLayout")
    lines.append("android:layout_width=\"wrap_content\"")
    lines.append("android:layout_height=\"wrap_content\"")
    lines.append("style=\"@style/CasterTable\"")
    lines.append("android:id=\"@+id/%s_table\"" % name)
    lines.append("android:columnCount=\"%d\">" % col_span)
    converted = line_sep.join(lines) + "\n\n"


    for row in header_rows:
        cells = row.find_all("th")
        for cell in cells:
            converted += convert_cell(cell, header=True)

    rows = table.find_all("tr")
    for row in rows:
        cells = row.find_all("td")
        for cell in cells:
            converted += convert_cell(cell)
    converted += "%s</GridLayout>\n\n</HorizontalScrollView>" % line_sep
    return converted



def main():
    tables_dir = "/home/jon/git/SpellbookApp_v2/app/src/main/res/values"
    os.chdir(tables_dir)
    if len(sys.argv) > 1:
        classes = [ sys.argv[1] ]
    else:
        classes = [ "bard", "cleric", "druid", "paladin", "ranger", "sorcerer", "warlock", "wizard" ]
    for cls in classes:
        filename = "%s_table.xml" % cls
        with open(filename, 'r') as f:
            data = f.read()
        xml = BeautifulSoup(data)
        table = xml.find("table")
        android_table = convert_table(table, cls)
        output_file = "../layout/%s_table_layout.xml" % cls
        with open(output_file, 'w') as f:
            f.write(android_table)


if __name__ == "__main__":
    main()