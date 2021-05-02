package dnd.jon.spellbook;

import android.annotation.SuppressLint;

import androidx.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {

    private static final Pattern pattern = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)$");

    final int major;
    final int minor;
    final int patch;

    Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    static Version fromString(String versionString) {
        final Matcher matcher = pattern.matcher(versionString);
        if (matcher.find()) {
            final int major = Integer.parseInt(matcher.group(1));
            final int minor = Integer.parseInt(matcher.group(2));
            final int patch = Integer.parseInt(matcher.group(3));
            return new Version(major, minor, patch);
        } else {
            return null;
        }
    }

    @Override
    public int compareTo(Version other) {
        if (major != other.major) {
            return Integer.compare(major, other.major);
        }
        if (minor != other.minor) {
            return Integer.compare(minor, other.minor);
        }
        return Integer.compare(patch, other.patch);
    }

    @SuppressLint("DefaultLocale")
    String string() {
        return String.format("%d.%d.%d", major, minor, patch);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Version)) {
            return super.equals(obj);
        }
        final Version other = (Version) obj;
        return major == other.major && minor == other.minor && patch == other.patch;
    }
}
