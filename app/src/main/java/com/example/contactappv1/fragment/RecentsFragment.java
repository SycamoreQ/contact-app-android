package com.example.contactappv1.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactappv1.R;
import com.example.contactappv1.adapter.RecentCallAdapter;
import com.example.contactappv1.model.RecentCall;

import java.util.ArrayList;
import java.util.List;

/**
 * Recents tab — shows recent call history with call type indicators.
 * Now includes toggleable search functionality.
 */
public class RecentsFragment extends Fragment {

    private RecentCallAdapter adapter;
    private boolean searchVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recents, container, false);

        com.example.contactappv1.db.DatabaseHelper dbHelper = com.example.contactappv1.db.DatabaseHelper.getInstance(getContext());

        RecyclerView rvRecents = view.findViewById(R.id.rvRecents);
        LinearLayout searchBarContainer = view.findViewById(R.id.searchBarContainer);
        EditText etRecentsSearch = view.findViewById(R.id.etRecentsSearch);
        View btnSearchToggle = view.findViewById(R.id.btnSearchToggle);

        rvRecents.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecentCallAdapter(dbHelper.getAllCalls());
        rvRecents.setAdapter(adapter);

        // ── Toggle search bar visibility ──
        btnSearchToggle.setOnClickListener(v -> {
            searchVisible = !searchVisible;
            if (searchVisible) {
                searchBarContainer.setVisibility(View.VISIBLE);
                etRecentsSearch.requestFocus();
            } else {
                searchBarContainer.setVisibility(View.GONE);
                etRecentsSearch.setText("");
                adapter.filter("");
            }
        });

        // ── Search filter ──
        etRecentsSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
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
        if (adapter != null) {
            com.example.contactappv1.db.DatabaseHelper dbHelper = com.example.contactappv1.db.DatabaseHelper.getInstance(getContext());
            adapter.updateItems(dbHelper.getAllCalls());
        }
    }
}
