package com.example.contactappv1.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactappv1.R;
import com.example.contactappv1.model.RecentCall;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for recent call history list.
 * Shows call type icon (incoming/outgoing/missed), name, time, and a call-back button.
 * Supports search filtering by name or phone number.
 */
public class RecentCallAdapter extends RecyclerView.Adapter<RecentCallAdapter.RecentCallViewHolder> {

    private static final String[] BG_COLORS  = {"#EDE9FE","#FCE7F3","#DCFCE7","#FEF3C7","#DBEAFE","#FFE4E6","#CCFBF1"};
    private static final String[] TXT_COLORS = {"#7C3AED","#DB2777","#16A34A","#D97706","#2563EB","#E11D48","#0D9488"};

    private final List<RecentCall> originalCalls;
    private List<RecentCall> filteredCalls;

    public RecentCallAdapter(List<RecentCall> calls) {
        this.originalCalls = new ArrayList<>(calls);
        this.filteredCalls = new ArrayList<>(calls);
    }

    public void updateItems(List<RecentCall> newCalls) {
        this.originalCalls.clear();
        this.originalCalls.addAll(newCalls);
        this.filteredCalls.clear();
        this.filteredCalls.addAll(newCalls);
        notifyDataSetChanged();
    }

    /** Filter the list by name or phone number. */
    public void filter(String query) {
        filteredCalls.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredCalls.addAll(originalCalls);
        } else {
            String lower = query.toLowerCase().trim();
            for (RecentCall c : originalCalls) {
                if (c.getName().toLowerCase().contains(lower)
                        || c.getPhone().contains(lower)) {
                    filteredCalls.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentCallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_call, parent, false);
        return new RecentCallViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentCallViewHolder holder, int position) {
        RecentCall call = filteredCalls.get(position);

        holder.tvRecentName.setText(call.getName());
        holder.tvRecentTime.setText(call.getTime());

        // ── Call type icon + name color ──
        switch (call.getType()) {
            case RecentCall.TYPE_INCOMING:
                holder.ivCallType.setImageResource(R.drawable.ic_call_incoming);
                holder.tvRecentName.setTextColor(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
                break;
            case RecentCall.TYPE_OUTGOING:
                holder.ivCallType.setImageResource(R.drawable.ic_call_outgoing);
                holder.tvRecentName.setTextColor(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
                break;
            case RecentCall.TYPE_MISSED:
                holder.ivCallType.setImageResource(R.drawable.ic_call_missed);
                holder.tvRecentName.setTextColor(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.call_red));
                break;
        }

        // ── Avatar ──
        String initial = call.getFirstLetter();
        holder.tvAvatarInitialsRecent.setText(initial);

        int index = Math.abs(call.getName().hashCode()) % BG_COLORS.length;
        holder.tvAvatarInitialsRecent.setTextColor(Color.parseColor(TXT_COLORS[index]));
        GradientDrawable gd = (GradientDrawable) holder.llAvatarBgRecent.getBackground().mutate();
        gd.setColor(Color.parseColor(BG_COLORS[index]));

        // ── Call-back button ──
        holder.btnCallBack.setOnClickListener(v -> {
            com.example.contactappv1.db.DatabaseHelper db = com.example.contactappv1.db.DatabaseHelper.getInstance(v.getContext());
            com.example.contactappv1.model.Contact contact = db.getContactByPhone(call.getPhone());
            long cid = (contact != null) ? contact.getId() : -1;
            String cname = (contact != null) ? contact.getName() : call.getName();
            
            db.insertCall(cid, cname, call.getPhone(),
                        com.example.contactappv1.model.RecentCall.TYPE_OUTGOING,
                        System.currentTimeMillis(), 0, "Mobile");
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + call.getPhone()));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredCalls.size();
    }

    static class RecentCallViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarInitialsRecent, tvRecentName, tvRecentTime;
        LinearLayout llAvatarBgRecent;
        ImageView ivCallType;
        FrameLayout btnCallBack;

        RecentCallViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarInitialsRecent = itemView.findViewById(R.id.tvAvatarInitialsRecent);
            tvRecentName           = itemView.findViewById(R.id.tvRecentName);
            tvRecentTime           = itemView.findViewById(R.id.tvRecentTime);
            llAvatarBgRecent       = itemView.findViewById(R.id.llAvatarBgRecent);
            ivCallType             = itemView.findViewById(R.id.ivCallType);
            btnCallBack            = itemView.findViewById(R.id.btnCallBack);
        }
    }
}
