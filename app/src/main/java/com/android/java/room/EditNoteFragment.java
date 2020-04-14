package com.android.java.room;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class EditNoteFragment extends Fragment {
    private TextView textViewDateEdit;
    private TextView textViewNoteEdit;
    private String number;
    private String date;
    private String note;

    public void setEditNote(Note note) {
        this.number = note.getNumber() + "";
        this.date = note.getDate();
        this.note = note.getNote();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_edit_note,
                container, false);
        textViewDateEdit = rootView.findViewById(R.id.text_view_date_edit);
        textViewDateEdit.setText(date);
        textViewNoteEdit = rootView.findViewById(R.id.text_view_note_edit);
        textViewNoteEdit.setText(note);

        Button button = rootView.findViewById(R.id.button_edit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                // activity.onFragmentChanged(0); 내용수정작업 모드 on시키는 동
            }
        });

        return rootView;
    }
}
