package com.example.contactappv1.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.contactappv1.R;
import com.example.contactappv1.db.DatabaseHelper;

/**
 * Keypad tab — phone dialer with numeric keypad and call button.
 */
public class KeypadFragment extends Fragment {

    private TextView tvDialedNumber;
    private TextView tvClear;
    private TextView tvAddContact;
    private final StringBuilder dialedText = new StringBuilder();
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_keypad, container, false);

        dbHelper = DatabaseHelper.getInstance(getContext());

        tvDialedNumber = view.findViewById(R.id.tvDialedNumber);
        tvClear        = view.findViewById(R.id.tvClear);
        tvAddContact   = view.findViewById(R.id.tvAddContact);
        FrameLayout btnCall = view.findViewById(R.id.btnCall);

        // ── Wire up keypad buttons ──
        setupKey(view.findViewById(R.id.btn1), "1", "");
        setupKey(view.findViewById(R.id.btn2), "2", "ABC");
        setupKey(view.findViewById(R.id.btn3), "3", "DEF");
        setupKey(view.findViewById(R.id.btn4), "4", "GHI");
        setupKey(view.findViewById(R.id.btn5), "5", "JKL");
        setupKey(view.findViewById(R.id.btn6), "6", "MNO");
        setupKey(view.findViewById(R.id.btn7), "7", "PQRS");
        setupKey(view.findViewById(R.id.btn8), "8", "TUV");
        setupKey(view.findViewById(R.id.btn9), "9", "WXYZ");
        setupKey(view.findViewById(R.id.btnStar), "*", "");
        setupKey(view.findViewById(R.id.btn0), "0", "+");
        setupKey(view.findViewById(R.id.btnHash), "#", "");

        // ── Clear button ──
        tvClear.setOnClickListener(v -> {
            if (dialedText.length() > 0) {
                dialedText.deleteCharAt(dialedText.length() - 1);
                updateDisplay();
            }
        });

        tvClear.setOnLongClickListener(v -> {
            dialedText.setLength(0);
            updateDisplay();
            return true;
        });

        // ── Add Contact logic ──
        tvAddContact.setOnClickListener(v -> {
            String currentNumber = dialedText.toString();
            if (currentNumber.isEmpty()) return;

            android.widget.EditText input = new android.widget.EditText(getContext());
            input.setHint("Contact Name");
            input.setPadding(64, 32, 64, 32);

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Add to Contacts")
                    .setMessage("Save " + currentNumber + " to contacts?")
                    .setView(input)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String name = input.getText().toString().trim();
                        if (name.isEmpty()) name = "Unknown";
                        long id = dbHelper.insertContact(name, currentNumber, false, false);
                        if (id > 0) {
                            android.widget.Toast.makeText(getContext(), "Contact saved locally!", android.widget.Toast.LENGTH_SHORT).show();
                            
                            // Launch native System Android Contacts to sync
                            Intent contactIntent = new Intent(Intent.ACTION_INSERT);
                            contactIntent.setType(android.provider.ContactsContract.RawContacts.CONTENT_TYPE);
                            contactIntent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, currentNumber);
                            contactIntent.putExtra(android.provider.ContactsContract.Intents.Insert.NAME, name);
                            startActivity(contactIntent);

                            // DO NOT clear dialedText here, so the user can immediately hit Call without retyping!
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // ── Call button → opens system dialer + logs the call ──
        btnCall.setOnClickListener(v -> {
            if (dialedText.length() > 0) {
                String number = dialedText.toString();
                
                // Lookup if contact exists locally
                com.example.contactappv1.model.Contact contact = dbHelper.getContactByPhone(number);
                long cid = (contact != null) ? contact.getId() : -1;
                String cname = (contact != null) ? contact.getName() : "Unknown";

                // Log strictly to our database before launching dialer
                dbHelper.insertCall(cid, cname, number, 
                        com.example.contactappv1.model.RecentCall.TYPE_OUTGOING, 
                        System.currentTimeMillis(), 0, "Mobile");

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + number));
                startActivity(intent);
            }
        });

        return view;
    }

    private void setupKey(View keyView, String number, String letters) {
        TextView tvNum = keyView.findViewById(R.id.tvKeyNumber);
        TextView tvLet = keyView.findViewById(R.id.tvKeyLetters);

        tvNum.setText(number);
        if (!letters.isEmpty()) {
            tvLet.setText(letters);
            tvLet.setVisibility(View.VISIBLE);
        }

        keyView.setOnClickListener(v -> {
            dialedText.append(number);
            updateDisplay();
        });
    }

    private void updateDisplay() {
        tvDialedNumber.setText(dialedText.toString());
        tvClear.setVisibility(dialedText.length() > 0 ? View.VISIBLE : View.INVISIBLE);
        tvAddContact.setVisibility(dialedText.length() > 0 ? View.VISIBLE : View.INVISIBLE);
    }
}
