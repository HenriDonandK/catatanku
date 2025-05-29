package com.example.catatanku.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.catatanku.R;

// Tidak perlu import NavController, AppBarConfiguration, dll. jika tidak digunakan di sini
// Tidak perlu implement listener apapun

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cukup setContentView, NavHostFragment akan menangani sisanya.
        setContentView(R.layout.activity_main);
    }

    // Hapus onSupportNavigateUp() jika MainActivity tidak mengelola ActionBar/Toolbar
    // dengan NavigationUI.setupWithNavController.
    // Jika Fragment yang mengelola Toolbarnya sendiri, tombol Up akan ditangani
    // oleh NavigationUI.setupWithNavController di dalam Fragment tersebut.
}