infile = "2014_to_2024_id_map.java"
lines = []
with open(infile, "r") as f:
    for line in f:
        if line.startswith("}}"):
            continue

        text = line[4:-3]
        text = text.replace(" ", "")

        lines.append(text)

outfile = "2014_to_2024_ids.txt"
with open(outfile, "w") as f:
    for line in lines:
        f.write(line)
        f.write("\n")
