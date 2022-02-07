package data.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class qolp_modPlugin extends BaseModPlugin {

    public static final String ID = "qolp_modPlugin";
    public static final String SETTINGS_PATH = "QoLPack.ini";

    private JSONObject settings;

    @Override
    public void onApplicationLoad() throws Exception {
        settings = Global.getSettings().getMergedJSONForMod(SETTINGS_PATH, ID);
        if (hasQol("BetterSensorBurst")) {
            Global.getSettings().getAbilitySpec("sensor_burst").getTags().remove("burn-");
            Global.getSettings().getAbilitySpec("sensor_burst").getTags().remove("sensors+");
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if (hasQol("EnableClock")) {
            addTransientScript(new qolp_clock());
        }
        if (hasQol("PartialSurveyAsYouFly")) {
            addTransientScript(new qolp_PartialSurveyScript());
        }
        if (hasQol("ScavengeAsYouFly")) {
            restoreAutoScavenge();
            notifyAboutState();
        }
        if (hasQol("Transpoffder")) {
            addTransientListener(new qolp_TranspoffderListener());
        }
    }

    private void addTransientListener(Object listener) {
        Global.getSector().getListenerManager().addListener(listener, true);
    }

    private void addTransientScript(EveryFrameScript script) {
        Global.getSector().addTransientScript(script);
    }

    private boolean hasQol(String key) {
        return settings.optBoolean(key, true);
    }

    private void notifyAboutState() {
        String state = AutoScavengeAbility.isOn() ? "enabled" : "disabled";
        Global
            .getSector()
            .getCampaignUI()
            .addMessage(
                "Automatic scavenging is %s.",
                Misc.getTextColor(),
                state,
                state,
                Misc.getHighlightColor(),
                Misc.getHighlightColor()
            );
    }

    private void removeAutoScavenge() {
        boolean isOn = AutoScavengeAbility.isOn();
        Global.getSector().getMemoryWithoutUpdate().set("$transpoffderAutoScavenge", isOn);
        Global.getSector().getCharacterData().removeAbility("auto_scavenge");
    }

    private void restoreAutoScavenge() {
        Global.getSector().getCharacterData().addAbility("auto_scavenge");
        boolean isOn = Global.getSector().getMemoryWithoutUpdate().getBoolean("$transpoffderAutoScavenge");
        AutoScavengeAbility.setOn(isOn);
    }
}
