package com.elliot.kim.java.room;

import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Note {
    @PrimaryKey(autoGenerate = true)
    private int number;
    private String title;
    private String dateAdd;
    private String dateEdit;
    private String dateAlarm;
    private String content;
    private Boolean isAlarmSet;
    private Boolean isDone;

    public Note(String title, String dateAdd, String dateEdit, String content) {
        this.title = title;
        this.dateAdd = dateAdd;
        this.dateEdit = dateEdit;
        this.content = content;
        isAlarmSet = false;
        isDone = false;
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

    /*
    public void setTitle(String title) {
        this.title = title;
    }
     */

    String getDateAdd() {
        return dateAdd;
    }

    /*
    public void setDateAdd(String dateAdd) {
        this.dateAdd = dateAdd;
    }
     */

    String getDateEdit() {
        return dateEdit;
    }

    void setDateEdit(String dateEdit) {
        this.dateEdit = dateEdit;
    }

    String getDateAlarm() {
        return dateAlarm;
    }

    void setDateAlarm(String dateAlarm) {
        this.dateAlarm = dateAlarm;
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

    Boolean getAlarmSet() {
        return isAlarmSet;
    }

    void setAlarmSet(Boolean alarmSet) {
        isAlarmSet = alarmSet;
    }

    Boolean getIsDone() {
        return isDone;
    }

    void setIsDone(Boolean checked) {
        isDone = checked;
    }

    @NonNull
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

    String toStringToShare() {
        return title + '\n' +
                "최초 작성일: " + dateAdd + '\n' +
                "최근 수정일: " + dateEdit + '\n' +
                "내용:\n" + content;
    }
}
