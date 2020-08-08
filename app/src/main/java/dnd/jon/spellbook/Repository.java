package dnd.jon.spellbook;

import android.app.Application;

import java.util.function.Function;

public class Repository<T, DaoType extends DAO<T>> {

    final DaoType dao;
    final AsyncDaoTaskFactory<T, DaoType> taskFactory;

    Repository(Application application, Function<Application, DaoType> daoGetter)  {
        dao = daoGetter.apply(application);
        taskFactory = new AsyncDaoTaskFactory<>(dao);
    }

    // Modifiers (C/U/D)
    void insert(T t) { taskFactory.makeInsertTask(t).execute(); }
    void update(T t) { taskFactory.makeUpdateTask(t).execute(); }
    void delete(T t) { taskFactory.makeDeleteTask(t).execute(); }
}
