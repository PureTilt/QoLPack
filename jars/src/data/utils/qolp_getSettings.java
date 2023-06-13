package data.utils;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import org.json.JSONException;

import java.io.IOException;

public class qolp_getSettings {

    static final String modID = "pt_qolpack";
    static final String ID = "qolp_modPlugin";
    static final String SETTINGS_PATH = "QoLPack.ini";

    public static boolean getBoolean(String key) throws JSONException, IOException {
        boolean value = false;
        try {
            if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
                value = LunaSettings.getBoolean(modID, key);
            } else {
                value = Global.getSettings().getMergedJSONForMod(SETTINGS_PATH, modID).getBoolean(key);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return value;
    }

    public static Integer getInt(String key) throws JSONException, IOException {
        Integer value = null;
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
        {
            value = LunaSettings.getInt(modID, key);
        } else {
            value = Global.getSettings().getMergedJSONForMod(SETTINGS_PATH, modID).getInt(key);
        }
        return value;
    }

    public static String getString(String key) throws JSONException, IOException {
        String value = null;
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
        {
            value = LunaSettings.getString(modID, key);
        } else {
            value = Global.getSettings().getMergedJSONForMod(SETTINGS_PATH, modID).getString(key);
        }
        return value;
    }
}
