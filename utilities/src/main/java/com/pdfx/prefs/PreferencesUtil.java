package com.pdfx.prefs;

import java.util.Optional;
import java.util.prefs.Preferences;

public class PreferencesUtil {

    /**
     * Retrieves the last used directory path from the user preferences.
     *
     * @return A string representing the last used directory path.
     */
    public static String getLastUsedDirectoryPath() {

        Preferences directoryPreference = Preferences.userNodeForPackage(PreferencesUtil.class);
        return directoryPreference.get("LAST_USED_FOLDER", System.getProperty("user.home"));
    }

    /**
     * Stores the given file path as the last used directory path in the user preferences.
     *
     * @param filePath A string representing the file path to store.
     */
    public static void setLastUsedDirectoryPath(String filePath) {

        Preferences directoryPreference = Preferences.userNodeForPackage(PreferencesUtil.class);
        directoryPreference.put("LAST_USED_FOLDER", filePath);
    }

    /**
     * Stores the given session ID in the user preferences.
     *
     * @param sessionId A string representing the session ID to store.
     */
    public static void storeSessionIdLocally(String sessionId) {

        Preferences saveSession = Preferences.userNodeForPackage(PreferencesUtil.class);
        saveSession.put("SESSION_ID", sessionId);
    }

    /**
     * Retrieves the locally stored session ID from the user preferences.
     *
     * @return An Optional containing the locally stored session ID, or empty if no session ID is stored.
     */
    public static Optional<String> getLocallyStoredSessionId() {

        Preferences session = Preferences.userNodeForPackage(PreferencesUtil.class);
        return Optional.ofNullable(session.get("SESSION_ID", null));
    }
}
