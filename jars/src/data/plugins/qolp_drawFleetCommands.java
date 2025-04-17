package data.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static data.utils.qolp_getSettings.getBoolean;
import static data.utils.qolp_getSettings.getFloat;

public class qolp_drawFleetCommands extends BaseCombatLayeredRenderingPlugin {

    List<CombatFleetManagerAPI.AssignmentInfo> ordersDefence = new ArrayList<>();
    List<CombatFleetManagerAPI.AssignmentInfo> ordersAvoid = new ArrayList<>();
    List<CombatFleetManagerAPI.AssignmentInfo> ordersRally = new ArrayList<>();
    List<CombatFleetManagerAPI.AssignmentInfo> ordersCiv = new ArrayList<>();

    SpriteAPI
            defend = Global.getSettings().getSprite("warroom", "icon_task_defend"),
            avoid = Global.getSettings().getSprite("warroom", "icon_task_avoid"),
            rally = Global.getSettings().getSprite("warroom", "icon_task_rally_task_force"),
            civ = Global.getSettings().getSprite("warroom", "icon_task_rally_civilian_craft");

    {
        float sizeMulti = 1;
        try {
            sizeMulti = getFloat("orderSize");
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
        defend.setSize(defend.getWidth() * sizeMulti, defend.getHeight() * sizeMulti);
        avoid.setSize(avoid.getWidth() * sizeMulti, avoid.getHeight() * sizeMulti);
        rally.setSize(rally.getWidth() * sizeMulti, rally.getHeight() * sizeMulti);
        civ.setSize(civ.getWidth() * sizeMulti, civ.getHeight() * sizeMulti);
    }

    public void advance(float amount) {

        //Global.getLogger(qolp_drawFleetCommands.class).info("i run");
        ordersDefence.clear();
        ordersAvoid.clear();
        ordersRally.clear();
        ordersCiv.clear();
        for (CombatFleetManagerAPI.AssignmentInfo order : Global.getCombatEngine().getFleetManager(Global.getCombatEngine().getPlayerShip().getOwner()).getTaskManager(false).getAllAssignments()) {
            if (order.getType() == CombatAssignmentType.DEFEND) {
                ordersDefence.add(order);
            }
            if (order.getType() == CombatAssignmentType.AVOID) {
                ordersAvoid.add(order);
            }
            if (order.getType() == CombatAssignmentType.RALLY_TASK_FORCE) {
                ordersRally.add(order);
            }
            if (order.getType() == CombatAssignmentType.RALLY_CIVILIAN) {
                ordersCiv.add(order);
            }
        }
    }

    public float getRenderRadius() {
        return 10000;
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        EnumSet<CombatEngineLayers> set = EnumSet.noneOf(CombatEngineLayers.class);
        set.add(CombatEngineLayers.UNDER_SHIPS_LAYER);
        set.add(CombatEngineLayers.ABOVE_SHIPS_LAYER);
        return set;
        //return EnumSet.allOf(CombatEngineLayers.class);
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if (layer == CombatEngineLayers.UNDER_SHIPS_LAYER) {
            for (AssignmentInfo order : ordersDefence) {
                defend.renderAtCenter(order.getTarget().getLocation().x, order.getTarget().getLocation().y);
            }
            for (AssignmentInfo order : ordersAvoid) {
                avoid.renderAtCenter(order.getTarget().getLocation().x, order.getTarget().getLocation().y);
            }
            for (AssignmentInfo order : ordersRally) {
                rally.renderAtCenter(order.getTarget().getLocation().x, order.getTarget().getLocation().y);
            }
            for (AssignmentInfo order : ordersCiv) {
                civ.renderAtCenter(order.getTarget().getLocation().x, order.getTarget().getLocation().y);
            }
        }
    }
}
