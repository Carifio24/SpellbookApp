package dnd.jon.spellbook;

import androidx.databinding.BaseObservable;
import androidx.databinding.Observable;
import androidx.lifecycle.MutableLiveData;

public class PropertyAwareLiveData<T extends BaseObservable> extends MutableLiveData<T> {

    Observable.OnPropertyChangedCallback callback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            setValue(getValue());
        }
    };

    @Override
    public void setValue(T value) {
        super.setValue(value);
        value.addOnPropertyChangedCallback(callback);
    }

}
