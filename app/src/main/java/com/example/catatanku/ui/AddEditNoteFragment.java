package com.example.catatanku.ui;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
// import android.content.DialogInterface; // Tidak digunakan secara langsung
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
// import android.graphics.BitmapFactory; // Glide menangani ini
import android.graphics.Canvas;
// import android.graphics.Color; // Tidak digunakan secara langsung
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build; // Pastikan import ini ada
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.catatanku.R;
import com.example.catatanku.data.Note;
import com.example.catatanku.databinding.FragmentAddEditNoteBinding;
import com.example.catatanku.viewmodel.NoteViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class AddEditNoteFragment extends Fragment {

    private FragmentAddEditNoteBinding binding;
    private NoteViewModel noteViewModel;
    private Note currentNote;
    private int noteIdArgs = -1;
    private String titleArgs = "";
    private boolean isNewNote = true;
    private NavController navController;
    private MenuHost menuHost;

    private Uri cameraImageUri;
    private String newNoteImageUriString = null; // Menyimpan URI gambar untuk catatan BARU

    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String[]> requestStoragePermissionsLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);

        setupActivityResultLaunchers();

        if (getArguments() != null) {
            try {
                AddEditNoteFragmentArgs args = AddEditNoteFragmentArgs.fromBundle(getArguments());
                noteIdArgs = args.getNoteId();
                titleArgs = args.getTitleArg();
                if (noteIdArgs != -1) {
                    isNewNote = false;
                } else {
                    isNewNote = true;
                    String defaultTitleArg = getString(R.string.add_note_title_default_arg);
                    if (titleArgs == null || titleArgs.equals(defaultTitleArg)) {
                        titleArgs = getString(R.string.add_note_title);
                    }
                }
            } catch (Exception e) {
                Log.e("AddEditNoteFragment", "Error getting args. Ensure Safe Args is configured or string resource missing.", e);
                isNewNote = true;
                titleArgs = getString(R.string.add_note_title);
            }
        } else {
            isNewNote = true;
            titleArgs = getString(R.string.add_note_title);
        }

        if (!isNewNote) {
            noteViewModel.getNoteById(noteIdArgs).observe(this, note -> {
                if (note != null) {
                    currentNote = note;
                    if (binding != null) {
                        populateUiWithNoteData(); // Panggil setelah currentNote di-set
                        if (currentNote.getImagePath() != null && !currentNote.getImagePath().isEmpty()) {
                            loadImageIntoView(Uri.parse(currentNote.getImagePath()));
                        }
                    }
                    if (menuHost != null) menuHost.invalidateMenu();
                } else {
                    Toast.makeText(getContext(), "Catatan tidak ditemukan.", Toast.LENGTH_SHORT).show();
                    if (navController != null) navController.popBackStack();
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
        menuHost = requireActivity();

        setupToolbarAndMenu();

        // Panggil populateUiWithNoteData di sini jika currentNote sudah ada (mode edit)
        // atau jika ada gambar sementara untuk catatan baru (misal setelah rotasi)
        if (currentNote != null) {
            populateUiWithNoteData();
            if (currentNote.getImagePath() != null && !currentNote.getImagePath().isEmpty()) {
                loadImageIntoView(Uri.parse(currentNote.getImagePath()));
            }
        } else if (isNewNote && newNoteImageUriString != null) {
            loadImageIntoView(Uri.parse(newNoteImageUriString));
        }


        binding.buttonSave.setOnClickListener(v -> saveNote());

        // Akses tombol gambar langsung dari binding utama
        binding.buttonTakePhoto.setOnClickListener(v -> checkCameraPermissionAndTakePhoto());
        binding.buttonChooseGallery.setOnClickListener(v -> checkStoragePermissionAndPickImage());
    }

    // DEFINISIKAN METHOD INI JIKA BELUM ADA ATAU PERBAIKI
    private void populateUiWithNoteData() {
        if (currentNote != null && binding != null) {
            binding.editTextTitle.setText(currentNote.getTitle());
            binding.editTextContent.setText(currentNote.getContent());
            // Jika ada field lain di Note yang perlu ditampilkan, set di sini
        }
    }

    private void setupActivityResultLaunchers() {
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) dispatchTakePictureIntent();
                    else Toast.makeText(requireContext(), "Izin kamera ditolak", Toast.LENGTH_SHORT).show();
                });

        requestStoragePermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                (Map<String, Boolean> permissions) -> {
                    boolean storagePermissionGranted = false;
                    String permissionNeeded = "";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionNeeded = Manifest.permission.READ_MEDIA_IMAGES;
                    } else {
                        permissionNeeded = Manifest.permission.READ_EXTERNAL_STORAGE;
                    }

                    if (Boolean.TRUE.equals(permissions.get(permissionNeeded))) {
                        storagePermissionGranted = true;
                    }

                    if (storagePermissionGranted) {
                        dispatchPickImageIntent();
                    } else {
                        Toast.makeText(requireContext(), "Izin penyimpanan ditolak", Toast.LENGTH_SHORT).show();
                    }
                });

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && cameraImageUri != null) {
                        Uri stableUri = cameraImageUri; // URI dari FileProvider sudah stabil
                        loadImageIntoView(stableUri);
                        if (isNewNote) {
                            newNoteImageUriString = stableUri.toString();
                        } else if (currentNote != null) {
                            currentNote.setImagePath(stableUri.toString());
                        }
                    } else {
                        Toast.makeText(requireContext(), "Gagal mengambil gambar", Toast.LENGTH_SHORT).show();
                        cameraImageUri = null;
                    }
                });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Uri persistentUri = copyUriToAppCache(uri);
                        if (persistentUri != null) {
                            loadImageIntoView(persistentUri);
                            if (isNewNote) {
                                newNoteImageUriString = persistentUri.toString();
                            } else if (currentNote != null) {
                                currentNote.setImagePath(persistentUri.toString());
                            }
                        } else {
                            Toast.makeText(requireContext(), "Gagal memproses gambar dari galeri", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Tidak ada gambar dipilih", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Uri copyUriToAppCache(Uri sourceUri) {
        if (sourceUri == null) return null;
        Context context = getContext(); // Gunakan getContext() karena bisa null sebelum onViewCreated
        if (context == null) return null;

        try (InputStream inputStream = context.getContentResolver().openInputStream(sourceUri)) {
            if (inputStream == null) return null;

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "GALLERY_" + timeStamp + ".jpg";

            File cacheDir = new File(context.getCacheDir(), "note_images_cache");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            File outputFile = new File(cacheDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[4 * 1024];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
            }
            return FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider",
                    outputFile);
        } catch (Exception e) {
            Log.e("AddEditNoteFragment", "Error copying URI to app cache", e);
            Toast.makeText(context, "Gagal menyalin gambar.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFileForCamera();
        } catch (IOException ex) {
            Log.e("AddEditNoteFragment", "Error creating image file for camera", ex);
            Toast.makeText(requireContext(), "Gagal membuat file gambar", Toast.LENGTH_SHORT).show();
            return;
        }

        cameraImageUri = FileProvider.getUriForFile(requireContext(),
                requireActivity().getPackageName() + ".fileprovider",
                photoFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        try {
            takePictureLauncher.launch(cameraImageUri);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Tidak ada aplikasi kamera ditemukan", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFileForCamera() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_NOTE_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            storageDir = new File(requireContext().getCacheDir(), "camera_captures_cache");
        }
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            Log.e("AddEditNoteFragment", "failed to create directory for camera image: " + storageDir.getAbsolutePath());
            throw new IOException("Failed to create directory: " + storageDir.getAbsolutePath());
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        // currentPhotoPath = image.getAbsolutePath(); // Tidak digunakan lagi secara aktif
        return image;
    }

    private void checkStoragePermissionAndPickImage() {
        String[] permissionsToRequest;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            permissionsToRequest = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        boolean allNeededPermissionsGranted = true;
        for (String permission : permissionsToRequest) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                allNeededPermissionsGranted = false;
                break;
            }
        }

        if (allNeededPermissionsGranted) {
            dispatchPickImageIntent();
        } else {
            requestStoragePermissionsLauncher.launch(permissionsToRequest);
        }
    }

    private void dispatchPickImageIntent() {
        pickImageLauncher.launch("image/*");
    }

    private void loadImageIntoView(Uri imageUri) {
        if (imageUri != null && binding != null && getContext() != null) {
            binding.imageViewNoteAttachment.setVisibility(View.VISIBLE);
            Glide.with(requireContext())
                    .load(imageUri)
                    // Hapus placeholder dan error jika tidak ada drawable-nya
                    // .placeholder(R.drawable.ic_image_placeholder)
                    // .error(R.drawable.ic_broken_image)
                    .into(binding.imageViewNoteAttachment);
        } else if (binding != null) {
            binding.imageViewNoteAttachment.setVisibility(View.GONE);
        }
    }

    private void saveNote() {
        String title = binding.editTextTitle.getText().toString().trim();
        String content = binding.editTextContent.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalImagePath = null;
        if (!isNewNote && currentNote != null) { // Mode Edit
            finalImagePath = currentNote.getImagePath(); // imagePath sudah diupdate oleh launcher
        } else { // Mode Tambah Baru
            finalImagePath = newNoteImageUriString;
        }

        if (!isNewNote && currentNote != null) { // Mode Edit
            currentNote.setTitle(title);
            currentNote.setContent(content);
            currentNote.setImagePath(finalImagePath);
            noteViewModel.update(currentNote);
            Toast.makeText(requireContext(), "Catatan diperbarui", Toast.LENGTH_SHORT).show();
        } else { // Mode Tambah Baru
            Note newNote = new Note(title, content);
            newNote.setImagePath(finalImagePath);
            noteViewModel.insert(newNote);
            Toast.makeText(requireContext(), "Catatan disimpan", Toast.LENGTH_SHORT).show();
        }

        if (isNewNote) {
            Context context = getContext();
            if (context != null) {
                Intent intent = new Intent(NoteListFragment.ACTION_NOTE_SAVED);
                intent.setPackage(context.getPackageName());
                context.sendBroadcast(intent);
            }
        }

        if (navController != null) {
            navController.popBackStack();
        }
        newNoteImageUriString = null;
        cameraImageUri = null;
    }

    // --- Method setupToolbarAndMenu, togglePinStatus, confirmDeleteNote, showShareOptionsDialog, shareNoteAsText, shareNoteAsImage ---
    // --- SAMA SEPERTI KODE ANDA SEBELUMNYA YANG SUDAH BERFUNGSI BAIK ---
    private void setupToolbarAndMenu() {
        Toolbar toolbar = binding.toolbarAddEditNote;
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
            NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle(titleArgs);
            }
        }

        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.add_edit_note_menu, menu);
            }
            @Override
            public void onPrepareMenu(@NonNull Menu menu) {
                MenuItem pinMenuItem = menu.findItem(R.id.action_pin_unpin_detail);
                MenuItem deleteMenuItem = menu.findItem(R.id.action_delete_note_detail);
                MenuItem shareMenuItem = menu.findItem(R.id.action_share_note_detail);
                boolean isInEditMode = !isNewNote && currentNote != null;
                pinMenuItem.setVisible(isInEditMode);
                deleteMenuItem.setVisible(isInEditMode);
                shareMenuItem.setVisible(isInEditMode);
                if (isInEditMode) {
                    pinMenuItem.setTitle(currentNote.isPinned() ? getString(R.string.unpin_note) : getString(R.string.pin_note));
                }
                MenuProvider.super.onPrepareMenu(menu);
            }
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_pin_unpin_detail) {
                    if (currentNote != null) togglePinStatus(); return true;
                } else if (itemId == R.id.action_delete_note_detail) {
                    if (currentNote != null) confirmDeleteNote(); return true;
                } else if (itemId == R.id.action_share_note_detail) {
                    if (currentNote != null) showShareOptionsDialog(); return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void togglePinStatus() {
        if (currentNote != null) {
            currentNote.setPinned(!currentNote.isPinned());
            noteViewModel.update(currentNote);
            Toast.makeText(getContext(), currentNote.isPinned() ? getString(R.string.note_pinned_toast) : getString(R.string.note_unpinned_toast), Toast.LENGTH_SHORT).show();
            if (menuHost != null) menuHost.invalidateMenu();
        }
    }

    private void confirmDeleteNote() {
        if (currentNote != null && getContext() != null) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.delete_note_confirmation_title_detail))
                    .setMessage(getString(R.string.delete_note_confirmation_message_detail, currentNote.getTitle()))
                    .setPositiveButton(getString(R.string.delete_button_text), (dialog, which) -> {
                        noteViewModel.delete(currentNote);
                        Toast.makeText(requireContext(), getString(R.string.note_deleted_message), Toast.LENGTH_SHORT).show();
                        if (navController != null) navController.popBackStack();
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setIcon(R.drawable.ic_delete)
                    .show();
        }
    }

    private void showShareOptionsDialog() {
        if (currentNote == null) {
            Toast.makeText(requireContext(), "Simpan catatan terlebih dahulu untuk membagikan.", Toast.LENGTH_SHORT).show();
            return;
        }
        final CharSequence[] options = {
                getString(R.string.share_as_text),
                getString(R.string.share_as_image),
                getString(R.string.cancel)
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.share_options_title));
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals(getString(R.string.share_as_text))) {
                shareNoteAsText();
            } else if (options[item].equals(getString(R.string.share_as_image))) {
                shareNoteAsImage();
            } else if (options[item].equals(getString(R.string.cancel))) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void shareNoteAsText() {
        if (currentNote != null) {
            String shareBody = currentNote.getTitle() + "\n\n" + currentNote.getContent();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentNote.getTitle());
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(shareIntent, "Bagikan catatan melalui..."));
        }
    }

    private void shareNoteAsImage() {
        if (currentNote == null) {
            Toast.makeText(requireContext(), "Tidak dapat membagikan catatan sebagai gambar saat ini.", Toast.LENGTH_SHORT).show();
            return;
        }
        Context context = requireContext();

        // Dapatkan referensi ke NestedScrollView dan LinearLayout menggunakan binding
        androidx.core.widget.NestedScrollView scrollView = binding.noteScrollView; // Asumsikan ID di layout adalah noteScrollView
        LinearLayout noteContentLayout = binding.linearLayoutNoteContent; // Gunakan ID yang benar dari layout

        View viewToCapture = noteContentLayout != null ? noteContentLayout : scrollView;
        if (viewToCapture == null) {
            Toast.makeText(context, "Gagal menemukan view untuk dibagikan.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ukur view
        viewToCapture.measure(View.MeasureSpec.makeMeasureSpec(
                        getResources().getDisplayMetrics().widthPixels, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        viewToCapture.layout(0, 0, viewToCapture.getMeasuredWidth(), viewToCapture.getMeasuredHeight());

        // Buat bitmap
        Bitmap bitmap = Bitmap.createBitmap(viewToCapture.getMeasuredWidth(),
                viewToCapture.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        viewToCapture.draw(canvas);

        try {
            File cacheDir = context.getCacheDir();
            String imageFileName = "shared_note_" + System.currentTimeMillis() + ".png";
            File imageFile = new File(cacheDir, imageFileName);
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            fos.close();

            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);

            if (contentUri != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Catatan: " + currentNote.getTitle());
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Bagikan gambar melalui..."));
            } else {
                Toast.makeText(context, "Gagal mendapatkan URI gambar.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("ShareAsImage", "Error creating or sharing image: " + e.getMessage(), e);
            Toast.makeText(context, "Gagal membagikan gambar catatan.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Penting untuk ViewBinding
    }
}
