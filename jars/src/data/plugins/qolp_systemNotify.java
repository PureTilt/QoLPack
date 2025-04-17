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

import static data.utils.qolp_getSettings.*;

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
            textSize = getInt("TextSize");
            enable = getBoolean("EnableSystemNotify");
            allowPlayer = getBoolean("AllowForPlayerShip");
            temp = getColor("OnTextColor");
            if (getBoolean("systemNotifcustomColors")) positiveTextColor = temp;
            temp = getColor("OffTextColor");
            if (getBoolean("systemNotifcustomColors")) negativeTextColor = temp;
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
}
