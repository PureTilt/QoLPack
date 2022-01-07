package data.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class qolp_modPlugin extends BaseModPlugin {

    public static final String ID = "qolp_modPlugin";
    public static final String SETTINGS_PATH = "QoLPack.ini";

    @Override
    public void onGameLoad(boolean newGame) {
        boolean clock = true;
        try {
            JSONObject cfg = Global.getSettings().getMergedJSONForMod(SETTINGS_PATH, ID);
            clock = cfg.getBoolean("EnableClock");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (clock && !Global.getSector().hasScript(qolp_clock.class)) {
            Global.getSector().addTransientScript(new qolp_clock());
        }
    }
}
