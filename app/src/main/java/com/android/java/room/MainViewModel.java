package com.android.java.room;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.room.Room;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private final AppDatabase database;

    public MainViewModel(@NonNull Application application) {
        super(application);

        database = Room.databaseBuilder(application, AppDatabase.class,
                "note_database").build();
    }

    public LiveData<List<Note>> getAll() {
        return database.noteDao().getAll();
    }

    public Note getNoteFromNumber(int number) {
        return database.noteDao().getNoteFromNumber(number);
    }

    public void insert(Note note) {
        new InsertAsyncTask(database.noteDao()).execute(note);
    }

    private static class InsertAsyncTask extends AsyncTask<Note, Void, Void> {
        private NoteDao noteDao;

        InsertAsyncTask(NoteDao noteDao) {
            this.noteDao = noteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.insert(notes[0]);
            return null;
        }
    }

    public void delete(int number) {
        new DeleteAsyncTask(database.noteDao(), number).execute();
    }

    private static class DeleteAsyncTask extends AsyncTask<Note, Void, Void> {
        private NoteDao noteDao;
        private int number;

        DeleteAsyncTask(NoteDao noteDao, int number) {
            this.noteDao = noteDao;
            this.number = number;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.delete(noteDao.getNoteFromNumber(number));
            return null;
        }
    }
}
