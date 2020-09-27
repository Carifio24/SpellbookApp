package dnd.jon.spellbook;

import androidx.databinding.BaseObservable;
import androidx.databinding.Observable;
import androidx.lifecycle.MutableLiveData;

public class PropertyAwareLiveData<T extends BaseObservable> extends MutableLiveData<T> {

    public PropertyAwareLiveData(T t) { super(t); }

    @Override
    public void setValue(T value) {
        super.setValue(value);

        //listen to property changes
        value.addOnPropertyChangedCallback(callback);
    }

    final Observable.OnPropertyChangedCallback callback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {

            //Trigger LiveData observer on change of any property in object
            setValue(getValue());

        }
    };


}