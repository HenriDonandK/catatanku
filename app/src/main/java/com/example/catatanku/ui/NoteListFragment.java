package com.example.catatanku.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI; // Untuk setup Toolbar dengan NavController di Fragment
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.catatanku.R;
import com.example.catatanku.data.Note;
import com.example.catatanku.databinding.FragmentNoteListBinding;
import com.example.catatanku.viewmodel.NoteViewModel;
import androidx.navigation.ui.AppBarConfiguration; // WAJIB IMPORT INI
import androidx.navigation.ui.NavigationUI;     // WAJIB IMPORT INI

public class NoteListFragment extends Fragment implements NoteListAdapter.OnNoteItemInteractionListener {

    public static final String ACTION_NOTE_SAVED = "com.example.catatanku.ACTION_NOTE_SAVED";
    private FragmentNoteListBinding binding;
    private NoteViewModel noteViewModel;
    private NoteListAdapter adapter;
    private SearchView searchView;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNoteListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = NavHostFragment.findNavController(this);

        Toolbar toolbar = binding.toolbarNoteList;
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            AppBarConfiguration appBarConfiguration =
                    new AppBarConfiguration.Builder(navController.getGraph()).build();
            NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        }

        setupMenu();

        RecyclerView recyclerView = binding.recyclerviewNotes;
        adapter = new NoteListAdapter(new DiffUtil.ItemCallback<Note>() {
            @Override
            public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
                // PASTIKAN PENGECEKAN isPinned ADA DI SINI
                return oldItem.getTitle().equals(newItem.getTitle()) &&
                        oldItem.getContent().equals(newItem.getContent()) &&
                        oldItem.isPinned() == newItem.isPinned(); // <--- INI PENTING
            }
        }, this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.displayedNotes.observe(getViewLifecycleOwner(), notes -> {
            adapter.submitList(notes); // Ini akan memicu DiffUtil
        });

        binding.fabAddNote.setOnClickListener(v -> {
            NoteListFragmentDirections.ActionNoteListFragmentToAddEditNoteFragment action =
                    NoteListFragmentDirections.actionNoteListFragmentToAddEditNoteFragment();
            action.setTitleArg(getString(R.string.add_note_title));
            navController.navigate(action);
        });
    }

    private void setupMenu() {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_note_list, menu);
                MenuItem searchItem = menu.findItem(R.id.action_search);
                searchView = (SearchView) searchItem.getActionView();
                if (searchView != null) {
                    searchView.setQueryHint(getString(R.string.search_hint));
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override public boolean onQueryTextSubmit(String query) { searchView.clearFocus(); return true; }
                        @Override public boolean onQueryTextChange(String newText) {
                            noteViewModel.setSearchQuery(newText.trim()); return true;
                        }
                    });
                    searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                        @Override public boolean onMenuItemActionExpand(MenuItem item) { return true; }
                        @Override public boolean onMenuItemActionCollapse(MenuItem item) {
                            noteViewModel.setSearchQuery(""); return true;
                        }
                    });
                }
            }
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_search) {
                    return true;
                } else if (itemId == R.id.action_delete_all_notes) {
                    showDeleteAllConfirmationDialog();
                    return true;
                }
                // Biarkan NavController menangani item menu lain jika ada (misalnya dari setupWithNavController)
                return NavigationUI.onNavDestinationSelected(menuItem, navController) || false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onNoteClick(Note note) {
        NoteListFragmentDirections.ActionNoteListFragmentToAddEditNoteFragment action =
                NoteListFragmentDirections.actionNoteListFragmentToAddEditNoteFragment();
        action.setNoteId(note.getId());
        action.setTitleArg(getString(R.string.edit_note_title));
        navController.navigate(action);
    }

    @Override
    public void onDeleteClick(Note note) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete_note_confirmation_title))
                .setMessage(getString(R.string.delete_note_confirmation_message, note.getTitle()))
                .setPositiveButton(getString(R.string.delete_button_text), (dialog, which) -> {
                    noteViewModel.delete(note);
                    Toast.makeText(getContext(), getString(R.string.note_deleted_message), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.cancel_button_text), null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    @Override
    public void onPinClick(Note note) {
        note.setPinned(!note.isPinned());
        noteViewModel.update(note); // Ini akan memicu pembaruan LiveData dari Room
        // dan kemudian observer di atas akan berjalan.
        if (note.isPinned()) {
            Toast.makeText(getContext(), "Catatan disematkan", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Sematkan catatan dilepas", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteAllConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.action_delete_all_notes)
                .setMessage(R.string.confirm_delete_all_message)
                .setPositiveButton(R.string.confirm_delete_all_positive, (dialog, which) -> {
                    noteViewModel.deleteAllNotes();
                    Toast.makeText(getContext(), "Semua catatan dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.confirm_delete_all_negative, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchView != null) {
            searchView.setOnQueryTextListener(null);
        }
        binding = null;
    }
}
