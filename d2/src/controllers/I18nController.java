import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Singleton controller for internationalization (i18n).
 * Manages language switching by loading locale-specific properties files
 * and notifying registered listeners of language changes.
 */
public class I18nController {
    /** The singleton instance. */
    private static I18nController instance;
    /** The currently loaded message properties. */
    private Properties messages;
    /** The current language code (e.g., "en", "fr", "zh"). */
    private String currentLanguage;
    /** Registered listeners to notify on language change. */
    private List<I18nListener> listeners;

    /** Private constructor; initializes with English as the default language. */
    private I18nController() {
        listeners = new ArrayList<>();
        currentLanguage = "en";
        messages = new Properties();
        loadLanguage(currentLanguage);
    }

    /**
     * Returns the singleton I18nController instance.
     *
     * @return the instance
     */
    public static I18nController getInstance() {
        if (instance == null) {
            instance = new I18nController();
        }
        return instance;
    }

    /** Loads the properties file for the given language code. */
    private void loadLanguage(String lang) {
        messages.clear();
        String fileName = "resources/messages_" + lang + ".properties";
        try {
            InputStream is = getClass().getClassLoader()
                    .getResourceAsStream(fileName);
            if (is == null) {
                is = new FileInputStream("d2/src/" + fileName);
            }
            messages.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            is.close();
        } catch (Exception e) {
            try {
                InputStream is = new FileInputStream(fileName);
                messages.load(new InputStreamReader(is, StandardCharsets.UTF_8));
                is.close();
            } catch (Exception e2) {
                System.err.println("Failed to load language: " + lang + " - " + e.getMessage());
            }
        }
    }

    /**
     * Retrieves a localized message by key, or a fallback string if the key is missing.
     *
     * @param key the i18n key
     * @return the localized message, or "!" + key + "!" if not found
     */
    public String get(String key) {
        String value = messages.getProperty(key);
        return value != null ? value : "!" + key + "!";
    }

    /**
     * Retrieves a localized message by key, or the provided default value if the key is missing.
     *
     * @param key          the i18n key
     * @param defaultValue the fallback value
     * @return the localized message, or the defaultValue if not found
     */
    public String get(String key, String defaultValue) {
        String value = messages.getProperty(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Switches the current language and notifies all listeners.
     * Does nothing if the language is already set.
     *
     * @param lang the language code to switch to
     */
    public void setLanguage(String lang) {
        if (!lang.equals(currentLanguage)) {
            currentLanguage = lang;
            loadLanguage(lang);
            notifyListeners();
        }
    }

    /**
     * Returns the current language code.
     *
     * @return the language code (e.g., "en")
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * Registers a listener to be notified when the language changes.
     *
     * @param listener the listener to add
     */
    public void addListener(I18nListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregisters a language change listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(I18nListener listener) {
        listeners.remove(listener);
    }

    /** Notifies all registered listeners of a language change. */
    private void notifyListeners() {
        for (I18nListener listener : listeners) {
            listener.onLanguageChanged(currentLanguage);
        }
    }

    /**
     * Interface for components that need to react to language changes.
     */
    public interface I18nListener {
        /**
         * Called when the current language has been changed.
         *
         * @param language the new language code
         */
        void onLanguageChanged(String language);
    }

    /**
     * Resets the singleton instance (mainly for testing).
     */
    public static void resetInstance() {
        instance = null;
    }
}
