package com.example.catatanku.ui;

import android.graphics.Typeface; // Import untuk Typeface
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.catatanku.R;
import com.example.catatanku.data.Note;

public class NoteListAdapter extends ListAdapter<Note, NoteListAdapter.NoteViewHolder> {

    private OnNoteItemInteractionListener listener;

    // Konstruktor diubah untuk menerima listener
    public NoteListAdapter(@NonNull DiffUtil.ItemCallback<Note> diffCallback, OnNoteItemInteractionListener listener) {
        super(diffCallback);
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_note, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note currentNote = getItem(position);
        holder.textViewNoteTitle.setText(currentNote.getTitle());
        holder.textViewNoteTitle.setTypeface(null, Typeface.BOLD); // Membuat judul tebal

        holder.textViewNoteContent.setText(currentNote.getContent());
        holder.textViewNoteContent.setTypeface(null, Typeface.NORMAL); // Memastikan konten tidak tebal

        // Listener untuk klik pada seluruh item (untuk edit)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && currentNote != null) {
                listener.onNoteClick(currentNote);
            }
        });

        // Listener untuk klik pada ikon hapus
        holder.imageViewDeleteNote.setOnClickListener(v -> {
            if (listener != null && currentNote != null) {
                listener.onDeleteClick(currentNote);
            }
        });
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewNoteTitle;
        private final TextView textViewNoteContent;
        private final ImageView imageViewDeleteNote; // Referensi ke ImageView ikon hapus

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNoteTitle = itemView.findViewById(R.id.textViewNoteTitle);
            textViewNoteContent = itemView.findViewById(R.id.textViewNoteContent);
            imageViewDeleteNote = itemView.findViewById(R.id.imageViewDeleteNote); // Inisialisasi ImageView
        }
    }

    // Interface untuk menangani interaksi klik
    public interface OnNoteItemInteractionListener {
        void onNoteClick(Note note);       // Untuk klik edit
        void onDeleteClick(Note note);    // Untuk klik hapus
    }
}