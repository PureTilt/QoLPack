package data.plugins;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.qolp_getSettings;
import org.json.JSONException;

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


    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        Color temp;
        boolean enable = true;
        try {
            textSize = qolp_getSettings.getInt("TextSize");
            enable = qolp_getSettings.getBoolean("EnableSystemNotify");
            allowPlayer = qolp_getSettings.getBoolean("AllowForPlayerShip");
            temp = getColor(qolp_getSettings.getString("OnTextColor"));
            if (temp.getAlpha() >= 1) positiveTextColor = temp;
            temp = getColor(qolp_getSettings.getString("OffTextColor"));
            if (temp.getAlpha() >= 1) negativeTextColor = temp;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (!enable) {
            engine.removePlugin(this);
        }
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) return;
        if (Global.getCurrentState().equals(GameState.TITLE)) return;
        for (ShipAPI ship : engine.getShips()) {
            if (!allowPlayer && ship == engine.getPlayerShip()) continue;
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

    Color getColor(String c) throws JSONException {
        String[] colorString = c.replace("[","").replace("]","").split(",");
        return new Color(
                Math.min(255, Math.max(0, Integer.parseInt(colorString[0]))),
                Math.min(255, Math.max(0, Integer.parseInt(colorString[1]))),
                Math.min(255, Math.max(0, Integer.parseInt(colorString[2]))),
                Math.min(255, Math.max(0, Integer.parseInt(colorString[3])))
        );
    }
}
