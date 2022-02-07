package data.plugins;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.util.Misc;

public class qolp_PartialSurveyScript implements EveryFrameScript {

    @Override
    public void advance(float amount) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        StarSystemAPI system = fleet.getStarSystem();
        if (system == null) {
            return;
        }
        for (PlanetAPI planet : system.getPlanets()) {
            process(fleet, planet);
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    private boolean canScan(MarketAPI market) {
        if (market == null) {
            return false;
        }
        SurveyLevel level = market.getSurveyLevel();
        return level == SurveyLevel.NONE || level == SurveyLevel.SEEN;
    }

    private boolean isInRange(CampaignFleetAPI fleet, SectorEntityToken token) {
        return Misc.getDistance(fleet, token) < fleet.getSensorStrength();
    }

    private void process(CampaignFleetAPI fleet, PlanetAPI planet) {
        String name = planet.getName();
        if (!isInRange(fleet, planet)) {
            return;
        }
        MarketAPI market = planet.getMarket();
        if (!canScan(market)) {
            return;
        }
        Misc.setPreliminarySurveyed(market, null, true);
    }
}
