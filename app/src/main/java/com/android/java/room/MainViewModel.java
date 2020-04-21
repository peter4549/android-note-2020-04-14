package com.android.java.room;

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

    public LiveData<List<Note>> getAll() {
        return database.noteDao().getAll();
    }

    // This method cannot be executed in the main thread.
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

    public void update(Note note) {
        new UpdateAsyncTask(database.noteDao()).execute(note);
    }

    private static class UpdateAsyncTask extends AsyncTask<Note, Void, Void> {
        private NoteDao noteDao;

        UpdateAsyncTask(NoteDao noteDao) {
            this.noteDao = noteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.update(notes[0]);
            return null;
        }
    }

    public Note getNote(int number) {
        GetNoteAsyncTask getNoteAsyncTask = new GetNoteAsyncTask(database.noteDao(), number);
        try {
            return getNoteAsyncTask.execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class GetNoteAsyncTask extends AsyncTask<Void, Void, Note> {
        private NoteDao noteDao;
        private int number;

        GetNoteAsyncTask(NoteDao noteDao, int number) {
            this.noteDao = noteDao;
            this.number = number;
        }

        @Override
        protected Note doInBackground(Void... voids) {
            return noteDao.getNoteFromNumber(number);
        }
    }
}
