package com.example.contactappv1.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactappv1.R;
import com.example.contactappv1.model.Contact;

import java.util.List;

/**
 * Adapter for the 3-column favorites grid layout.
 */
public class FavoriteContactAdapter extends RecyclerView.Adapter<FavoriteContactAdapter.FavViewHolder> {

    private static final String[] BG_COLORS  = {"#EDE9FE","#FCE7F3","#DCFCE7","#FEF3C7","#DBEAFE","#FFE4E6","#CCFBF1"};
    private static final String[] TXT_COLORS = {"#7C3AED","#DB2777","#16A34A","#D97706","#2563EB","#E11D48","#0D9488"};

    public interface OnFavoriteLongClickListener {
        void onItemLongClick(int position);
    }

    private final List<Contact> favorites;
    private OnFavoriteLongClickListener longClickListener;

    public void setOnFavoriteLongClickListener(OnFavoriteLongClickListener listener) {
        this.longClickListener = listener;
    }

    public FavoriteContactAdapter(List<Contact> favorites) {
        this.favorites = favorites;
    }

    @NonNull
    @Override
    public FavViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_contact, parent, false);
        return new FavViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FavViewHolder holder, int position) {
        Contact c = favorites.get(position);

        holder.tvFavName.setText(c.getFirstName());
        holder.tvFavPhone.setText(c.getPhone());

        // Avatar
        String initial = c.getFirstLetter();
        holder.tvAvatarInitialsFav.setText(initial);

        int index = Math.abs(c.getName().hashCode()) % BG_COLORS.length;
        holder.tvAvatarInitialsFav.setTextColor(Color.parseColor(TXT_COLORS[index]));
        GradientDrawable gd = (GradientDrawable) holder.llAvatarBgFav.getBackground().mutate();
        gd.setColor(Color.parseColor(BG_COLORS[index]));

        // Tap to call
        holder.itemView.setOnClickListener(v -> {
            com.example.contactappv1.db.DatabaseHelper.getInstance(v.getContext())
                .insertCall(c.getId(), c.getName(), c.getPhone(),
                        com.example.contactappv1.model.RecentCall.TYPE_OUTGOING,
                        System.currentTimeMillis(), 0, "Mobile");
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + c.getPhone()));
            v.getContext().startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(position);
                return true;
            }
            return false;
        });
    }

    public void addContact(Contact contact) {
        favorites.add(contact);
        notifyItemInserted(favorites.size() - 1);
    }

    public void removeContact(int position) {
        favorites.remove(position);
        notifyItemRemoved(position);
    }

    public Contact getContact(int position) {
        return favorites.get(position);
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    static class FavViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarInitialsFav, tvFavName, tvFavPhone;
        LinearLayout llAvatarBgFav;

        FavViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarInitialsFav = itemView.findViewById(R.id.tvAvatarInitialsFav);
            tvFavName           = itemView.findViewById(R.id.tvFavName);
            tvFavPhone          = itemView.findViewById(R.id.tvFavPhone);
            llAvatarBgFav       = itemView.findViewById(R.id.llAvatarBgFav);
        }
    }
}
