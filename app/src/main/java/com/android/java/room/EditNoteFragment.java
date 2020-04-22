package com.android.java.room;

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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.android.java.room.databinding.FragmentEditNoteBinding;

public class EditNoteFragment extends Fragment {
    private FragmentEditNoteBinding binding;
    private MainActivity activity;
    private AlertDialog.Builder builder;
    private ActionBar actionBar;
    private Note note;
    private boolean isEditMode;
    private CharSequence charSequence;
    //private boolean contentChanged;

    public interface OnEditNoteListener {
        public void onEditNote(Note note);
    }

    public void setEditNote(Note note) {
        this.note = note;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = ((MainActivity)context);
        builder = new AlertDialog.Builder(context);
        MainActivity.isFragment = true;

        //contentChanged = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
        isEditMode = false;
        binding.textViewDateAdd.setText("최초 작성일: " + note.getDateAdd());
        binding.textViewDateEdit.setText("최근 수정일: " + note.getDateEdit());
        binding.editTextContentEdit.setText(note.getContent());
        binding.editTextContentEdit.setEnabled(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        MainActivity.isFragment = false;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_note,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                activity.originalOnBackPressed();
                break;
            case R.id.edit:
                if (isEditMode) {
                    item.setIcon(R.drawable.pencil_039be5_240);
                    binding.editTextContentEdit.setEnabled(false);
                    if (charSequence.toString().equals(note.getContent())) {
                        Toast.makeText(getContext(), "변경사항이 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        note.setDateEdit(activity.getDate());
                        note.setContent(charSequence.toString());
                        activity.onEditNote(note);
                        activity.originalOnBackPressed();
                    }
                } else {
                    item.setIcon(R.drawable.check_mark_green_240);
                    binding.editTextContentEdit.setEnabled(true);
                }
                isEditMode = !isEditMode;
                break;
            case R.id.menu_alarm:
                activity.onAlarmFragmentStart(this.note);
                break;
            case R.id.menu_share:
                Toast.makeText(getContext(), "Here!", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
