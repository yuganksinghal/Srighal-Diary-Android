package edu.virginia.cs.cs4720.diary;
import java.util.Date;

/**
 * Created by Yugank Singhal on 9/13/2015.
 */
public class DiaryEntry {

    private String entry;
    private Date entryDate;
    private String picture;
    private String voice;
    private String geocache;

    public DiaryEntry(String entry) {
        this.entry = entry;
        this.entryDate= new Date();
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
}
