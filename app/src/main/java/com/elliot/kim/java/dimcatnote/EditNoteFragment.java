package com.elliot.kim.java.dimcatnote;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.android.java.room.R;
import com.android.java.room.databinding.FragmentEditNoteBinding;

import java.util.Objects;

public class EditNoteFragment extends Fragment {
    static boolean isContentChanged;
    static TextView textViewDate;

    private MainActivity activity;
    private AlarmFragment alarmFragment;
    private FragmentEditNoteBinding binding;
    private AlertDialog.Builder builder;
    private Note note;

    private boolean isEditMode;
    private CharSequence charSequence;

    private MenuItem editModeItem;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_note,
                container, false);
        setHasOptionsMenu(true);
        activity.setSupportActionBar(binding.toolBar);
        binding.toolBar.setTitle(note.getTitle());
        Objects.requireNonNull(activity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        textViewDate = binding.textViewDate;

        alarmFragment = new AlarmFragment();

        binding.editTextContentEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged (CharSequence s ,int start, int before, int count){
                charSequence = s;
                isContentChanged = !note.getContent().equals(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.focusBlock.setOnTouchListener(new View.OnTouchListener() {

            private GestureDetector gestureDetector = new GestureDetector(activity,
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {
                            Toast.makeText(activity, "더블탭하여 노트를 편집하세요.", Toast.LENGTH_SHORT).show();
                            return super.onSingleTapUp(e);
                        }

                        @Override
                        public boolean onDoubleTap(MotionEvent e) {
                            isEditMode = true;

                            Toast.makeText(activity, "노트를 편집하세요.", Toast.LENGTH_SHORT).show();
                            getFocus();
                            return super.onDoubleTap(e);
                        }
                    });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.isFragment = true;
        MainActivity.fab.hide();
        isEditMode = false;

        setText();

        binding.focusBlock.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();
        MainActivity.isFragment = false;
        MainActivity.fab.show();
        isContentChanged = false;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_edit_note, menu);
        editModeItem = menu.findItem(R.id.menu_edit);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem menuDone = menu.findItem(R.id.menu_done);
        MenuItem menuAlarm = menu.findItem(R.id.menu_alarm);
        MenuItem menuChangeAlarm = menu.findItem(R.id.menu_change_alarm);

        if (menuDone != null) {
            if (note.getIsDone())
                menuDone.setTitle("완료해제");
            else
                menuDone.setTitle("완료체크");
        }

        if (menuAlarm != null) {
            if (note.getAlarmSet()) {
                menuAlarm.setTitle("알림해제");
                menuChangeAlarm.setVisible(true);
            }
            else {
                menuAlarm.setTitle("알림설정");
                menuChangeAlarm.setVisible(false);
            }
        }


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                AddNoteFragment.hideKeyboard(activity);
                if (isContentChanged) {
                    showCheckMessage();
                } else {
                    activity.originalOnBackPressed();
                }
                break;
            case R.id.menu_edit:
                if (isEditMode) {
                    binding.focusBlock.setVisibility(View.VISIBLE);
                    item.setIcon(R.drawable.pencil_039be5_240);
                    binding.editTextContentEdit.setEnabled(false);
                    if (isContentChanged) {
                        note.setDateEdit(activity.getCurrentTime());
                        note.setContent(charSequence.toString());

                        activity.updateNote(note);
                        Toast.makeText(getContext(), "노트가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                        activity.originalOnBackPressed();
                    } else {
                        Toast.makeText(getContext(), "변경사항이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    getFocus();
                }
                isEditMode = !isEditMode;
                break;
            case R.id.menu_alarm:
                if(note.getAlarmSet()) {
                    activity.cancelAlarm(note, false);
                    setDateText(note);
                }
                else
                    onAlarmFragmentStartFromEditNote(note);

                break;
            case R.id.menu_change_alarm:
                onAlarmFragmentStartFromEditNote(note);
                break;
            case R.id.menu_share:
                activity.share(note);
                break;
            case R.id.menu_done:
                if (note.getIsDone()) {
                    note.setIsDone(false);
                }
                else {
                    note.setIsDone(true);
                }
                activity.updateNote(note);
                break;
            case R.id.menu_delete:
                activity.deleteNote(note);
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
                    activity.updateNote(note);
                    Toast.makeText(getContext(), "노트가 수정되었습니다.", Toast.LENGTH_SHORT).show();
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

    private void showKeyboard(Context context) {
        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = ((MainActivity) context).getCurrentFocus();
        if (view != null && manager != null)
            manager.showSoftInput(view, 0);
    }

    private void getFocus() {
        editModeItem.setIcon(R.drawable.check_mark_8c9eff_120);
        binding.focusBlock.setVisibility(View.GONE);
        binding.editTextContentEdit.setEnabled(true);
        binding.editTextContentEdit.requestFocus();
        binding.editTextContentEdit.setSelection(binding.editTextContentEdit.getText().length());
        showKeyboard(activity);
    }

    private void onAlarmFragmentStartFromEditNote(Note note) {
        alarmFragment.setNote(note);
        activity.getSupportFragmentManager().beginTransaction().
                addToBackStack(null).
                setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_in_top).
                replace(R.id.edit_note_container, alarmFragment).commit();
    }

    private void setText() {
        setDateText(note);
        binding.editTextContentEdit.setText(note.getContent());
        binding.editTextContentEdit.setEnabled(false);
    }

    static void setDateText(Note note) {
        String date;
        String dateAddText = "최초 작성일: " + note.getDateAdd();
        String dateEditText = "최근 수정일: " + note.getDateEdit();
        if (note.getAlarmSet())
            date = dateAddText + "\n" + dateEditText + "\n알림시간: " + note.getDateAlarm();
        else
            date = dateAddText + "\n" + dateEditText;

        textViewDate.setText(date);
    }
}
