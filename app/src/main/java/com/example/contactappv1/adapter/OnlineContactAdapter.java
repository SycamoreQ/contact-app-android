package com.example.contactappv1.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactappv1.R;
import com.example.contactappv1.model.Contact;

import java.util.List;

/**
 * Adapter for the horizontal "Online Now" contact list.
 */
public class OnlineContactAdapter extends RecyclerView.Adapter<OnlineContactAdapter.ViewHolder> {

    private static final int[][] AVATAR_COLORS = {
            {R.color.avatar_1, R.color.avatar_1_text},
            {R.color.avatar_2, R.color.avatar_2_text},
            {R.color.avatar_3, R.color.avatar_3_text},
            {R.color.avatar_4, R.color.avatar_4_text},
            {R.color.avatar_5, R.color.avatar_5_text},
            {R.color.avatar_6, R.color.avatar_6_text},
            {R.color.avatar_7, R.color.avatar_7_text},
    };

    private final List<Contact> onlineContacts;

    public OnlineContactAdapter(List<Contact> onlineContacts) {
        this.onlineContacts = onlineContacts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_online_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = onlineContacts.get(position);

        holder.tvOnlineName.setText(contact.getFirstName());
        holder.tvInitial.setText(contact.getFirstLetter());

        int colorIdx = position % AVATAR_COLORS.length;
        int bgColor = ContextCompat.getColor(holder.itemView.getContext(), AVATAR_COLORS[colorIdx][0]);
        int textColor = ContextCompat.getColor(holder.itemView.getContext(), AVATAR_COLORS[colorIdx][1]);

        holder.llAvatar.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        holder.tvInitial.setTextColor(textColor);
    }

    @Override
    public int getItemCount() {
        return onlineContacts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llAvatar;
        TextView tvInitial;
        TextView tvOnlineName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            llAvatar     = itemView.findViewById(R.id.llAvatar);
            tvInitial    = itemView.findViewById(R.id.tvInitial);
            tvOnlineName = itemView.findViewById(R.id.tvOnlineName);
        }
    }
}
