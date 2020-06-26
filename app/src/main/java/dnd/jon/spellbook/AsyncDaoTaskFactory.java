package dnd.jon.spellbook;

import android.os.AsyncTask;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

// This class can be used to create AsyncTasks that run methods of the given DAO

public class AsyncDaoTaskFactory<T, Dao extends DAO<T>> {

    private final Dao dao;

    // Constructor
    AsyncDaoTaskFactory(Dao dao) { this.dao = dao; }

    // The implementation class for the AsyncTasks
    private static class AsyncDaoTask<R, DaoType extends DAO<R>,Input,Output> extends AsyncTask<Input,Void,Output> {

        private final DaoType dao;
        private final BiFunction<DaoType,Input[],Output> function;

        AsyncDaoTask(DaoType dao, BiFunction<DaoType,Input[],Output> function) {
            this.dao = dao;
            this.function = function;
        }

        @SafeVarargs
        @Override
        protected final Output doInBackground(Input... params) {
            return function.apply(dao, params);
        }

    }


    // Create a task, given a function
    <Input,Output> AsyncTask<Input,Void,Output> createTask(BiFunction<Dao,Input[],Output> function) { return new AsyncDaoTask<>(dao, function); }
    <Output> AsyncTask<Void,Void,Output> createTask(Function<Dao,Output> function) { return new AsyncDaoTask<>(dao, (Dao dao1, Void[] nothings) -> function.apply(dao1)); }

    // Create a task, given a consumer
    <Input> AsyncTask<Input,Void,Void> createTask(BiConsumer<Dao,Input[]> consumer) { return new AsyncDaoTask<>(dao, (dao1, inputs) -> { consumer.accept(dao1, inputs); return null; }); }
    AsyncTask<Void,Void,Void> createTask(Consumer<Dao> consumer) {
        final BiConsumer<Dao,Void[]> biConsumer = (dao1, nothing) -> consumer.accept(dao);
        return createTask(biConsumer);
    }


    // Insert task
    AsyncTask<Void,Void,Void> makeInsertTask(T t) { return createTask((Dao dao1) -> dao1.insert(t)); }

    // Delete task
    AsyncTask<Void,Void,Void> makeDeleteTask(T t) { return createTask((Dao dao1) -> dao1.delete(t)); }

    //Update task
    AsyncTask<Void,Void,Void> makeUpdateTask(T t) { return createTask((Dao dao1) -> dao1.update(t)); }

}
