package com.elliot.kim.java.room;

import android.os.Parcel;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Note {
    @PrimaryKey(autoGenerate = true)
    private int number;
    private String title;
    private String dateAdd;
    private String dateEdit;
    private String content;

    public Note(String title, String dateAdd, String dateEdit, String content) {
        this.title = title;
        this.dateAdd = dateAdd;
        this.dateEdit = dateEdit;
        this.content = content;
    }

    protected Note(Parcel in) {
        number = in.readInt();
        title = in.readString();
        dateAdd = in.readString();
        dateEdit = in.readString();
        content = in.readString();
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

    public String getContent() {
        return content;
    }

    public void setContent(String note) {
        this.content = note;
    }

    @Override
    public String toString() {
        return "Note{" +
                "number=" + number +
                ", title='" + title + '\'' +
                ", dateAdd='" + dateAdd + '\'' +
                ", dateEdit='" + dateEdit + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
