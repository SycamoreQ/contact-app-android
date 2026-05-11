package com.example.contactappv1.model;

/**
 * Model representing a single contact entry.
 * The {@code id} field mirrors the SQLite primary key;
 * use id = -1 when the contact has not yet been persisted.
 */
public class Contact {

    private final long    id;
    private final String  name;
    private final String  phone;
    private final boolean online;
    private final boolean favorite;

    /** Constructor used when reading from SQLite (id is known). */
    public Contact(long id, String name, String phone, boolean online, boolean favorite) {
        this.id       = id;
        this.name     = name;
        this.phone    = phone;
        this.online   = online;
        this.favorite = favorite;
    }

    /** Convenience constructor for transient objects (id = -1). */
    public Contact(String name, String phone, boolean online, boolean favorite) {
        this(-1, name, phone, online, favorite);
    }

    public long    getId()       { return id; }
    public String  getName()     { return name; }
    public String  getPhone()    { return phone; }
    public boolean isOnline()    { return online; }
    public boolean isFavorite()  { return favorite; }

    /** Returns the uppercase first letter of the contact's name. */
    public String getFirstLetter() {
        if (name != null && !name.isEmpty()) {
            return name.substring(0, 1).toUpperCase();
        }
        return "?";
    }

    /** Returns just the first name for compact displays. */
    public String getFirstName() {
        if (name != null && name.contains(" ")) {
            return name.split(" ")[0];
        }
        return name;
    }
}
