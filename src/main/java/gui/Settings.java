package gui;

import model.IO;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class Settings {
    private Settings () {
    }

    /*
     * Settings
     */
    public static boolean USE_NATIVE_TXT_EDITOR = false;
    private static final String use_native_txt_editor = "use_native_txt_editor";

    public static String WORKSPACE_LOCATION = "user.dir";
    private static final String workspace_location = "workspace_location";

    /*
     * Class stuff
     */
    public static final Map<String, String[]> about = createAbout();
    public static final Properties properties = new Properties();
    public static final int OPTION_INDEX = 0, ABOUT_INDEX = 1, TYPE_INDEX = 2;

    // ================================================================================= //
    // Methods that must be changed every time a new setting variable is introduced
    // ================================================================================= //
    private static Map<String, String[]> createAbout () {
        Map<String, String[]> about = new HashMap<>();

        about.put(use_native_txt_editor, // Key
                new String[]{"Native Preview", // Display name
                        "Try to use the native editor for some previews. As of 2017-11-09, will cause a crash on Ubuntu 16.04.", // about
                        Boolean.class.getCanonicalName() + ""});

        about.put(workspace_location, // Key
                new String[]{"Workspace Location", // Display name
                        "Recommended: \"user.dir\". Use with caution. CopyPasta must be restarted for this to take effect." + "\nDefault location (Windows 10): \"%UserProfile%/CopyPasta/workspace\"", // about
                        String.class.getCanonicalName() + ""});

        return about;
    }

    public static void loadFromProperties () {
        try {
            USE_NATIVE_TXT_EDITOR = Boolean.parseBoolean((String) properties.get(use_native_txt_editor));

            WORKSPACE_LOCATION = (String) properties.get(workspace_location);
            if (WORKSPACE_LOCATION == null || WORKSPACE_LOCATION.equals("null"))
                WORKSPACE_LOCATION = "user.dir";
        } catch (Exception e) {
            IO.showExceptionAlert(e);
        }
    }

    public static void putToProperties () {
        putValue(use_native_txt_editor, "" + USE_NATIVE_TXT_EDITOR);
        putValue(workspace_location, "" + WORKSPACE_LOCATION);
    }
    // endregion

    // ================================================================================= //
    // Other stuff
    // ================================================================================= //

    public static final void loadSettingsFile () {
        try {
            properties.load(new FileReader(Tools.SETTINGS_FILE));
        } catch (IOException e) {
        }
        loadFromProperties();
    }

    public static void putValue (String key, String value) {
        properties.put(key, value);
    }

    public static final void storeStoreSettingsFile () {
        try {
            new FileOutputStream(Tools.SETTINGS_FILE);

            putToProperties();

            properties.store(new FileOutputStream(Tools.SETTINGS_FILE), "CopyPasta settings file\nVersion: " + Tools.VERSION);
        } catch (IOException e) {
            IO.showExceptionAlert(e);
        }
    }

    public static void setRunningFile (boolean value) {
        IO.printStringToFile(value + "", Tools.IS_RUNNING_FILE);
    }

    public static boolean getRunningFile () {
        return Boolean.parseBoolean(IO.getFileAsString(Tools.IS_RUNNING_FILE).replaceAll("\\s+", ""));
    }
}
