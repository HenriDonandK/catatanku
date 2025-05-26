package com.example.catatanku.ui;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.catatanku.R;
import com.example.catatanku.data.Note;
import com.example.catatanku.viewmodel.NoteViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NoteListFragment extends Fragment {

    public static final String ACTION_NOTE_SAVED = "com.example.catatanku.ACTION_NOTE_SAVED";

    private NoteViewModel noteViewModel;
    private OnNoteInteractionListener mListener;

    public static NoteListFragment newInstance() {
        return new NoteListFragment();
    }

    // Interface untuk komunikasi dengan Activity
    public interface OnNoteInteractionListener {
        void onAddNoteClicked();
        void onNoteClicked(Note note);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNoteInteractionListener) {
            mListener = (OnNoteInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNoteInteractionListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_notes);
        final NoteListAdapter adapter = new NoteListAdapter();
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_note);
        fab.setOnClickListener(v -> mListener.onAddNoteClicked());

        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            // Update RecyclerView
            adapter.setNotes(notes);
        });

        adapter.setOnItemClickListener(note -> mListener.onNoteClicked(note));

        // Tambahkan swipe-to-delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                noteViewModel.delete(adapter.getNoteAt(viewHolder.getAdapterPosition()));
                Toast.makeText(getContext(), "Catatan dihapus", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}