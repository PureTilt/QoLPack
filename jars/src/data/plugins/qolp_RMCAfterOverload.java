package data.plugins;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import data.utils.qolp_getSettings;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class qolp_RMCAfterOverload extends BaseEveryFrameCombatPlugin {


    boolean
            activateRMC = false;
    private CombatEngineAPI engine;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        boolean enable = true;
        try {
            enable = qolp_getSettings.getBoolean("EnableAutoShieldOnAfterOverload");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (!enable) {
            engine.removePlugin(this);
        }
    }

    @Override
    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
        if (engine == null || engine.getPlayerShip() == null) return;
        ShipAPI player = engine.getPlayerShip();
        if (!player.getFluxTracker().isOverloadedOrVenting()) return;
        for (InputEventAPI e : events) {
            if (e.isConsumed()) continue;
            if (e.isRMBDownEvent()) {
                activateRMC = !activateRMC;
            }
        }
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null || engine.getPlayerShip() == null) return;
        ShipAPI player = engine.getPlayerShip();
        if (activateRMC && !player.getFluxTracker().isOverloadedOrVenting()) {
            if (player.getVariant().getHullSpec().getShieldType() != ShieldAPI.ShieldType.NONE) {
                player.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
                activateRMC = false;
            }
        }
        if (!player.getFluxTracker().isOverloadedOrVenting()) {
            activateRMC = false;
        }
        if (activateRMC) {
            String text = "after overload";
            if (player.getFluxTracker().isVenting()) text = "after venting";
            if (player.getVariant().getHullSpec().getShieldType() != ShieldAPI.ShieldType.NONE && player.getVariant().getHullSpec().getShieldType() != ShieldAPI.ShieldType.PHASE) {

                engine.maintainStatusForPlayerShip("qolp_AutoShield", "graphics/icons/hullsys/fortress_shield.png", "Auto Shield", "Deploy shield " + text, false);
            } else if (player.getVariant().getHullSpec().getShieldType() == ShieldAPI.ShieldType.PHASE) {
                engine.maintainStatusForPlayerShip("qolp_AutoShield", "graphics/icons/hullsys/phase_cloak.png", "Auto Phase", "Enable phase " + text, false);
            }
        }
    }
}
