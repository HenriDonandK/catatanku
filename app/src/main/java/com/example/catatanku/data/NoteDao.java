package com.example.catatanku.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    void insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("DELETE FROM notes_table")
    void deleteAllNotes();

    @Query("SELECT * FROM notes_table ORDER BY id DESC")
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * from notes_table WHERE id = :noteId")
    LiveData<Note> getNoteById(int noteId);

    // Ditambahkan untuk fungsionalitas pencarian
    // Menggunakan "LOWER()" untuk pencarian case-insensitive
    @Query("SELECT * FROM notes_table WHERE LOWER(title) LIKE LOWER(:query) OR LOWER(content) LIKE LOWER(:query) ORDER BY id DESC")
    LiveData<List<Note>> searchNotes(String query);
}