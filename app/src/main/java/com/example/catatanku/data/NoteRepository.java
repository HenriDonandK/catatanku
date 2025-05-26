package com.example.catatanku.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

public class NoteRepository {

    private NoteDao mNoteDao;
    private LiveData<List<Note>> mAllNotes;

    public NoteRepository(Application application) {
        NoteDatabase db = NoteDatabase.getDatabase(application);
        mNoteDao = db.noteDao();
        mAllNotes = mNoteDao.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return mAllNotes;
    }

    public void insert(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            mNoteDao.insert(note);
        });
    }

    public void update(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            mNoteDao.update(note);
        });
    }

    public void delete(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            mNoteDao.delete(note);
        });
    }
}