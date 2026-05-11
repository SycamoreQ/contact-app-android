package com.example.contactappv1.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Model representing a recent phone call entry.
 * Carries the SQLite row id, timestamp (epoch millis), duration, and label.
 */
public class RecentCall {

    public static final int TYPE_INCOMING = 0;
    public static final int TYPE_OUTGOING = 1;
    public static final int TYPE_MISSED   = 2;

    private final long   id;
    private final String name;
    private final String phone;
    private final int    type;
    private final long   timestamp;  // epoch millis
    private final int    duration;   // seconds
    private final String label;      // "Mobile", "Home", "Work"

    /** Full constructor used when reading from SQLite. */
    public RecentCall(long id, String name, String phone,
                      int type, long timestamp, int duration, String label) {
        this.id        = id;
        this.name      = name;
        this.phone     = phone;
        this.type      = type;
        this.timestamp = timestamp;
        this.duration  = duration;
        this.label     = label;
    }

    public long   getId()        { return id; }
    public String getName()      { return name; }
    public String getPhone()     { return phone; }
    public int    getType()      { return type; }
    public long   getTimestamp() { return timestamp; }
    public int    getDuration()  { return duration; }
    public String getLabel()     { return label != null ? label : "Mobile"; }

    /**
     * Returns a human-readable relative time string, e.g.
     * "10 min ago", "2 hrs ago", "Yesterday", "Last week".
     * Prefixed with the label so the UI shows "Mobile · 10 min ago".
     */
    public String getTime() {
        long now  = System.currentTimeMillis();
        long diff = now - timestamp;

        long mins  = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days  = TimeUnit.MILLISECONDS.toDays(diff);

        String relative;
        if      (mins  < 1)  relative = "Just now";
        else if (mins  < 60) relative = mins + " min ago";
        else if (hours < 24) relative = hours + " hr" + (hours > 1 ? "s" : "") + " ago";
        else if (days  == 1) relative = "Yesterday";
        else if (days  < 7)  relative = new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date(timestamp));
        else if (days  < 14) relative = "Last week";
        else                 relative = new SimpleDateFormat("MMM d", Locale.getDefault()).format(new Date(timestamp));

        return getLabel() + " · " + relative;
    }

    /** Returns the uppercase first letter of the caller's name. */
    public String getFirstLetter() {
        if (name != null && !name.isEmpty()) {
            return name.substring(0, 1).toUpperCase();
        }
        return "?";
    }
}
