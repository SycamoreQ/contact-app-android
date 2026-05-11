package com.example.contactappv1.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactappv1.R;
import com.example.contactappv1.adapter.FavoriteContactAdapter;
import com.example.contactappv1.db.DatabaseHelper;
import com.example.contactappv1.model.Contact;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * Favorites tab — shows a 3-column grid of favorite contacts.
 * Data is loaded from SQLite via {@link DatabaseHelper}.
 *
 * Removing a contact from favorites updates the SQLite record (toggleFavorite).
 * Adding a favorite picks from non-favorite contacts stored in SQLite.
 */
public class FavoriteFragment extends Fragment {

    private FavoriteContactAdapter adapter;
    private List<Contact> favoritesList;
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        db = DatabaseHelper.getInstance(requireContext());

        // ── Load favorites from SQLite ──
        favoritesList = db.getFavoriteContacts();

        RecyclerView rvFavorites = view.findViewById(R.id.rvFavorites);
        rvFavorites.setLayoutManager(new GridLayoutManager(getContext(), 3));

        adapter = new FavoriteContactAdapter(favoritesList);
        rvFavorites.setAdapter(adapter);

        // ── Remove Favorite (Long Click) — persists change to SQLite ──
        adapter.setOnFavoriteLongClickListener(position -> {
            Contact contactToRem = adapter.getContact(position);
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Remove Favorite")
                    .setMessage("Remove " + contactToRem.getName() + " from favorites?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        // Persist: flip favorite flag in SQLite
                        db.toggleFavorite(contactToRem.getId());
                        adapter.removeContact(position);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // ── Add Favorite (Click) — pick from non-favorite contacts in SQLite ──
        FrameLayout btnAddFavorite = view.findViewById(R.id.btnAddFavorite);
        btnAddFavorite.setOnClickListener(v -> showAddFavoriteDialog());

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) reloadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadData();
    }

    private void reloadData() {
        if (adapter != null && db != null && favoritesList != null) {
            favoritesList.clear();
            favoritesList.addAll(db.getFavoriteContacts());
            adapter.notifyDataSetChanged();
        }
    }

    private void showAddFavoriteDialog() {
        // Load all contacts from SQLite and filter out current favorites
        List<Contact> allContacts = db.getAllContacts();
        List<Contact> nonFavorites = new java.util.ArrayList<>();
        for (Contact c : allContacts) {
            if (!c.isFavorite()) nonFavorites.add(c);
        }

        if (nonFavorites.isEmpty()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Add to Favorites")
                    .setMessage("All contacts are already favorites.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        String[] displayNames = new String[nonFavorites.size()];
        for (int i = 0; i < nonFavorites.size(); i++) {
            displayNames[i] = nonFavorites.get(i).getName();
        }

        List<Contact> finalNonFavorites = nonFavorites;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_favorite, null);
        android.widget.EditText searchBox = dialogView.findViewById(R.id.etSearchFav);
        android.widget.ListView listView = dialogView.findViewById(R.id.lvFavs);

        android.widget.ArrayAdapter<String> listAdapter = new android.widget.ArrayAdapter<>(
                requireContext(), android.R.layout.simple_list_item_1, displayNames);
        listView.setAdapter(listAdapter);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add to Favorites")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .create();

        searchBox.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                listAdapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = listAdapter.getItem(position);
            Contact chosen = null;
            for (Contact c : finalNonFavorites) {
                if (c.getName().equals(selectedName)) {
                    chosen = c; break;
                }
            }
            if (chosen != null) {
                db.toggleFavorite(chosen.getId());
                Contact updated = new Contact(
                        chosen.getId(), chosen.getName(), chosen.getPhone(),
                        chosen.isOnline(), true);
                adapter.addContact(updated);
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}
