package com.android.java.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Note {
    @PrimaryKey(autoGenerate = true)
    private int number;
    private String title;
    private String dateAdd;
    private String dateEdit;
    private String note;

    public Note(String title, String dateAdd, String dateEdit, String note) {
        this.title = title;
        this.dateAdd = dateAdd;
        this.dateEdit = dateEdit;
        this.note = note;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDateAdd() {
        return dateAdd;
    }

    public void setDateAdd(String dateAdd) {
        this.dateAdd = dateAdd;
    }

    public String getDateEdit() {
        return dateEdit;
    }

    public void setDateEdit(String dateEdit) {
        this.dateEdit = dateEdit;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "Note{" +
                "number=" + number +
                ", title='" + title + '\'' +
                ", dateAdd='" + dateAdd + '\'' +
                ", dateEdit='" + dateEdit + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
