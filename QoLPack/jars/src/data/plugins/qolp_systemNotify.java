package data.plugins;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class qolp_systemNotify extends BaseEveryFrameCombatPlugin {

    CombatEngineAPI engine;
    List<ShipAPI> shipsAlreadyReporter = new ArrayList<>();
    float
    textSize = 30;
    Color textColor = new Color(255,0,0,255);

    public static final String ID = "qolp_AutoShieldAfterOverload";
    public static final String SETTINGS_PATH = "QoLPack.ini";


    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) return;
        if (Global.getCurrentState().equals(GameState.TITLE)) return;
        for (ShipAPI ship : engine.getShips()){
            if (ship == engine.getPlayerShip()) continue;
            if (shipsAlreadyReporter.contains(ship)) continue;
            if (ship.getSystem() != null && ship.getSystem().isActive()){
                engine.addFloatingText(ship.getLocation(), "System was activated" + "\n" + ship.getSystem().getDisplayName(), textSize, textColor, ship, 0.1f, 0.25f);
                //engine.addFloatingText(new Vector2f(ship.getLocation().x, ship.getLocation().y + textSize), ship.getSystem().getDisplayName(), textSize, textColor, ship, 0.25f, 0.25f);
                shipsAlreadyReporter.add(ship);
            }
        }
        List<ShipAPI> cloneList = new ArrayList<>(shipsAlreadyReporter);
        for (ShipAPI ship : cloneList){
            if (!ship.getSystem().isActive()){
                shipsAlreadyReporter.remove(ship);
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

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        try {
            JSONObject cfg = Global.getSettings().getMergedJSONForMod(SETTINGS_PATH, ID);
            textSize = cfg.getInt("TextSize");
            textColor = getColor(cfg.getJSONArray("TextColor"));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
