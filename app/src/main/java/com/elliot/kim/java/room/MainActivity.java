package com.elliot.kim.java.room;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.android.java.room.R;
import com.android.java.room.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // References:
    // https://stackoverflow.com/questions/43404557/clear-toolbar-menu-options-added-by-fragment-when-it-is-replaced

    private static boolean initialization;
    static boolean isFragment = false;
    static boolean isAlarmFragment = false;
    static FloatingActionButton fab;

    private ActivityMainBinding binding;
    private LayoutAnimationController animationController;

    private AddNoteFragment addNoteFragment;
    private EditNoteFragment editNoteFragment;
    private AlarmFragment alarmFragment;

    private ViewModelProvider.AndroidViewModelFactory viewModelFactory;
    private MainViewModel viewModel;

    NoteAdapter adapter;

    private long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.toolBar);

        initialization = true;
        pressedTime = 0;

        editNoteFragment = new EditNoteFragment();
        addNoteFragment = new AddNoteFragment();
        alarmFragment = new AlarmFragment();

        animationController = AnimationUtils.loadLayoutAnimation(getApplicationContext(),
                R.anim.layout_animation);

        // https://themach.tistory.com/42
        if(viewModelFactory == null){
            viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication());
        }
        viewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(layoutManager);

        fab = binding.floatingActionButton;
        fab.setOnClickListener(v -> onAddNoteFragmentStart());

        viewModel.getAll().observe(this, new Observer<List<Note>>() {
            int noteListSize;

            @Override
            public void onChanged(List<Note> noteList) {
                if (initialization) {
                    adapter = new NoteAdapter(MainActivity.this, noteList);
                    binding.recyclerView.setAdapter(adapter);
                    binding.recyclerView.setLayoutAnimation(animationController);
                    binding.recyclerView.scheduleLayoutAnimation();

                    initialization = false;
                } else if (noteListSize < noteList.size()) {
                    adapter.insert(noteList.get(noteList.size() - 1));
                    adapter.notifyItemInserted(noteList.size() - 1);
                    binding.recyclerView.setAdapter(adapter);
                }
                noteListSize = noteList.size();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String action = intent.getAction();
        if (action != null) {
            if (action.equals("ALARM_ACTION")) {
                int number = intent.getIntExtra("NUMBER", -1);
                onEditNoteFragmentStart(viewModel.getNote(number));
            }
        }
    }

    public String getCurrentTime() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 hh시 mm분 ss초");
        return simpleDateFormat.format(date);    }

    public void onEditNoteFragmentStart(Note note) {
        editNoteFragment.setEditNote(note);
        getSupportFragmentManager().beginTransaction().
                setCustomAnimations(R.anim.slide_in_top, R.anim.slide_in_bottom).
                addToBackStack(null).
                replace(R.id.container, editNoteFragment).commit();
    }

    public void onAddNoteFragmentStart() {
        getSupportFragmentManager().beginTransaction().
                setCustomAnimations(R.anim.slide_in_top, R.anim.slide_in_bottom).
                addToBackStack(null).
                replace(R.id.container, addNoteFragment).commit();
    }

    public void onAlarmFragmentStart(Note note) {
        alarmFragment.setNote(note);
        getSupportFragmentManager().beginTransaction().
                addToBackStack(null).
                setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_in_top).
                replace(R.id.container, alarmFragment).commit();
    }

    public void deleteNote(Note note) {
        viewModel.delete(note.getNumber());
        cancelAlarm(note, true);
    }

    public void originalOnBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (isFragment) {
            if(AddNoteFragment.isContentEntered) {
                addNoteFragment.showCheckMessage();
            }
            else if(EditNoteFragment.isContentChanged)
                editNoteFragment.showCheckMessage();
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchView searchView = ((SearchView) menu.findItem(R.id.menu_search).getActionView());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        searchView.setOnSearchClickListener(v -> binding.imageViewLogo.setVisibility(View.GONE));

        searchView.setOnCloseListener(() -> {
            binding.imageViewLogo.setVisibility(View.VISIBLE);
            return false;
        });

        searchView.setQueryHint("찾을 노트 제목을 입력해주세요.");

        return true;
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

    public void saveNoteInDatabase(Note note) {
        viewModel.insert(note);
        Toast.makeText(this, "노트가 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    public void updateNote(Note note) {
        viewModel.update(note);
        adapter.notifyItemChanged(adapter.getPosition(note));
    }

    public void cancelAlarm(Note note, boolean isDelete) {
        int number = note.getNumber();
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                number,
                intent,
                PendingIntent.FLAG_ONE_SHOT);

        assert alarmManager != null;
        alarmManager.cancel(pendingIntent);

        removeAlarmPreferences(number);

        if (!isDelete) {
            note.setAlarmSet(false);
            viewModel.update(note);
            adapter.notifyItemChanged(adapter.getPosition(note));
            animationController = AnimationUtils.loadLayoutAnimation(getApplicationContext(),
                    R.anim.layout_animation);
            Toast.makeText(this, "알림이 해제되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeAlarmPreferences(int number) {
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
                        (dialog, which) -> {
                            adapter.sort(which);
                            adapter.notifyDataSetChanged();
                            binding.recyclerView.setLayoutAnimation(animationController);
                            binding.recyclerView.scheduleLayoutAnimation();
                        });
        builder.create();
        builder.show();
    }

    // Function for use in EditNoteFragment
    public void share(Note note) {
        adapter.share(note);
    }

    /*
    public Note getNote(int number) {
        return viewModel.getNote(number);
    }
     */

    /* Considered unnecessary function
    @Override
    public void onAlarmReceiver(int number) {
        if (!isFragment) {
            onEditNoteFragmentStart(viewModel.getNote(number));
        }
    }
     */
}