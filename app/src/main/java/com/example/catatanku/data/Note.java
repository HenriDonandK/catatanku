package com.example.catatanku.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.io.Serializable; // Untuk mengirim antar fragment

@Entity(tableName = "notes_table")
public class Note implements Serializable { // Implement Serializable

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String content;

    @ColumnInfo(name = "is_pinned", defaultValue = "0") // defaultValue "0" untuk false (boolean disimpan sebagai integer 0 atau 1 di SQLite)
    private boolean isPinned;

    @ColumnInfo(name = "image_path") // Nama kolom di database
    private String imagePath; // Akan menyimpan URI sebagai String atau path file

    public Note(String title, String content) {
        this.title = title;
        this.content = content;
        this.isPinned = false; // Default saat membuat catatan baru, tidak di-pin
        this.imagePath = null; // Defaultnya tidak ada gambar
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

    public boolean isPinned() { // Getter untuk isPinned
        return isPinned;
    }
    public String getImagePath() { return imagePath; } // Getter untuk imagePath
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

    public void setPinned(boolean pinned) { // Setter untuk isPinned
        isPinned = pinned;
    }

    public void setImagePath(String imagePath) { this.imagePath = imagePath; } // Setter untuk imagePath
}