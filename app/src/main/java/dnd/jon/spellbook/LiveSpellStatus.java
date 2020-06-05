package dnd.jon.spellbook;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class LiveSpellStatus {

    private MutableLiveData<Boolean> favorite = new MutableLiveData<>();
    private MutableLiveData<Boolean> known  = new MutableLiveData<>();
    private MutableLiveData<Boolean> prepared = new MutableLiveData<>();

    LiveSpellStatus(boolean favorite, boolean prepared, boolean known) {
        this.favorite.setValue(favorite);
        this.prepared.setValue(prepared);
        this.known.setValue(known);
    }

    LiveSpellStatus() {
        this(false, false, false);
    }

    public LiveData<Boolean> isFavorite() { return favorite; }
    public LiveData<Boolean> isKnown() { return known; }
    public LiveData<Boolean> isPrepared() { return prepared; }

    public void setFavorite(Boolean favorite) { this.favorite.setValue(favorite); }
    public void setKnown(Boolean known) { this.known.setValue(known); }
    public void setPrepared(Boolean prepared) { this.prepared.setValue(prepared); }



}
