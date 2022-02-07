package data.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.listeners.CurrentLocationChangedListener;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;

public class qolp_TranspoffderListener implements CurrentLocationChangedListener {

    @Override
    public void reportCurrentLocationChanged(LocationAPI prev, LocationAPI curr) {
        if (prev == null || curr == null) {
            return;
        }
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return;
        }
        if (curr.isHyperspace()) {
            playerFleet.setTransponderOn(false);
            playerFleet.getAbility(Abilities.TRANSPONDER).deactivate();
        }
    }
}
