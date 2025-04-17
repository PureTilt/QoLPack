package data.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatLayeredRenderingPlugin;
import data.utils.qolp_getSettings;
import org.json.JSONException;

import java.io.IOException;

public class qolp_combatPlugin extends BaseEveryFrameCombatPlugin {

    @Override
    public void init(CombatEngineAPI engine) {
        try {
            if (qolp_getSettings.getBoolean("EnableClock")) {
                CombatLayeredRenderingPlugin layerRenderer = new qolp_drawFleetCommands();
                engine.addLayeredRenderingPlugin(layerRenderer);
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();;
        }
    }
}
