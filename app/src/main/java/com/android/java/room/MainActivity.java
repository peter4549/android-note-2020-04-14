package com.android.java.room;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.android.java.room.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        AddNoteFragment.OnAddNoteListener{
    private static boolean initialization;

    private AddNoteFragment addNoteFragment;
    private EditNoteFragment editNoteFragment;
    private ViewModelProvider.AndroidViewModelFactory viewModelFactory;
    private MainViewModel viewModel;

    //private OnBackPressedListener onBackPressedListener;

    private LayoutInflater inflater;
    private Note note;
    private long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        initialization = true;
        editNoteFragment = new EditNoteFragment();
        addNoteFragment = new AddNoteFragment();
        pressedTime = 0;

        // https://themach.tistory.com/42
        if(viewModelFactory == null){
            viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication());
        }
        viewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);

        binding.recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerView.setLayoutManager(layoutManager);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddNoteFragmentStart();
                // viewModel.insert(new Note(getDate(), binding.editText.getText().toString()));
                // binding.editText.setText(null);
            }
        });

        viewModel.getAll().observe(this, new Observer<List<Note>>() {
            RecyclerView.Adapter adapter;
            int notesSize;

            @Override
            public void onChanged(List<Note> notes) {
                if (initialization) {
                    adapter = new NoteAdapter(notes, MainActivity.this);
                    binding.recyclerView.setAdapter(adapter);
                    initialization = false;
                } else if (notesSize < notes.size()) {
                    ((NoteAdapter)adapter).insert(notes.get(notes.size() - 1));
                    binding.recyclerView.setAdapter(adapter);
                }
                notesSize = notes.size();
            }
        });
    }

    public String getDate() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return simpleDateFormat.format(date);
    }

    public void onEditNoteFragmentStart(Note note) {
        editNoteFragment.setEditNote(note);
        getSupportFragmentManager().beginTransaction().addToBackStack(null)
            .replace(R.id.container, editNoteFragment).commit();
    }

    public void onAddNoteFragmentStart() {
        getSupportFragmentManager().beginTransaction().addToBackStack(null)
                .replace(R.id.container, addNoteFragment).commit();
    }

    public void onItemDelete(int number) {
        viewModel.delete(number);
    }

    public void originalOnBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (AddNoteFragment.isAddNoteFragment) {
            super.onBackPressed();
        } else {
            if (pressedTime == 0) {
                Snackbar.make(findViewById(R.id.container),
                        "한 번 더 누르면 종료됩니다.", Snackbar.LENGTH_LONG).show();
                pressedTime = System.currentTimeMillis();
            } else {
                int seconds = (int) (System.currentTimeMillis() - pressedTime);

                if (seconds > 2000) {
                    Snackbar.make(findViewById(R.id.container),
                            " 한 번 더 누르면 종료됩니다.", Snackbar.LENGTH_LONG).show();
                    pressedTime = 0;
                } else {
                    super.onBackPressed();
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }
        }
    }

    @Override
    public void onAddNote(Note note) {
        this.note = note;
        viewModel.insert(note);
        Toast.makeText(this, "노트가 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }
}