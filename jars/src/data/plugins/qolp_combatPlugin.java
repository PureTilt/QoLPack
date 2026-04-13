package data.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatLayeredRenderingPlugin;
import data.utils.qolp_getSettings;

public class qolp_combatPlugin extends BaseEveryFrameCombatPlugin {

    @Override
    public void init(CombatEngineAPI engine) {
        if (qolp_getSettings.getBoolean("orderDrawEnable")) {
            CombatLayeredRenderingPlugin layerRenderer = new qolp_drawFleetCommands();
            engine.addLayeredRenderingPlugin(layerRenderer);
        }
    }
}
