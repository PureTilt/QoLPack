package data.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.qolp_getSettings;
import org.json.JSONException;
import org.lwjgl.util.vector.Vector2f;

import java.io.IOException;
import java.util.List;

public class qolp_shieldSnapping extends BaseEveryFrameCombatPlugin {

    int snapIfTarget = 59;
    int rememberTarget = 60;
    int shipSide = 61;
    int direction = 62;

    float skippedFrames = 0;


    CombatEngineAPI engine;
    private static shieldMods mode;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        int defaultMode = 0;
        boolean saveMode = false;
        try {
            snapIfTarget = qolp_getSettings.getInt("snapIfTarget");
            rememberTarget = qolp_getSettings.getInt("rememberTarget");
            shipSide = qolp_getSettings.getInt("shipSide");
            direction = qolp_getSettings.getInt("direction");
            defaultMode = qolp_getSettings.getInt("DefaultShieldSnapMode");
            saveMode = qolp_getSettings.getBoolean("SaveModeBetweenEncounters");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (!saveMode || mode == null) {
            switch (defaultMode) {
                case 0:
                    mode = shieldMods.none;
                    break;
                case 1:
                    mode = shieldMods.snapIfTarget;
                    break;
                case 2:
                    mode = shieldMods.rememberTarget;
                    break;
                case 3:
                    mode = shieldMods.shipSide;
                    break;
                case 4:
                    mode = shieldMods.direction;
                    break;
            }
        }
    }

    @Override
    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
        if (engine == null || engine.getPlayerShip() == null) return;
        for (InputEventAPI e : events) {
            if (e.isKeyDownEvent()) {
                if (e.isConsumed()) continue;
                if (e.getEventValue() == snapIfTarget) {
                    if (mode.equals(shieldMods.snapIfTarget)) mode = shieldMods.none;
                    else mode = shieldMods.snapIfTarget;
                } else if (e.getEventValue() == rememberTarget) {
                    if (mode.equals(shieldMods.rememberTarget)) mode = shieldMods.none;
                    else mode = shieldMods.rememberTarget;
                } else if (e.getEventValue() == shipSide) {
                    if (mode.equals(shieldMods.shipSide)) mode = shieldMods.none;
                    else mode = shieldMods.shipSide;
                } else if (e.getEventValue() == direction) {
                    if (mode.equals(shieldMods.direction)) mode = shieldMods.none;
                    else mode = shieldMods.direction;
                }

            }
        }
    }


    ShipAPI mode2Target;
    Float mode3Angle;
    Vector2f mode4Direction;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (skippedFrames < 0.5f) {
            skippedFrames += amount;
            return;
        }
        if (engine == null || engine.getPlayerShip() == null) return;
        ShipAPI player = engine.getPlayerShip();
        if (player.getShield() == null || player.getShield().getType() != ShieldAPI.ShieldType.OMNI) return;
        if (player.getAI() != null) return;
        if (!mode.equals(shieldMods.rememberTarget)) mode2Target = null;
        if (!mode.equals(shieldMods.shipSide)) mode3Angle = null;
        if (!mode.equals(shieldMods.direction)) mode4Direction = null;
//        engine.getViewport().setExternalControl(true);
//        engine.getViewport().setCenter(player.getLocation());
        if (mode.equals(shieldMods.none)) return;
        if (mode.equals(shieldMods.snapIfTarget)) {
            engine.maintainStatusForPlayerShip("qolp_shieldSnapping", "graphics/icons/hullsys/qolp_shieldSnap.png", "Snapping mode", "1 snap if target", false);
            if (player.getShipTarget() != null) {
                player.setShieldTargetOverride(player.getShipTarget().getLocation().x, player.getShipTarget().getLocation().y);
            }
        } else if (mode.equals(shieldMods.rememberTarget)) {
            engine.maintainStatusForPlayerShip("qolp_shieldSnapping", "graphics/icons/hullsys/qolp_shieldSnap.png", "Snapping mode", "2 remember target", false);
            if (mode2Target != null && mode2Target.isHulk()) mode2Target = null;
            if (player.getShipTarget() != null && mode2Target == null) {
                mode2Target = player.getShipTarget();
            }
            if (mode2Target != null) {
                player.setShieldTargetOverride(mode2Target.getLocation().x, mode2Target.getLocation().y);
            } else {
                engine.maintainStatusForPlayerShip("qolp_shieldSnapping", "graphics/icons/hullsys/qolp_shieldSnap.png", "Snapping mode", "2 remember No target", true);
            }

        } else if (mode.equals(shieldMods.shipSide)) {
            engine.maintainStatusForPlayerShip("qolp_shieldSnapping", "graphics/icons/hullsys/qolp_shieldSnap.png", "Snapping mode", "3 ship side", false);
            if (mode3Angle == null) {
                mode3Angle = player.getShield().getFacing() - player.getFacing();
            } else {
                Vector2f side = Misc.getUnitVectorAtDegreeAngle(mode3Angle + player.getFacing());
                side.scale(1000);
                Vector2f target = new Vector2f(side.x + player.getShield().getLocation().x, side.y + player.getShield().getLocation().y);
                player.setShieldTargetOverride(target.x, target.y);
            }
            //engine.maintainStatusForPlayerShip("qolp_shieldSnappingInfo", "graphics/icons/hullsys/qolp_shieldSnap.png", "mode 3 info", player.getFacing() + "/" + mode3Angle + "/" + player.getShield().getFacing(), false);
        } else if (mode.equals(shieldMods.direction)) {
            engine.maintainStatusForPlayerShip("qolp_shieldSnapping", "graphics/icons/hullsys/qolp_shieldSnap.png", "Snapping mode", "4 direction", false);
            if (mode4Direction == null) {
                mode4Direction = Misc.getUnitVectorAtDegreeAngle(player.getShield().getFacing());
                mode4Direction.scale(1000);
            } else {
                Vector2f target = new Vector2f(mode4Direction.x + player.getShield().getLocation().x, mode4Direction.y + player.getShield().getLocation().y);
                player.setShieldTargetOverride(target.x, target.y);
            }
        }
    }

    protected enum shieldMods {
        none,
        snapIfTarget,
        rememberTarget,
        shipSide,
        direction
    }
}
