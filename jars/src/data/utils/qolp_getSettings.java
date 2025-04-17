package data.utils;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import org.json.JSONException;

import java.awt.*;
import java.io.IOException;

public class qolp_getSettings {

    static final String modID = "pt_qolpack";
    static final String ID = "qolp_modPlugin";
    static final String SETTINGS_PATH = "QoLPack.ini";

    public static boolean getBoolean(String key) throws JSONException, IOException {
        boolean value = false;
        try {
            if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
                value = Boolean.TRUE.equals(LunaSettings.getBoolean(modID, key));
            } else {
                value = Global.getSettings().getMergedJSONForMod(SETTINGS_PATH, modID).getBoolean(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static Integer getInt(String key) throws JSONException, IOException {
        Integer value;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getInt(modID, key);
        } else {
            value = Global.getSettings().getMergedJSONForMod(SETTINGS_PATH, modID).getInt(key);
        }
        return value;
    }

    public static Float getFloat(String key) throws JSONException, IOException {
        Float value;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getFloat(modID, key);
        } else {
            value = (float) Global.getSettings().getMergedJSONForMod(SETTINGS_PATH, modID).getLong(key);
        }
        return value;
    }

    public static String getString(String key) throws JSONException, IOException {
        String value;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getString(modID, key);
        } else {
            value = Global.getSettings().getMergedJSONForMod(SETTINGS_PATH, modID).getString(key);
        }
        return value;
    }

    public static Color getColor(String key) throws JSONException, IOException {
        Color value;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getColor(modID, key);
        } else {
            String iniColor = Global.getSettings().getMergedJSONForMod(SETTINGS_PATH, modID).getString(key);
            String[] colorString = iniColor.replace("[","").replace("]","").split(",");
            value = new Color(
                    Math.min(255, Math.max(0, Integer.parseInt(colorString[0]))),
                    Math.min(255, Math.max(0, Integer.parseInt(colorString[1]))),
                    Math.min(255, Math.max(0, Integer.parseInt(colorString[2]))),
                    Math.min(255, Math.max(0, Integer.parseInt(colorString[3]))));
        }
        return value;
    }
}
