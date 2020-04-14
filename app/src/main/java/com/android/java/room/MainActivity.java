package com.android.java.room;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;
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
    private ViewModelProvider.AndroidViewModelFactory viewModelFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialization = true;
        editText = findViewById(R.id.edit_text);
        editNoteFragment = new EditNoteFragment();

        // https://themach.tistory.com/42
        if(viewModelFactory == null){
            viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication());
        }

        final MainViewModel viewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);

        final RecyclerView recyclerView;
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.insert(new Note(getDate(), editText.getText().toString()));
                editText.setText(null);
            }
        });

        viewModel.getAll().observe(this, new Observer<List<Note>>() {
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
