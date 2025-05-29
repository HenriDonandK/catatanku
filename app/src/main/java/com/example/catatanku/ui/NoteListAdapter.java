package com.example.catatanku.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView; // Pastikan ImageView diimpor
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
// import androidx.core.content.ContextCompat; // Tidak perlu jika tint sudah di XML
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.catatanku.R;
import com.example.catatanku.data.Note;

public class NoteListAdapter extends ListAdapter<Note, NoteListAdapter.NoteViewHolder> {

    private final OnNoteItemInteractionListener listener;

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
        holder.textViewNoteTitle.setTypeface(null, Typeface.BOLD);

        holder.textViewNoteContent.setText(currentNote.getContent());
        holder.textViewNoteContent.setTypeface(null, Typeface.NORMAL);

        // --- LOGIKA UNTUK INDIKATOR PIN ---
        if (currentNote.isPinned()) {
            holder.imageViewPinnedIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewPinnedIndicator.setVisibility(View.GONE);
        }
        // ----------------------------------

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteClick(currentNote);
            }
        });

        holder.imageButtonOptions.setOnClickListener(view -> {
            if (listener != null) {
                showPopupMenu(holder.itemView.getContext(), view, currentNote, listener);
            }
        });
    }

    private void showPopupMenu(Context context, View anchorView, final Note note, final OnNoteItemInteractionListener itemListener) {
        PopupMenu popup = new PopupMenu(context, anchorView);
        popup.getMenuInflater().inflate(R.menu.note_item_options_menu, popup.getMenu());

        MenuItem pinMenuItem = popup.getMenu().findItem(R.id.action_item_pin_unpin);
        if (note.isPinned()) {
            pinMenuItem.setTitle("Lepas Sematan");
        } else {
            pinMenuItem.setTitle("Sematkan");
        }

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_item_pin_unpin) {
                itemListener.onPinClick(note);
                return true;
            } else if (itemId == R.id.action_item_delete) {
                itemListener.onDeleteClick(note);
                return true;
            }
            return false;
        });
        popup.show();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        final TextView textViewNoteTitle;
        final TextView textViewNoteContent;
        final ImageButton imageButtonOptions;
        final ImageView imageViewPinnedIndicator; // Tambahkan referensi ini

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNoteTitle = itemView.findViewById(R.id.textViewNoteTitle);
            textViewNoteContent = itemView.findViewById(R.id.textViewNoteContent);
            imageButtonOptions = itemView.findViewById(R.id.imageButtonOptions);
            imageViewPinnedIndicator = itemView.findViewById(R.id.imageViewPinnedIndicator); // Inisialisasi
        }
    }

    public interface OnNoteItemInteractionListener {
        void onNoteClick(Note note);
        void onDeleteClick(Note note);
        void onPinClick(Note note);
    }
}
