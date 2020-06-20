package dnd.jon.spellbook;

import android.os.AsyncTask;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

// This class can be used to create AsyncTasks that run methods of the given DAO

public class AsyncDaoTaskFactory<Dao> {

    private final Dao dao;

    // Constructor
    AsyncDaoTaskFactory(Dao dao) { this.dao = dao; }

    // The class to use for creating the AsyncTasks

    private static class AsyncDaoTask<DaoType,Input,Output> extends AsyncTask<Input,Void,Output> {

        private final DaoType dao;
        private final BiFunction<DaoType,Input[],Output> function;

        // For a general function
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

    // Create a task, given a consumer
    <Input> AsyncTask<Input,Void,Void> createTask(BiConsumer<Dao,Input[]> consumer) { return new AsyncDaoTask<>(dao, (dao1, inputs) -> {
        consumer.accept(dao1, inputs);
        return null;
    }); }

    <Output> AsyncTask<Void,Void,Output> createTask(Function<Dao,Output> function) { return new AsyncDaoTask<>(dao, (dao1, nothing) -> function.apply(dao1)); }

}
