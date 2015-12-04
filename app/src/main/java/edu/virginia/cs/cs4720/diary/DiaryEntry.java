package edu.virginia.cs.cs4720.diary;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Yugank Singhal on 9/13/2015.
 */
public class DiaryEntry implements Parcelable {

    private String entry;
    @SerializedName("entrydate")
    private Date entryDate;
    private String picture;
    private String voice;
    private String geocache;
    private String title;
    private String id;

    public DiaryEntry(String entry, String title) {
        this.entry = entry;
        this.title = title;
        this.id = UUID.randomUUID().toString();
    }

    public DiaryEntry(String entry, String title, String id){
        this.entry = entry;
        this.title = title;
        this.id = id;
    }

    public String getGeocache() {
        return geocache;
    }

    public void setGeocache(String geocache) {
        this.geocache = geocache;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Date getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(Date entryDate) {
        this.entryDate = entryDate;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getId(){
        return id;
    }

    public String toString(){
        return title + " ("+ DateFormat.getDateInstance().format(entryDate)+")";
    }

    protected DiaryEntry(Parcel in) {
        entry = in.readString();
        long tmpEntryDate = in.readLong();
        entryDate = tmpEntryDate != -1 ? new Date(tmpEntryDate) : null;
        picture = in.readString();
        voice = in.readString();
        geocache = in.readString();
        title = in.readString();
        id = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(entry);
        dest.writeLong(entryDate != null ? entryDate.getTime() : -1L);
        dest.writeString(picture);
        dest.writeString(voice);
        dest.writeString(geocache);
        dest.writeString(title);
        dest.writeString(id);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<DiaryEntry> CREATOR = new Parcelable.Creator<DiaryEntry>() {
        @Override
        public DiaryEntry createFromParcel(Parcel in) {
            return new DiaryEntry(in);
        }

        @Override
        public DiaryEntry[] newArray(int size) {
            return new DiaryEntry[size];
        }
    };
}
