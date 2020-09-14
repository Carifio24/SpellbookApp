package dnd.jon.spellbook;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

public interface DAO<T> {

    @Delete
    void delete(T t);

    @Insert
    long insert(T t);

    @Update
    void update(T t);

}
