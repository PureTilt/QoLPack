package data.plugins;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.HostileFleetNearbyAndAware;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class qolp_AutoScavengeAbility extends BaseToggleAbility {

    float maxTime = 2;
    float timer = maxTime;

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
        timer += amount;
        if (!turnedOn) {
            return;
        }
        AbilityPlugin scavenge = Global.getSector().getPlayerFleet().getAbility(Abilities.SCAVENGE);
        if (scavenge == null) {
            return;
        }
        if (timer >= maxTime && scavenge.isUsable()) {
            if (hostileCHeck()){
                timer = 0;
            } else {
                scavenge.activate();
            }
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

    public boolean hostileCHeck() {

        //float range = params.get(0).getFloat(memoryMap);

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        for (CampaignFleetAPI fleet : playerFleet.getContainingLocation().getFleets()) {
            if (fleet.getAI() == null) continue; // dormant Remnant fleets
            if (fleet.getFaction().isPlayerFaction()) continue;
            if (fleet.isStationMode()) continue;

            if (!fleet.isHostileTo(playerFleet)) continue;
            if (fleet.getBattle() != null) continue;

            if (Misc.isInsignificant(fleet)) {
                continue;
            }


            SectorEntityToken.VisibilityLevel level = playerFleet.getVisibilityLevelTo(fleet);
//			MemoryAPI mem = fleet.getMemoryWithoutUpdate();
//			if (!mem.contains(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_OFF) &&
//					!mem.contains(MemFlags.MEMORY_KEY_PURSUE_PLAYER)) {
//				if (level == VisibilityLevel.NONE) continue;
//			}
            if (level == SectorEntityToken.VisibilityLevel.NONE) continue;

            if (fleet.getFleetData().getMembersListCopy().isEmpty()) continue;

            float dist = Misc.getDistance(playerFleet.getLocation(), fleet.getLocation());
            if (dist > 1500f) continue;

            //fleet.getAI().pickEncounterOption(null, playerFleet, true);
            if (fleet.getAI() instanceof ModularFleetAIAPI ai) {
                if (ai.getTacticalModule() != null &&
                        (ai.getTacticalModule().isFleeing() || ai.getTacticalModule().isMaintainingContact() ||
                                ai.getTacticalModule().isStandingDown())) {
                    continue;
                }
            }

            return true;
        }

        return false;
    }
}
