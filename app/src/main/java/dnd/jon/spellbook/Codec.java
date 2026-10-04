package dnd.jon.spellbook;

public interface Codec<T, S, DE extends Throwable, EE extends Throwable> {
    T decode(S serialized) throws DE;
    S encode(T object) throws EE;
}
