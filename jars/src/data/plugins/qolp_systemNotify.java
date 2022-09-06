package data.plugins;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class qolp_systemNotify extends BaseEveryFrameCombatPlugin {

    CombatEngineAPI engine;

    List<ShipAPI> shipsAlreadyReporter = new ArrayList<>();

    float
            textSize = 30;
    Color
            positiveTextColor = Misc.getPositiveHighlightColor(),
            negativeTextColor = Misc.getNegativeHighlightColor();

    boolean allowPlayer = false;

    public static final String ID = "qolp_AutoShieldAfterOverload";
    public static final String SETTINGS_PATH = "QoLPack.ini";

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        Color temp;
        boolean enable = true;
        try {
            JSONObject cfg = Global.getSettings().getMergedJSONForMod(SETTINGS_PATH, ID);
            textSize = cfg.getInt("TextSize");
            enable = cfg.getBoolean("EnableSystemNotify");
            allowPlayer = cfg.getBoolean("AllowForPlayerShip");
            temp = getColor(cfg.getJSONArray("OnTextColor"));
            if (temp.getAlpha() != 0) positiveTextColor = temp;
            temp = getColor(cfg.getJSONArray("OffTextColor"));
            if (temp.getAlpha() != 0) negativeTextColor = temp;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (!enable){
            engine.removePlugin(this);
        }
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) return;
        if (Global.getCurrentState().equals(GameState.TITLE)) return;
        for (ShipAPI ship : engine.getShips()) {
            if (ship == engine.getPlayerShip() && !allowPlayer) continue;
            if (ship.getHullSize().equals(ShipAPI.HullSize.FIGHTER)) continue;
            if (shipsAlreadyReporter.contains(ship)) continue;
            if (ship.getSystem() != null && ship.getSystem().isActive()) {
                engine.addFloatingText(ship.getLocation(), ship.getSystem().getDisplayName(), textSize, positiveTextColor, ship, 2f, 0.75f);
                shipsAlreadyReporter.add(ship);
            }
        }
        List<ShipAPI> cloneList = new ArrayList<>(shipsAlreadyReporter);
        for (ShipAPI ship : cloneList) {
            if (!ship.getSystem().isActive()) {
                shipsAlreadyReporter.remove(ship);
                if (ship.getSystem().getSpecAPI().isToggle() || ship.getSystem().getChargeActiveDur() > 1f) {
                    engine.addFloatingText(ship.getLocation(), ship.getSystem().getDisplayName(), textSize, negativeTextColor, ship, 2f, 0.75f);
                }
            }
        }
    }

    Color getColor(JSONArray c) throws JSONException {
        return new Color(
                Math.min(255, Math.max(0, c.getInt(0))),
                Math.min(255, Math.max(0, c.getInt(1))),
                Math.min(255, Math.max(0, c.getInt(2))),
                Math.min(255, Math.max(0, c.getInt(3)))
        );
    }
}
