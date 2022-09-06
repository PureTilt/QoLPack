package data.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class qolp_AutoShieldAfterSystem implements EveryFrameCombatPlugin {

    boolean
            systemActive = false,
            shieldWasOn = false;

    public static final String ID = "qolp_AutoShieldAfterSystem";
    public static final String SETTINGS_PATH = "QoLPack.ini";

    float
            framesActive = 0,
            maxFrames = 2;

    CombatEngineAPI engine;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        boolean enable = true;
        try {
            JSONObject cfg = Global.getSettings().getMergedJSONForMod(SETTINGS_PATH, ID);
            enable = cfg.getBoolean("EnableAutoShieldOnAfterSystem");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (!enable){
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
        if (player.getSystem() == null || player.getShield() == null || player.getShield().getType().equals(ShieldAPI.ShieldType.PHASE)) return;

        boolean systemActive1 = player.getSystem().isActive();
        if (!systemActive1){
            shieldWasOn = player.getShield().isOn();
        }
        if (systemActive1 && framesActive < maxFrames + 1){
            framesActive++;
        }

        if (framesActive <= maxFrames && systemActive1){
            if (shieldWasOn && player.getShield().isOff()) systemActive = true;
        }

        if (framesActive > maxFrames && systemActive && systemActive1 && player.getShield().isOff()){
            engine.maintainStatusForPlayerShip("qolp_AutoShieldSystem", "graphics/icons/hullsys/fortress_shield.png", "Auto Shield", "Deploy shield after system", false);
        }

        if (!(player.getSystem().isStateActive() || player.getSystem().isChargeup()) && framesActive > 0 ){
            if (systemActive && player.getShield().isOff()){
                player.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
            }
            systemActive = false;
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
