import os

drawable_dir = r"c:\Users\anant\Downloads\MAD\contactappv1\app\src\main\res\drawable"
os.makedirs(drawable_dir, exist_ok=True)

drawables = {
    # ---- SEARCH BAR: white with light border ----
    "bg_search_bar.xml": '''<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="#FFFFFF"/>
    <stroke android:width="1.5dp" android:color="#E5E7EB"/>
    <corners android:radius="28dp"/>
</shape>''',

    # ---- AVATAR CIRCLE (base, tinted in code) ----
    "bg_circle.xml": '''<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#E8D5FF"/>
</shape>''',

    # ---- ONLINE DOT ----
    "bg_online_dot.xml": '''<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#22C55E"/>
    <stroke android:width="2dp" android:color="#FFFFFF"/>
</shape>''',

    # ---- FAB: Purple filled circle ----
    "bg_fab.xml": '''<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#4F46E5"/>
</shape>''',

    # ---- CALL BUTTON: Outline circle (transparent + green stroke) ----
    "bg_call_btn.xml": '''<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#00000000"/>
    <stroke android:width="1.5dp" android:color="#D1D5DB"/>
</shape>''',

    # ---- MSG BUTTON: Outline circle (transparent + grey stroke) ----
    "bg_msg_btn.xml": '''<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#00000000"/>
    <stroke android:width="1.5dp" android:color="#D1D5DB"/>
</shape>''',

    # ---- HEADER ACTION BTN ----
    "bg_header_action.xml": '''<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="#FFFFFF"/>
    <corners android:radius="14dp"/>
    <stroke android:width="1dp" android:color="#E5E7EB"/>
</shape>''',

    # ---- NAV ACTIVE INDICATOR DOT ----
    "bg_nav_dot.xml": '''<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#4F46E5"/>
</shape>''',

    # ---- BOTTOM NAV BACKGROUND ----
    "bg_bottom_nav.xml": '''<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="#FFFFFF"/>
    <stroke android:width="1dp" android:color="#F3F4F6"/>
</shape>''',

    # ---- CALL ICON ----
    "ic_call.xml": '''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="20dp"
    android:height="20dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#22C55E"
        android:pathData="M6.62,10.79c1.44,2.83 3.76,5.14 6.59,6.59l2.2,-2.2c0.27,-0.27 0.67,-0.36 1.02,-0.24 1.12,0.37 2.33,0.57 3.57,0.57 0.55,0 1,0.45 1,1L21,20c0,0.55 -0.45,1 -1,1 -9.39,0 -17,-7.61 -17,-17 0,-0.55 0.45,-1 1,-1h3.5c0.55,0 1,0.45 1,1 0,1.25 0.2,2.45 0.57,3.57 0.11,0.35 0.03,0.74 -0.25,1.02l-2.2,2.2z"/>
</vector>''',

    # ---- MESSAGE ICON ----
    "ic_message.xml": '''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="20dp"
    android:height="20dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#4F46E5"
        android:pathData="M20,2L4,2C2.9,2 2,2.9 2,4v18l4,-4h14c1.1,0 2,-0.9 2,-2L22,4C22,2.9 21.1,2 20,2zM20,16L5.17,16L4,17.17L4,4h16v12zM7,9h10v2L7,11zM7,12h7v2L7,14z"/>
</vector>''',

    # ---- SEARCH ICON ----
    "ic_search.xml": '''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="20dp"
    android:height="20dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#00000000"
        android:strokeColor="#9CA3AF"
        android:strokeWidth="2"
        android:strokeLineCap="round"
        android:pathData="M11,11m-7,0a7,7 0,1 0,14 0a7,7 0,1 0,-14 0"/>
    <path
        android:fillColor="#00000000"
        android:strokeColor="#9CA3AF"
        android:strokeWidth="2"
        android:strokeLineCap="round"
        android:pathData="M16.5,16.5 L21,21"/>
</vector>''',

    # ---- PLUS ICON ----
    "ic_plus.xml": '''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="26dp"
    android:height="26dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path android:fillColor="#FFFFFF"
        android:pathData="M19,13h-6v6h-2v-6L5,13v-2h6L11,5h2v6h6v2z"/>
</vector>''',

    # ---- CHAT BUBBLE ICON ----
    "ic_chat.xml": '''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="22dp"
    android:height="22dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#00000000"
        android:strokeColor="#1F2937"
        android:strokeWidth="1.8"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:pathData="M21,15a2,2 0,0 1,-2 2H7l-4,4V5a2,2 0,0 1,2 -2h14a2,2 0,0 1,2 2z"/>
</vector>''',

    # ---- NAV: RECENT ----
    "ic_recent.xml": '''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#00000000"
        android:strokeColor="#9CA3AF"
        android:strokeWidth="1.8"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:pathData="M12,8v4l3,3m6,-3a9,9 0,1 1,-18 0a9,9 0,0 1,18 0z"/>
</vector>''',

    # ---- NAV: CONTACTS (active) ----
    "ic_contacts.xml": '''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#4F46E5"
        android:pathData="M12,12c2.21,0 4,-1.79 4,-4s-1.79,-4 -4,-4 -4,1.79 -4,4 1.79,4 4,4zM12,14c-2.67,0 -8,1.34 -8,4v2h16v-2c0,-2.66 -5.33,-4 -8,-4z"/>
</vector>''',

    # ---- NAV: FAVORITE ----
    "ic_favorite.xml": '''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#00000000"
        android:strokeColor="#9CA3AF"
        android:strokeWidth="1.8"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:pathData="M12,17.27L18.18,21l-1.64,-7.03L22,9.24l-7.19,-0.61L12,2 9.19,8.63 2,9.24l5.46,4.73L5.82,21z"/>
</vector>''',

    # ---- NAV: KEYPAD ----
    "ic_keypad.xml": '''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#9CA3AF"
        android:pathData="M5,5m-1.5,0a1.5,1.5 0,1 0,3 0a1.5,1.5 0,1 0,-3 0"/>
    <path
        android:fillColor="#9CA3AF"
        android:pathData="M12,5m-1.5,0a1.5,1.5 0,1 0,3 0a1.5,1.5 0,1 0,-3 0"/>
    <path
        android:fillColor="#9CA3AF"
        android:pathData="M19,5m-1.5,0a1.5,1.5 0,1 0,3 0a1.5,1.5 0,1 0,-3 0"/>
    <path
        android:fillColor="#9CA3AF"
        android:pathData="M5,12m-1.5,0a1.5,1.5 0,1 0,3 0a1.5,1.5 0,1 0,-3 0"/>
    <path
        android:fillColor="#9CA3AF"
        android:pathData="M12,12m-1.5,0a1.5,1.5 0,1 0,3 0a1.5,1.5 0,1 0,-3 0"/>
    <path
        android:fillColor="#9CA3AF"
        android:pathData="M19,12m-1.5,0a1.5,1.5 0,1 0,3 0a1.5,1.5 0,1 0,-3 0"/>
    <path
        android:fillColor="#9CA3AF"
        android:pathData="M5,19m-1.5,0a1.5,1.5 0,1 0,3 0a1.5,1.5 0,1 0,-3 0"/>
    <path
        android:fillColor="#9CA3AF"
        android:pathData="M12,19m-1.5,0a1.5,1.5 0,1 0,3 0a1.5,1.5 0,1 0,-3 0"/>
    <path
        android:fillColor="#9CA3AF"
        android:pathData="M19,19m-1.5,0a1.5,1.5 0,1 0,3 0a1.5,1.5 0,1 0,-3 0"/>
</vector>''',
}

for name, content in drawables.items():
    path = os.path.join(drawable_dir, name)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"OK: {name}")

print("\\nAll drawables generated!")
