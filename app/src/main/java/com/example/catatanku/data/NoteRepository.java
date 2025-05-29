package com.example.catatanku.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

public class NoteRepository {

    private final NoteDao mNoteDao; // Dibuat final
    private final LiveData<List<Note>> mAllNotes; // Dibuat final

    public NoteRepository(Application application) {
        NoteDatabase db = NoteDatabase.getDatabase(application);
        mNoteDao = db.noteDao();
        mAllNotes = mNoteDao.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return mAllNotes;
    }

    public LiveData<Note> getNoteById(int id) {
        return mNoteDao.getNoteById(id);
    }

    public void insert(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> mNoteDao.insert(note)); // Expression lambda
    }

    public void update(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> mNoteDao.update(note)); // Expression lambda
    }

    public void delete(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> mNoteDao.delete(note)); // Expression lambda
    }

    public void deleteAllNotes() {
        NoteDatabase.databaseWriteExecutor.execute(mNoteDao::deleteAllNotes); // Method reference
    }

    public LiveData<List<Note>> searchNotes(String query) {
        return mNoteDao.searchNotes("%" + query + "%");
    }
}