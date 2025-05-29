package com.example.catatanku.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.catatanku.data.Note;
import com.example.catatanku.data.NoteRepository;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {

    private NoteRepository mRepository;
    // private LiveData<List<Note>> mAllNotes; // Bisa dihapus jika logika sepenuhnya ditangani displayedNotes
    private MutableLiveData<String> searchQuery = new MutableLiveData<>();
    public LiveData<List<Note>> displayedNotes; // LiveData untuk catatan yang akan ditampilkan

    public NoteViewModel(Application application) {
        super(application);
        mRepository = new NoteRepository(application);

        // Inisialisasi searchQuery dengan string kosong agar semua catatan dimuat saat pertama kali
        searchQuery.setValue("");

        displayedNotes = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return mRepository.getAllNotes(); // Jika query kosong, tampilkan semua catatan
            } else {
                // Repository sudah menangani penambahan wildcard "%"
                return mRepository.searchNotes(query);
            }
        });
    }

    // Method untuk UI (Fragment) memperbarui query pencarian
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    // Method yang sudah ada untuk operasi CRUD dasar
    public LiveData<List<Note>> getAllNotes() {
        // Masih berguna jika perlu akses langsung ke semua catatan tanpa filter dari tempat lain
        return mRepository.getAllNotes();
    }

    public LiveData<Note> getNoteById(int id) {
        return mRepository.getNoteById(id);
    }

    public void insert(Note note) {
        mRepository.insert(note);
    }

    public void update(Note note) {
        mRepository.update(note);
    }

    public void delete(Note note) {
        mRepository.delete(note);
    }

    public void deleteAllNotes() { // Jika Anda memiliki fitur hapus semua
        mRepository.deleteAllNotes();
    }
}