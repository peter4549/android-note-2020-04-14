package com.elliot.kim.java.room;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.java.room.R;
import com.android.java.room.databinding.FragmentAddNoteBinding;

public class AddNoteFragment extends Fragment {
    private MainActivity activity;
    private FragmentAddNoteBinding binding;
    private AlertDialog.Builder builder;
    static boolean isContentEntered;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = ((MainActivity)context);
        builder = new AlertDialog.Builder(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_note,
                container, false);
        setHasOptionsMenu(true);
        activity.setSupportActionBar(binding.toolBar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolBar.setTitle("새 노트");

        isContentEntered = false;

        binding.editTextContent.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged (CharSequence s,int start, int before, int count){
                isContentEntered = count > 0;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.isFragment = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        MainActivity.isFragment = false;
        isContentEntered = false;
        binding.editTextTitle.setText(null);
        binding.editTextContent.setText(null);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_note, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isContentEntered)
                    showCheckMessage();
                else {
                    Toast.makeText(getContext(), "저장되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    activity.originalOnBackPressed();
                }
                break;
            case R.id.save:
                if (isContentEntered) {
                    saveNote();
                    activity.originalOnBackPressed();
                } else {
                    Toast.makeText(getContext(), "저장되지 않았습니다.", Toast.LENGTH_LONG).show();
                    activity.originalOnBackPressed();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveNote () {
        String title = binding.editTextTitle.getText().toString();
        String content = binding.editTextContent.getText().toString();
        if (title.equals("") && content.equals(""))
            return;

        if (title.equals("")) {
            if(content.length() > 16)
                title = content.substring(0, 16);
            else
                title = content;
        }
        String date = activity.getCurrentTime();
        activity.saveNoteInDatabase(new Note(title, date, date, content));
    }

    void showCheckMessage() {
        builder.setTitle("노트 저장");
        builder.setMessage("지금까지 작성한 내용을 저장하시겠습니까?");
        builder.setPositiveButton("저장",
                (dialog, id) -> {
                    saveNote();
                    activity.originalOnBackPressed();
                }).setNeutralButton("계속쓰기",
                (dialog, id) -> {

                });

        builder.setNegativeButton("아니요",
                (dialog, id) -> {
                    Toast.makeText(getContext(), "저장되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    activity.originalOnBackPressed();
                });
        builder.create();
        builder.show();
    }
}