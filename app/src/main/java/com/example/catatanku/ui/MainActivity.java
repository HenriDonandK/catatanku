package com.example.catatanku.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.catatanku.R;
import com.example.catatanku.data.Note;

public class MainActivity extends AppCompatActivity implements NoteListFragment.OnNoteInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            showNoteListFragment();
        }
    }

    private void showNoteListFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, NoteListFragment.newInstance(), "NoteListFragment")
                .commit();
    }

    @Override
    public void onAddNoteClicked() {
        showAddEditNoteFragment(null); // null berarti mode tambah
    }

    @Override
    public void onNoteClicked(Note note) {
        showAddEditNoteFragment(note); // Mengirim note berarti mode edit
    }

    private void showAddEditNoteFragment(Note note) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, AddEditNoteFragment.newInstance(note));
        transaction.addToBackStack(null); // Agar bisa kembali dengan tombol back
        transaction.commit();
    }
}