package dnd.jon.spellbook;

class GlobalInfo {

    static final Version VERSION = new Version(4,4,2);
    static final String VERSION_CODE = VERSION.string();

    // We don't always want to show an update message
    // i.e. for updates that are pure bugfixes, the old message may be
    // more useful to users
    static final Version UPDATE_LOG_VERSION = new Version(4,4,2);
    static final String UPDATE_LOG_CODE = UPDATE_LOG_VERSION.string();
    static final int UPDATE_LOG_DESCRIPTION_ID = R.string.update_04_04_02_description;

    static final int ANDROID_VERSION = android.os.Build.VERSION.SDK_INT;

}
