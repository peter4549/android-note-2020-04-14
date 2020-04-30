package com.elliot.kim.java.dimcatnote;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.room.Room;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainViewModel extends AndroidViewModel {
    private AppDatabase database;

    public MainViewModel(@NonNull Application application) {
        super(application);

        database = Room.databaseBuilder(application, AppDatabase.class,
                "note_database")
                .fallbackToDestructiveMigration()
                .build();
    }

    LiveData<List<Note>> getAll() {
        return database.dao().getAll();
    }

    // This method cannot be executed in the main thread.
    /*
    public Note getNoteByNumber(int number) {
        return database.dao().getNoteByNumber(number);
    }
     */

    void insert(Note note) {
        new InsertAsyncTask(database.dao()).execute(note);
    }

    private static class InsertAsyncTask extends AsyncTask<Note, Void, Void> {
        private NoteDao dao;

        InsertAsyncTask(NoteDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            dao.insert(notes[0]);
            return null;
        }
    }

    void delete(int number) {
        new DeleteAsyncTask(database.dao(), number).execute();
    }

    private static class DeleteAsyncTask extends AsyncTask<Note, Void, Void> {
        private NoteDao dao;
        private int number;

        DeleteAsyncTask(NoteDao dao, int number) {
            this.dao = dao;
            this.number = number;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            dao.delete(dao.getNoteByNumber(number));
            return null;
        }
    }

    void update(Note note) {
        new UpdateAsyncTask(database.dao()).execute(note);
    }

    private static class UpdateAsyncTask extends AsyncTask<Note, Void, Void> {
        private NoteDao noteDao;

        UpdateAsyncTask(NoteDao dao) {
            this.noteDao = dao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.update(notes[0]);
            return null;
        }
    }

    Note getNote(int number) {
        GetNoteAsyncTask getNoteAsyncTask = new GetNoteAsyncTask(database.dao(), number);
        try {
            return getNoteAsyncTask.execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class GetNoteAsyncTask extends AsyncTask<Void, Void, Note> {
        private NoteDao dao;
        private int number;

        GetNoteAsyncTask(NoteDao dao, int number) {
            this.dao = dao;
            this.number = number;
        }

        @Override
        protected Note doInBackground(Void... voids) {
            return dao.getNoteByNumber(number);
        }
    }
}