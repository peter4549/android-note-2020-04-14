package com.android.java.room;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.java.room.databinding.FragmentAddNoteBinding;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AddNoteFragment extends Fragment {

    static boolean isAddNoteFragment;
    private MainActivity activity;
    private ActionBar actionBar;
    private FragmentAddNoteBinding binding;
    private Note note;
    private AlertDialog.Builder builder;

    public AddNoteFragment() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = ((MainActivity)context);
        builder = new AlertDialog.Builder(context);
        isAddNoteFragment = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_note,
                container, false);
        setHasOptionsMenu(true);
        actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("새 노트");
        actionBar.setDisplayHomeAsUpEnabled(true);

        return binding.getRoot();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isAddNoteFragment = false;
        actionBar.setTitle("클로버 노트");
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_add_note,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                activity.originalOnBackPressed();
                break;
            case R.id.save:
                if (saveNote()) {
                    activity.onAddNote(note);

                    activity.originalOnBackPressed();
                } else {
                    Toast.makeText(getContext(), "저장되지 않았습니다.", Toast.LENGTH_LONG).show();

                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean saveNote () {
        String title = binding.editTextTitle.getText().toString();
        String content = binding.editTextNote.getText().toString();
        if (title.equals("") && content.equals(""))
            return false;

        if (title.equals("")) {
            if(content.length() > 16)
                title = content.substring(0, 15);
            else
                title = content;
        }
        String date = getDate();
        note = new Note(title, date, date, content);
        return true;
    }

    private String getDate() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return simpleDateFormat.format(date);
    }

    public interface OnAddNoteListener {
        public void onAddNote(Note note);
    }

    /*
    private boolean showCheckMessage() {
        builder.setMessage("지금까지 작성한 내용을 자장하시겠습니까?").
                setPositiveButton("저장",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return true;
                            }
                        };

    }

     */
}

