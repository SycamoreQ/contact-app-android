package com.example.contactappv1.adapter;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactappv1.R;
import com.example.contactappv1.model.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the main vertical contacts list.
 * Supports search filtering, section letter headers, and call/message intents.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private static final int[][] AVATAR_COLORS = {
            {R.color.avatar_1, R.color.avatar_1_text},
            {R.color.avatar_2, R.color.avatar_2_text},
            {R.color.avatar_3, R.color.avatar_3_text},
            {R.color.avatar_4, R.color.avatar_4_text},
            {R.color.avatar_5, R.color.avatar_5_text},
            {R.color.avatar_6, R.color.avatar_6_text},
            {R.color.avatar_7, R.color.avatar_7_text},
    };

    private final List<Contact> originalContacts;
    private List<Contact> filteredContacts;

    public ContactAdapter(List<Contact> contacts) {
        this.originalContacts = contacts;
        this.filteredContacts = new ArrayList<>(contacts);
    }

    /** Replace the displayed list (used by SQLite-backed search in ContactsFragment). */
    public void setContacts(List<Contact> contacts) {
        filteredContacts.clear();
        filteredContacts.addAll(contacts);
        originalContacts.clear();
        originalContacts.addAll(contacts);
        notifyDataSetChanged();
    }

    /** Filter the list by name or phone number (in-memory fallback). */
    public void filter(String query) {
        filteredContacts.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredContacts.addAll(originalContacts);
        } else {
            String lower = query.toLowerCase().trim();
            for (Contact c : originalContacts) {
                if (c.getName().toLowerCase().contains(lower)
                        || c.getPhone().contains(lower)) {
                    filteredContacts.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = filteredContacts.get(position);

        holder.tvName.setText(contact.getName());
        holder.tvPhone.setText(contact.getPhone());

        // ── Section letter header ──
        boolean showHeader;
        if (position > 0) {
            String prevLetter = filteredContacts.get(position - 1).getFirstLetter();
            String currLetter = contact.getFirstLetter();
            showHeader = !prevLetter.equals(currLetter);
        } else {
            showHeader = true;
        }
        if (showHeader) {
            holder.tvLetterHeader.setVisibility(View.VISIBLE);
            holder.tvLetterHeader.setText(contact.getFirstLetter());
        } else {
            holder.tvLetterHeader.setVisibility(View.GONE);
        }

        // ── Avatar color ──
        int colorIdx = Math.abs(contact.getName().hashCode()) % AVATAR_COLORS.length;
        int bgColor = ContextCompat.getColor(holder.itemView.getContext(), AVATAR_COLORS[colorIdx][0]);
        int textColor = ContextCompat.getColor(holder.itemView.getContext(), AVATAR_COLORS[colorIdx][1]);
        holder.llAvatar.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        holder.tvInitial.setTextColor(textColor);
        holder.tvInitial.setText(contact.getFirstLetter());

        // ── Online dot ──
        holder.vOnlineDot.setVisibility(contact.isOnline() ? View.VISIBLE : View.GONE);

        // ── Call intent ──
        holder.btnCall.setOnClickListener(v -> {
            com.example.contactappv1.db.DatabaseHelper.getInstance(v.getContext())
                .insertCall(contact.getId(), contact.getName(), contact.getPhone(),
                        com.example.contactappv1.model.RecentCall.TYPE_OUTGOING,
                        System.currentTimeMillis(), 0, "Mobile");
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + contact.getPhone()));
            v.getContext().startActivity(intent);
        });

        // ── Message intent ──
        holder.btnMsg.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + contact.getPhone()));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredContacts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLetterHeader, tvName, tvPhone, tvInitial;
        LinearLayout llAvatar;
        View vOnlineDot;
        FrameLayout btnCall, btnMsg;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLetterHeader = itemView.findViewById(R.id.tvLetterHeader);
            tvName         = itemView.findViewById(R.id.tvName);
            tvPhone        = itemView.findViewById(R.id.tvPhone);
            tvInitial      = itemView.findViewById(R.id.tvInitial);
            llAvatar       = itemView.findViewById(R.id.llAvatar);
            vOnlineDot     = itemView.findViewById(R.id.vOnlineDot);
            btnCall        = itemView.findViewById(R.id.btnCallContact);
            btnMsg         = itemView.findViewById(R.id.btnMsgContact);
        }
    }
}
