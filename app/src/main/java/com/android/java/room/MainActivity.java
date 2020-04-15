package com.android.java.room;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.android.java.room.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static boolean initialization;
    private EditNoteFragment editNoteFragment;
    private ViewModelProvider.AndroidViewModelFactory viewModelFactory;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initialization = true;
        editNoteFragment = new EditNoteFragment();

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
                viewModel.insert(new Note(getDate(), binding.editText.getText().toString()));
                binding.editText.setText(null);
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

    public void onFragmentChanged(int index, Note note) {
        if (index == 0) {
            editNoteFragment.setEditNote(note);
            getSupportFragmentManager().beginTransaction().addToBackStack(null)
                .replace(R.id.container, editNoteFragment).commit();
        }
    }

    public void onItemDelete(int number) {
        viewModel.delete(number);
    }
}