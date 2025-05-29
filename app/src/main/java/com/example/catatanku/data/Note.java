package com.example.catatanku.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable; // Untuk mengirim antar fragment

@Entity(tableName = "notes_table")
public class Note implements Serializable { // Implement Serializable

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String content;

    public Note(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    // --- Setters ---
    public void setId(int id) {
        this.id = id;
    }

    // TAMBAHKAN SETTER INI
    public void setTitle(String title) {
        this.title = title;
    }

    // TAMBAHKAN SETTER INI
    public void setContent(String content) {
        this.content = content;
    }
}