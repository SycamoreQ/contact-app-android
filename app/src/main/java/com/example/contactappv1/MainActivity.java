package com.example.contactappv1;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.contactappv1.fragment.ContactsFragment;
import com.example.contactappv1.fragment.FavoriteFragment;
import com.example.contactappv1.fragment.KeypadFragment;
import com.example.contactappv1.fragment.RecentsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Single-activity host for the Contact App.
 * Manages four fragments via BottomNavigationView using show/hide
 * to preserve fragment state across tab switches.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG_CONTACTS  = "frag_contacts";
    private static final String TAG_RECENTS   = "frag_recents";
    private static final String TAG_FAVORITES = "frag_favorites";
    private static final String TAG_KEYPAD    = "frag_keypad";

    private Fragment activeFragment;
    private Fragment contactsFragment;
    private Fragment recentsFragment;
    private Fragment favoritesFragment;
    private Fragment keypadFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            // First launch — create all fragments and add them, showing only Contacts
            contactsFragment  = new ContactsFragment();
            recentsFragment   = new RecentsFragment();
            favoritesFragment = new FavoriteFragment();
            keypadFragment    = new KeypadFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, contactsFragment, TAG_CONTACTS)
                    .add(R.id.fragment_container, recentsFragment, TAG_RECENTS)
                    .add(R.id.fragment_container, favoritesFragment, TAG_FAVORITES)
                    .add(R.id.fragment_container, keypadFragment, TAG_KEYPAD)
                    .hide(recentsFragment)
                    .hide(favoritesFragment)
                    .hide(keypadFragment)
                    .commit();

            activeFragment = contactsFragment;
        } else {
            // Restore fragments after config change
            contactsFragment  = getSupportFragmentManager().findFragmentByTag(TAG_CONTACTS);
            recentsFragment   = getSupportFragmentManager().findFragmentByTag(TAG_RECENTS);
            favoritesFragment = getSupportFragmentManager().findFragmentByTag(TAG_FAVORITES);
            keypadFragment    = getSupportFragmentManager().findFragmentByTag(TAG_KEYPAD);

            // Determine which was active (default to contacts)
            activeFragment = contactsFragment;
            if (recentsFragment != null && recentsFragment.isVisible()) activeFragment = recentsFragment;
            if (favoritesFragment != null && favoritesFragment.isVisible()) activeFragment = favoritesFragment;
            if (keypadFragment != null && keypadFragment.isVisible()) activeFragment = keypadFragment;
        }

        // ── Tab selection listener ──
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();

            if (id == R.id.nav_contacts) {
                selected = contactsFragment;
            } else if (id == R.id.nav_recents) {
                selected = recentsFragment;
            } else if (id == R.id.nav_favorites) {
                selected = favoritesFragment;
            } else if (id == R.id.nav_keypad) {
                selected = keypadFragment;
            }

            if (selected != null && selected != activeFragment) {
                getSupportFragmentManager().beginTransaction()
                        .hide(activeFragment)
                        .show(selected)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                activeFragment = selected;
            }
            return true;
        });

        // Set initial selection
        bottomNav.setSelectedItemId(R.id.nav_contacts);
    }
}