package com.elliot.kim.java.room;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.java.room.R;
import com.android.java.room.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        AddNoteFragment.OnAddNoteListener, EditNoteFragment.OnEditNoteListener {
    private static boolean initialization;
    static boolean isFragment = false;
    static boolean isAlarmFragment = false;

    private AddNoteFragment addNoteFragment;
    private EditNoteFragment editNoteFragment;
    private AlarmFragment alarmFragment;
    private ViewModelProvider.AndroidViewModelFactory viewModelFactory;
    private MainViewModel viewModel;
    private NoteAdapter adapter;

    private LayoutInflater inflater;
    private Note note;
    private long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.toolBar);
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty())
                    adapter.getFilter().filter(newText);
                return true;
            }
        });

        initialization = true;
        editNoteFragment = new EditNoteFragment();
        addNoteFragment = new AddNoteFragment();
        alarmFragment = new AlarmFragment();
        pressedTime = 0;

        // https://themach.tistory.com/42
        if(viewModelFactory == null){
            viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication());
        }
        viewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);

        binding.recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddNoteFragmentStart();
            }
        });

        viewModel.getAll().observe(this, new Observer<List<Note>>() {
            int notesSize;

            @Override
            public void onChanged(List<Note> notes) {
                if (initialization) {
                    adapter = new NoteAdapter(notes, MainActivity.this);
                    binding.recyclerView.setAdapter(adapter);
                    initialization = false;
                } else if (notesSize < notes.size()) {
                    ((NoteAdapter)adapter).insert(notes.get(notes.size() - 1));
                }
                notesSize = notes.size();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String action = intent.getAction();
        assert action != null;
        if(action.equals("RUN_FROM_ALARM")) {
            int number = intent.getIntExtra("NUMBER", -1);
            onEditNoteFragmentStart(viewModel.getNote(number));
            // 예외처리 필요. null반환시,
        }
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

    public void onAlarmFragmentStart(Note note) {
        alarmFragment.setNote(note);
        getSupportFragmentManager().beginTransaction().addToBackStack(null)
                .replace(R.id.container, alarmFragment).commit();
    }

    public void onItemDelete(int number) {
        viewModel.delete(number);
    }

    public void originalOnBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (isFragment) {
            if(AddNoteFragment.contentAdded)
                addNoteFragment.showCheckMessage();
            else
                super.onBackPressed();
        } else if (isAlarmFragment) {
            super.onBackPressed();
        }
        else {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu) ;
        return true ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add :
                onAddNoteFragmentStart();
                return true;
            case R.id.menu_sort:
                showSortDialog();
                return true;
            default :
                return super.onOptionsItemSelected(item) ;
        }
    }

    @Override
    public void onAddNote(Note note) {
        this.note = note;
        viewModel.insert(note);
        Toast.makeText(this, "노트가 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditNote(Note note) {
        this.note = note;
        viewModel.update(this.note);
        Toast.makeText(this, "노트가 수정되었습니다.", Toast.LENGTH_SHORT).show();
    }

    public void cancelAlarm(int number) {
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                number,
                intent,
                PendingIntent.FLAG_ONE_SHOT);
        assert alarmManager != null;
        alarmManager.cancel(pendingIntent);

        removeAlarmInformation(number);

        Toast.makeText(this, "알림이 해제되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void removeAlarmInformation(int number) {
        SharedPreferences sharedPreferences = getSharedPreferences("alarm_information",
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(number + "0");
        editor.remove(number + "1");
        editor.remove(number + "2");
        editor.remove(number + "3");
        editor.apply();
    }

    private void showSortDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sort_by)
                .setItems(getResources().getStringArray(R.array.sort_by),
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.sort(which);
                    }
                });
        builder.create();
        builder.show();
    }

    /* Considered unnecessary function
    @Override
    public void onAlarmReceiver(int number) {
        if (!isFragment) {
            onEditNoteFragmentStart(viewModel.getNote(number));
        }
    }
     */
}