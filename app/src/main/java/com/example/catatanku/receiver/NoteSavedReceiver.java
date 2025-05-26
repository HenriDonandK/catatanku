package com.example.catatanku.receiver; // Buat package 'receiver'

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.example.catatanku.ui.NoteListFragment; // Import ACTION_NOTE_SAVED

public class NoteSavedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (NoteListFragment.ACTION_NOTE_SAVED.equals(intent.getAction())) {
            Toast.makeText(context, "Catatan baru berhasil disimpan! (via Broadcast)", Toast.LENGTH_LONG).show();
            // Kamu bisa menambahkan logika lain di sini, misal update widget, dll.
        }
    }
}