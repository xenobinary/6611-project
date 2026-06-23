import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class I18nController {
    private static I18nController instance;
    private Properties messages;
    private String currentLanguage;
    private List<I18nListener> listeners;

    private I18nController() {
        listeners = new ArrayList<>();
        currentLanguage = "en";
        messages = new Properties();
        loadLanguage(currentLanguage);
    }

    public static I18nController getInstance() {
        if (instance == null) {
            instance = new I18nController();
        }
        return instance;
    }

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

    public String get(String key) {
        String value = messages.getProperty(key);
        return value != null ? value : "!" + key + "!";
    }

    public String get(String key, String defaultValue) {
        String value = messages.getProperty(key);
        return value != null ? value : defaultValue;
    }

    public void setLanguage(String lang) {
        if (!lang.equals(currentLanguage)) {
            currentLanguage = lang;
            loadLanguage(lang);
            notifyListeners();
        }
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public void addListener(I18nListener listener) {
        listeners.add(listener);
    }

    public void removeListener(I18nListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (I18nListener listener : listeners) {
            listener.onLanguageChanged(currentLanguage);
        }
    }

    public interface I18nListener {
        void onLanguageChanged(String language);
    }

    public static void resetInstance() {
        instance = null;
    }
}
