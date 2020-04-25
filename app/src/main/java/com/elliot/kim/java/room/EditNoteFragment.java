package com.elliot.kim.java.room;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.android.java.room.R;
import com.android.java.room.databinding.FragmentEditNoteBinding;

public class EditNoteFragment extends Fragment {
    static boolean isContentChanged;

    private MainActivity activity;
    private FragmentEditNoteBinding binding;
    private AlertDialog.Builder builder;
    private Note note;

    private boolean isEditMode;
    private CharSequence charSequence;

    void setEditNote(Note note) {
        this.note = note;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = ((MainActivity)context);
        builder = new AlertDialog.Builder(context);

        MainActivity.isFragment = true;
        isContentChanged = false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_note,
                container, false);
        setHasOptionsMenu(true);
        activity.setSupportActionBar(binding.toolBar);
        binding.toolBar.setTitle(note.getTitle());
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.editTextContentEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged (CharSequence s ,int start, int before, int count){
                charSequence = s;
                isContentChanged = !note.getContent()
                        .equals(charSequence.toString());
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
        isEditMode = false;

        String dateAddText = "최초 작성일: " + note.getDateAdd();
        String dateEditText = "최근 수정일: " + note.getDateEdit();

        binding.textViewDateAdd.setText(dateAddText);
        binding.textViewDateEdit.setText(dateEditText);
        binding.editTextContentEdit.setText(note.getContent());
        binding.editTextContentEdit.setEnabled(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        MainActivity.isFragment = false;
        isContentChanged = false;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_note,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isContentChanged) {
                    showCheckMessage();
                } else {
                    activity.originalOnBackPressed();
                }
                break;
            case R.id.edit:
                if (isEditMode) {
                    item.setIcon(R.drawable.pencil_039be5_240);
                    binding.editTextContentEdit.setEnabled(false);
                    if (isContentChanged) {
                        note.setDateEdit(activity.getCurrentTime());
                        note.setContent(charSequence.toString());

                        activity.applyEditNote(note);
                        Toast.makeText(getContext(), "노트가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                        activity.originalOnBackPressed();
                    } else {
                        Toast.makeText(getContext(), "변경사항이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    item.setIcon(R.drawable.check_mark_green_240);
                    binding.editTextContentEdit.setEnabled(true);
                }
                isEditMode = !isEditMode;
                break;
            case R.id.menu_alarm:
                activity.onAlarmFragmentStart(note);
                break;
            case R.id.menu_share:
                activity.share(note);
                break;
            case R.id.menu_delete:
                activity.deleteNote(note.getNumber());
                activity.adapter.delete(note);
                Toast.makeText(getContext(), "노트가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                activity.originalOnBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void showCheckMessage() {
        builder.setTitle("노트 수정");
        builder.setMessage("지금까지 편집한 내용을 저장하시겠습니까?");
        builder.setPositiveButton("저장",
                (dialog, id) -> {
                    activity.applyEditNote(note);
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
