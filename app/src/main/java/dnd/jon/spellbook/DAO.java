package dnd.jon.spellbook;

public interface DAO<T> {

    void delete(T t);
    void insert(T t);
    void update(T t);

}
