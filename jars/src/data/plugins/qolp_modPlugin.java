package data.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import data.utils.qolp_getSettings;
import org.json.JSONException;

import java.io.IOException;

public class qolp_modPlugin extends BaseModPlugin {

    @Override
    public void afterGameSave() {
        try {
            if (qolp_getSettings.getBoolean("ScavengeAsYouFly")) {
                restoreAutoScavenge();
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beforeGameSave() {
        try {
            if (qolp_getSettings.getBoolean("ScavengeAsYouFly")) {
                removeAutoScavenge();
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onApplicationLoad() throws JSONException, IOException {
        if (Global.getSettings().getModManager().isModEnabled("transpoffder")) {
            throw new RuntimeException("QoLP not compatible with Transponder Off pls disable it, all functionality was moved to QoLP");
        }
        if (qolp_getSettings.getBoolean("BetterSensorBurst")) {
            Global.getSettings().getAbilitySpec("sensor_burst").getTags().remove("burn-");
            Global.getSettings().getAbilitySpec("sensor_burst").getTags().remove("sensors+");
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {
        try {
            if (qolp_getSettings.getBoolean("EnableClock")) {
                addTransientScript(new qolp_clock(qolp_getSettings.getBoolean("12HourCLock")));
            }
            if (qolp_getSettings.getBoolean("PartialSurveyAsYouFly")) {
                addTransientScript(new qolp_PartialSurveyScript());
            }
            if (qolp_getSettings.getBoolean("ScavengeAsYouFly")) {
                restoreAutoScavenge();
                notifyAboutState();
            }
            if (qolp_getSettings.getBoolean("Transpoffder")) {
                addTransientListener(new qolp_TranspoffderListener());
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    private void addTransientListener(Object listener) {
        Global.getSector().getListenerManager().addListener(listener, true);
    }

    private void addTransientScript(EveryFrameScript script) {
        Global.getSector().addTransientScript(script);
    }


    private void notifyAboutState() {
        String state = qolp_AutoScavengeAbility.isOn() ? "enabled" : "disabled";
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
        boolean isOn = qolp_AutoScavengeAbility.isOn();
        Global.getSector().getMemoryWithoutUpdate().set("$transpoffderAutoScavenge", isOn);
        Global.getSector().getCharacterData().removeAbility("auto_scavenge");
    }

    private void restoreAutoScavenge() {
        Global.getSector().getCharacterData().addAbility("auto_scavenge");
        boolean isOn = Global.getSector().getMemoryWithoutUpdate().getBoolean("$transpoffderAutoScavenge");
        qolp_AutoScavengeAbility.setOn(isOn);
    }
}
