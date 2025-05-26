package com.example.catatanku.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.catatanku.R;
import com.example.catatanku.data.Note;
import com.example.catatanku.viewmodel.NoteViewModel;

public class AddEditNoteFragment extends Fragment {

    private static final String ARG_NOTE = "note_to_edit";

    private EditText editTextTitle;
    private EditText editTextContent;
    private Button buttonSave;
    private NoteViewModel noteViewModel;
    private Note currentNote;

    public static AddEditNoteFragment newInstance(Note note) {
        AddEditNoteFragment fragment = new AddEditNoteFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_NOTE, note);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentNote = (Note) getArguments().getSerializable(ARG_NOTE);
        }
        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_edit_note, container, false);

        editTextTitle = view.findViewById(R.id.edit_text_title);
        editTextContent = view.findViewById(R.id.edit_text_content);
        buttonSave = view.findViewById(R.id.button_save);

        if (currentNote != null) {
            editTextTitle.setText(currentNote.getTitle());
            editTextContent.setText(currentNote.getContent());
            getActivity().setTitle("Edit Catatan"); // Set judul activity/toolbar
        } else {
            getActivity().setTitle("Tambah Catatan");
        }

        buttonSave.setOnClickListener(v -> saveNote());

        return view;
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(getContext(), "Judul dan isi tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note = new Note(title, content);
        if (currentNote != null) {
            note.setId(currentNote.getId()); // Penting untuk update
            noteViewModel.update(note);
            Toast.makeText(getContext(), "Catatan diperbarui", Toast.LENGTH_SHORT).show();
        } else {
            noteViewModel.insert(note);
            Toast.makeText(getContext(), "Catatan disimpan", Toast.LENGTH_SHORT).show();
            // Kirim Broadcast
            Intent intent = new Intent(NoteListFragment.ACTION_NOTE_SAVED);
            requireActivity().sendBroadcast(intent);
        }

        // Kembali ke fragment list
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}