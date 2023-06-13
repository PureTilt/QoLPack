package data.plugins;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import data.utils.qolp_getSettings;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class qolp_AutoShieldAfterSystem implements EveryFrameCombatPlugin {

    boolean
            systemActive = false,
            shieldWasOn = false;

    int
            framesActive = 0,
            maxFrames = 2;

    CombatEngineAPI engine;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        boolean enable = true;
        try {
            enable = qolp_getSettings.getBoolean("EnableAutoShieldOnAfterSystem");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (!enable) {
            engine.removePlugin(this);
        }
    }

    @Override
    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {

    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null || engine.getPlayerShip() == null) return;
        ShipAPI player = engine.getPlayerShip();
        if (player.getSystem() == null || player.getShield() == null || player.getShield().getType().equals(ShieldAPI.ShieldType.PHASE) || player.getSystem().getSpecAPI().isShieldAllowed())
            return;

        boolean systemCurrentlyActive = player.getSystem().isActive() || player.getSystem().isChargeup();

        if (systemCurrentlyActive && framesActive < maxFrames + 1) {
            framesActive++;
        }

        if (framesActive == 1) shieldWasOn = player.getShield().isOn();
        if (framesActive == 2 && shieldWasOn) shieldWasOn = !player.getShield().isOn();

        if (framesActive <= maxFrames && systemCurrentlyActive) {
            if (shieldWasOn && player.getShield().isOff()) systemActive = true;
        }

        //engine.maintainStatusForPlayerShip("qolp_AutoShieldSystem2", "graphics/icons/hullsys/fortress_shield.png", "Auto Shield", framesActive + "/" + maxFrames, false);
        //engine.maintainStatusForPlayerShip("qolp_AutoShieldSystem3", "graphics/icons/hullsys/fortress_shield.png", "shield on", shieldWasOn + "", false);

        if (systemActive && systemCurrentlyActive && player.getShield().isOff()) {
            engine.maintainStatusForPlayerShip("qolp_AutoShieldSystem", "graphics/icons/hullsys/fortress_shield.png", "Auto Shield", "Deploy shield after system", false);
        }

        if (framesActive > 1 && shieldWasOn) {
            engine.maintainStatusForPlayerShip("qolp_AutoShieldSystem", "graphics/icons/hullsys/fortress_shield.png", "Auto Shield", "Deploy shield after system", false);
        }

        if (!(player.getSystem().isStateActive() || player.getSystem().isChargeup()) && framesActive > 0) {
            if (shieldWasOn && player.getShield().isOff()) {
                player.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
            }
            shieldWasOn = false;
            framesActive = 0;
        }
        //engine.maintainStatusForPlayerShip("qolp_AutoShieldSystem2", "graphics/icons/hullsys/fortress_shield.png", "Auto Shield", systemActive + "", false);

        //Global.getLogger(qolp_AutoShieldAfterSystem.class).info(player.getSystem().isActive() + "/" + player.getShield().isOn() + "/" + framesActive);
//        engine.maintainStatusForPlayerShip("qolp_qutoShieldSystem", null, "info", shieldWasOn + "",false);
//        engine.maintainStatusForPlayerShip("qolp_qutoShieldSystem2", null, "info2", framesActive + "",false);
    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {

    }

    @Override
    public void renderInUICoords(ViewportAPI viewport) {

    }

}
