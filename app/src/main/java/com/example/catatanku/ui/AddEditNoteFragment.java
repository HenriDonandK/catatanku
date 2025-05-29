package com.example.catatanku.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Jika Fragment ini punya Toolbar sendiri
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration; // Untuk setup Toolbar
import androidx.navigation.ui.NavigationUI;     // Untuk setup Toolbar

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Hapus import Button, EditText jika menggunakan ViewBinding
import android.widget.Toast;
import com.example.catatanku.R;
import com.example.catatanku.data.Note;
import com.example.catatanku.databinding.FragmentAddEditNoteBinding; // Gunakan ViewBinding
import com.example.catatanku.viewmodel.NoteViewModel;

public class AddEditNoteFragment extends Fragment {

    private FragmentAddEditNoteBinding binding; // Menggunakan ViewBinding
    private NoteViewModel noteViewModel;
    private Note currentNote; // Untuk menampung note yang sedang diedit (jika mode edit)
    private int noteId = -1;
    private String fragmentArgTitle = ""; // Untuk menampung argumen judul

    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);

        // Mengambil argumen menggunakan Safe Args
        if (getArguments() != null) {
            AddEditNoteFragmentArgs args = AddEditNoteFragmentArgs.fromBundle(getArguments());
            noteId = args.getNoteId();
            fragmentArgTitle = args.getTitleArg(); // Ambil judul dari argumen
        }

        if (noteId != -1) { // Mode edit, load data note
            noteViewModel.getNoteById(noteId).observe(this, note -> {
                if (note != null) {
                    currentNote = note;
                    if (binding != null) { // Pastikan binding sudah ada sebelum mengisi UI
                        populateUiWithNoteData();
                    }
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddEditNoteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        // Setup Toolbar jika ada di layout fragment_add_edit_note.xml
        // Toolbar toolbar = binding.toolbarAddEdit; // Ganti dengan ID Toolbar Anda
        // if (getActivity() instanceof AppCompatActivity && toolbar != null) {
        //     ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        //     // Setup dengan NavController agar tombol Up berfungsi dan judul sesuai argumen
        //     AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        //     NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        //     // Anda juga bisa set judul secara manual jika label di nav_graph tidak cukup
        //     // toolbar.setTitle(fragmentArgTitle);
        // } else
        if (getActivity() instanceof AppCompatActivity) {
            // Jika Toolbar dikelola oleh Activity atau Fragment sebelumnya, cukup set judul
            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(fragmentArgTitle);
            }
        }


        if (currentNote != null) {
            populateUiWithNoteData();
        }
        // Jika mode tambah baru, judul sudah diatur oleh fragmentArgTitle

        binding.buttonSave.setOnClickListener(v -> saveNote());
    }

    private void populateUiWithNoteData() {
        if (currentNote != null && binding != null) {
            binding.editTextTitle.setText(currentNote.getTitle());
            binding.editTextContent.setText(currentNote.getContent());
        }
    }

    private void saveNote() {
        String title = binding.editTextTitle.getText().toString().trim();
        String content = binding.editTextContent.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentNote != null) { // Mode Edit
            currentNote.setTitle(title);
            currentNote.setContent(content);
            noteViewModel.update(currentNote);
            Toast.makeText(getContext(), "Catatan diperbarui", Toast.LENGTH_SHORT).show();
        } else { // Mode Tambah Baru
            Note newNote = new Note(title, content);
            noteViewModel.insert(newNote);
            Toast.makeText(getContext(), "Catatan disimpan", Toast.LENGTH_SHORT).show();

            // Kirim Broadcast
            Intent intent = new Intent(NoteListFragment.ACTION_NOTE_SAVED);
            if (getContext() != null) {
                // ---- TAMBAHKAN BARIS INI ----
                intent.setPackage(requireActivity().getPackageName());
                // -----------------------------
                requireActivity().sendBroadcast(intent);
            }
        }
        navController.popBackStack();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
