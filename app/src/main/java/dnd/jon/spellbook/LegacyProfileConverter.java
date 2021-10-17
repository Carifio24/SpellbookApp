package dnd.jon.spellbook;

import org.json.JSONException;
import org.json.JSONObject;

public class LegacyProfileConverter {

    private static final String versionCodeKey = "VersionCode";

    private static final Version V2_10_0 = new Version(2,10,0);
    private static final Version V2_11_0 = new Version(2,11,0);
    private static final Version V3_0_0 = new Version(3,0,0);

    static CharacterProfile fromJSON(JSONObject json) throws JSONException {
        if (json.has(versionCodeKey)) {
            final String versionCode = json.getString(versionCodeKey);
            final Version version = SpellbookUtils.coalesce(Version.fromString(versionCode), GlobalInfo.VERSION);
            if (version.compareTo(V3_0_0) >= 0) {
                return fromJSONv3(json);
            } else if (version.compareTo(V2_10_0) >= 0) {
                return fromJSONNew(json, version);
            } else {
                return fromJSONPre2_10(json);
            }
        } else {
            return fromJSONOld(json);
        }
    }

    private CharacterProfile fromJSONv3(JSONObject json) throws JSONException {
        final String name = json.getString(charNameKey);
    }

}
