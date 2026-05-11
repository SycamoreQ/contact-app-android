package com.example.contactappv1.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactappv1.R;
import com.example.contactappv1.adapter.ContactAdapter;
import com.example.contactappv1.adapter.OnlineContactAdapter;
import com.example.contactappv1.db.DatabaseHelper;
import com.example.contactappv1.model.Contact;

import java.util.List;

/**
 * Contacts tab — shows profile header, online contacts row, search bar, and full contacts list.
 * Data is loaded from SQLite via {@link DatabaseHelper}.
 */
public class ContactsFragment extends Fragment {

    private ContactAdapter contactAdapter;
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        db = DatabaseHelper.getInstance(requireContext());

        RecyclerView rvOnline   = view.findViewById(R.id.rvOnlineContacts);
        RecyclerView rvContacts = view.findViewById(R.id.rvContacts);
        EditText     etSearch   = view.findViewById(R.id.etSearch);
        TextView     tvCount    = view.findViewById(R.id.tvContactCount);
        TextView     tvOnline   = view.findViewById(R.id.tvOnlineCount);

        // ── Load from SQLite ──
        List<Contact> allContacts    = db.getAllContacts();   // already sorted A-Z
        List<Contact> onlineContacts = db.getOnlineContacts();

        // ── Stats ──
        tvCount.setText(allContacts.size() + " Contacts");
        tvOnline.setText("  · " + onlineContacts.size() + " Online");

        // ── Online contacts (horizontal) ──
        rvOnline.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvOnline.setAdapter(new OnlineContactAdapter(onlineContacts));

        // ── All contacts (vertical) ──
        rvContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvContacts.setNestedScrollingEnabled(false);
        contactAdapter = new ContactAdapter(allContacts);
        rvContacts.setAdapter(contactAdapter);

        // ── Live search — queries SQLite on each keystroke ──
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    contactAdapter.setContacts(db.getAllContacts());
                } else {
                    // SQLite LIKE query — searches name AND phone
                    contactAdapter.setContacts(db.searchContacts(query));
                }
            }
        });

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
        if (contactAdapter != null && db != null) {
            java.util.List<Contact> allContacts = db.getAllContacts();
            java.util.List<Contact> onlineContacts = db.getOnlineContacts();
            
            // Only update if search bar is currently empty to avoid wiping search results
            if (getView() != null) {
                android.widget.EditText etSearch = getView().findViewById(R.id.etSearch);
                if (etSearch != null && etSearch.getText().toString().trim().isEmpty()) {
                    contactAdapter.setContacts(allContacts);
                }
                android.widget.TextView tvCount = getView().findViewById(R.id.tvContactCount);
                android.widget.TextView tvOnline = getView().findViewById(R.id.tvOnlineCount);
                if (tvCount != null) tvCount.setText(allContacts.size() + " Contacts");
                if (tvOnline != null) tvOnline.setText("  • " + onlineContacts.size() + " Online");
            }
        }
    }
}
