package com.android.java.room;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<Note> notes;
    EditText editText;
    private static boolean initialization;
    private EditNoteFragment editNoteFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialization = true;
        editText = findViewById(R.id.edit_text);
        editNoteFragment = new EditNoteFragment();

        final AppDatabase database = Room.databaseBuilder(this, AppDatabase.class,
                "note_database").build();

        final RecyclerView recyclerView;
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new InsertAsyncTask(database.noteDao())
                        .execute(new Note(getDate(), editText.getText().toString()));
                editText.setText(null);
            }
        });

        database.noteDao().getAll().observe(this, new Observer<List<Note>>() {
            RecyclerView.Adapter adapter;
            @Override
            public void onChanged(List<Note> notes) {
                if (initialization) {
                    adapter = new NoteAdapter(notes, MainActivity.this);
                    recyclerView.setAdapter(adapter);
                    initialization = false;
                } else {
                    ((NoteAdapter)adapter).insert(notes);
                    recyclerView.setAdapter(adapter);
                }
            }
        });
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

    public String getDate() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return simpleDateFormat.format(date);
    }

    public void onFragmentChanged(int index, Note note) {
        if (index == 0) {
            editNoteFragment.setEditNote(note);
            getSupportFragmentManager().beginTransaction().addToBackStack(null)
                .replace(R.id.container, editNoteFragment).commit();
        }
    }
}
