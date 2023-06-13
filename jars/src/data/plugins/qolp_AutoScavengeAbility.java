package data.plugins;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class qolp_AutoScavengeAbility extends BaseToggleAbility {

    public static boolean isOn() {
        if (getInstance() != null) return getInstance().isActive();
        return false;
    }

    public static void setOn(boolean isOn) {
        if (isOn) {
            getInstance().activate();
        } else {
            getInstance().deactivate();
        }
    }

    private static AbilityPlugin getInstance() {
        return Global.getSector().getPlayerFleet().getAbility("auto_scavenge");
    }

    @Override
    public void advance(float amount) {
        if (amount == 0) {
            return;
        }
        if (!turnedOn) {
            return;
        }
        AbilityPlugin scavenge = Global.getSector().getPlayerFleet().getAbility(Abilities.SCAVENGE);
        if (scavenge == null) {
            return;
        }
        if (scavenge.isUsable()) {
            scavenge.activate();
        }
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        Color gray = Misc.getGrayColor();
        float pad = 10f;
        String status = getStatus();
        LabelAPI title = tooltip.addTitle(spec.getName() + status);
        title.highlightLast(status);
        title.setHighlightColor(gray);
        tooltip.addPara("Enable automatic scavenging of encountered debris fields.", pad);
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    public boolean isTooltipExpandable() {
        return false;
    }

    @Override
    protected void activateImpl() {}

    @Override
    protected void applyEffect(float amount, float level) {}

    @Override
    protected void deactivateImpl() {}

    @Override
    protected void cleanupImpl() {}

    @Override
    protected String getActivationText() {
        return "Auto-scavenging activated";
    }

    @Override
    protected String getDeactivationText() {
        return "Auto-scavenging deactivated";
    }

    private String getStatus() {
        if (turnedOn) {
            return " (on)";
        }
        return " (off)";
    }
}
