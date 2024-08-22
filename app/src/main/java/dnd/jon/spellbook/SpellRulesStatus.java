package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class SpellRulesStatus implements Parcelable, JSONifiable {

    private static final String defaultRulesetKey = "DefaultRuleset";
    private Ruleset defaultRuleset;

    SpellRulesStatus(Ruleset ruleset) {
        this.defaultRuleset = ruleset;
    }

    SpellRulesStatus() {
        this(Ruleset.DND_2024);
    }

    protected SpellRulesStatus(Parcel in) {
        final int rulesetValue = in.readInt();
        defaultRuleset = Ruleset.fromValue(rulesetValue);
    }

    static SpellRulesStatus fromJSON(JSONObject json) {
        final SpellRulesStatus status = new SpellRulesStatus();
        final Ruleset ruleset = Ruleset.fromValue(json.optInt(defaultRulesetKey, Ruleset.DND_2024.getValue()));
        status.setDefaultRuleset(ruleset);

        return status;
    }

    public static final Creator<SpellRulesStatus> CREATOR = new Creator<SpellRulesStatus>() {
        @Override
        public SpellRulesStatus createFromParcel(Parcel in) {
            return new SpellRulesStatus(in);
        }

        @Override
        public SpellRulesStatus[] newArray(int size) {
            return new SpellRulesStatus[size];
        }
    };

    // Getters
    Ruleset getDefaultRuleset() { return defaultRuleset; }

    // Setters
    void setDefaultRuleset(Ruleset ruleset) { this.defaultRuleset = defaultRuleset; }

    // Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        ParcelUtils.writeRuleset(dest, defaultRuleset);
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(defaultRulesetKey, defaultRuleset.getValue());
        return json;
    }
}
