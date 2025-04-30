package data.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONException;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static data.utils.qolp_getSettings.getBoolean;
import static data.utils.qolp_getSettings.getFloat;

public class qolp_ammoIndicator extends BaseEveryFrameCombatPlugin {

    SpriteAPI border, background, bar, stria;
    Color positive = Misc.getPositiveHighlightColor(),
            negative = Misc.getNegativeHighlightColor();

    boolean onlyPhase, ignorePD, ignoreSMall;
    boolean drawSelected = false;
    float minCooldown = 3f,
            minReloadTime = 3f,
            offset = 10f;

    @Override
    public void init(CombatEngineAPI engine) {
        float scale = 1;
        float scaleX = 1;
        float scaleY = 1;
        try {
            if (!getBoolean("rfi_enable")) engine.removePlugin(this);
            onlyPhase = getBoolean("rfi_onlyPhase");
            ignorePD = getBoolean("rfi_ignorePD");
            ignoreSMall = getBoolean("rfi_ignoreSMALL");
            drawSelected = getBoolean("rfi_drawonActive");
            minCooldown = getFloat("rfi_minCD");
            minReloadTime = getFloat("rfi_minReload");
            offset = getFloat("rfi_offset");
            scale = getFloat("rfi_scale");
            scaleX = getFloat("rfi_scaleX");
            scaleY = getFloat("rfi_scaleY");
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        border = Global.getSettings().getSprite("ui", "qolp_ammoUIborder");
        border.setColor(positive.darker());
        border.setSize(border.getWidth() * scale * scaleX, border.getHeight() * scale * scaleY);

        background = Global.getSettings().getSprite("ui", "qolp_ammoUIBG");
        background.setHeight(29);
        background.setSize(background.getWidth() * scale * scaleX, background.getHeight() * scale * scaleY);

        bar = Global.getSettings().getSprite("ui", "qolp_ammoUIbar");
        bar.setHeight(29);
        bar.setSize(bar.getWidth() * scale * scaleX, bar.getHeight() * scale * scaleY);

        stria = Global.getSettings().getSprite("ui", "qolp_ammoUIseparator");
        stria.setSize(8 * scale * scaleX, 2 * scale * scaleY);
    }

    List<weaponTracker> weapons = new ArrayList<>();
    ShipAPI playerShip = null;

    public void advance(float amount, List<InputEventAPI> events) {
        boolean newShip = Global.getCombatEngine().getPlayerShip() != playerShip;
        if (Global.getCombatEngine().getPlayerShip() == null) return;
        playerShip = Global.getCombatEngine().getPlayerShip();
        if (!(!onlyPhase || playerShip.getHullSpec().isPhase())) return;
        if (newShip) {
            weapons.clear();
            for (WeaponAPI weapon : playerShip.getAllWeapons()) {
                if ((weapon.getAmmoPerSecond() > 0 && weapon.getMaxAmmo() / weapon.getAmmoPerSecond() >= minReloadTime) || weapon.getCooldown() >= minCooldown) {
                    if (ignorePD && weapon.getSpec().getAIHints().contains(WeaponAPI.AIHints.PD)) continue;
                    if (ignoreSMall && weapon.getSize() == WeaponAPI.WeaponSize.SMALL) continue;
                    weaponTracker temp = new weaponTracker(weapon);
                    temp.distanceBetween = bar.getHeight() / temp.amountOfMags;
                    weapons.add(temp);
                }
            }
        }

        List<WeaponAPI> selectedWeapons = new ArrayList<>();
        if (playerShip.getSelectedGroupAPI() != null)
            selectedWeapons = playerShip.getSelectedGroupAPI().getWeaponsCopy();
        for (weaponTracker weaponTracker : weapons) {
            WeaponAPI weapon = weaponTracker.weapon;
            if (!drawSelected && selectedWeapons.contains(weapon)) {
                weaponTracker.render = false;
                continue;
            } else {
                weaponTracker.render = true;
            }
            if (weapon.isInBurst()) {
                weaponTracker.burst = true;
            } else if (weaponTracker.burst && weapon.getCooldownRemaining() > 0) weaponTracker.burst = false;
            weaponTracker.canShoot = (!(weapon.getCooldown() >= 2) || weapon.getCooldownRemaining() <= 0) && (!weapon.usesAmmo() || weapon.getAmmo() >= 1);
            /*
            weaponTracker.barFraction = weapon.getAmmoPerSecond() > 0 ? (weapon.getAmmo() + weapon.getAmmoTracker().getReloadProgress() * weapon.getAmmoTracker().getReloadSize()) / weapon.getMaxAmmo()
                    : weaponTracker.burst ? 0 : (1 - weapon.getCooldownRemaining() / weapon.getCooldown());
            if (weaponTracker.barFraction > 1) weaponTracker.barFraction = 1;
             */
            weaponTracker.barFraction = weapon.getAmmoPerSecond() > 0 ? (float) weapon.getAmmo() / weapon.getMaxAmmo()
                    : weaponTracker.burst ? 0 : (1 - weapon.getCooldownRemaining() / weapon.getCooldown());
            if (weaponTracker.barFraction > 1) weaponTracker.barFraction = 1;

            if (weapon.getAmmoPerSecond() > 0){
                if (weaponTracker.reloadTime >= 2){
                    weaponTracker.reloadBarFraction = (weapon.getAmmoTracker().getReloadProgress() * weapon.getAmmoTracker().getReloadSize()) / weapon.getMaxAmmo();
                    if (weaponTracker.reloadBarFraction > 1 - weaponTracker.barFraction) weaponTracker.reloadBarFraction = 1 - weaponTracker.barFraction;
                } else {
                    weaponTracker.barFraction = (weapon.getAmmo() + weapon.getAmmoTracker().getReloadProgress() * weapon.getAmmoTracker().getReloadSize()) / weapon.getMaxAmmo();
                    if (weaponTracker.barFraction > 1) weaponTracker.barFraction = 1;
                }
            }
        }
    }

    public void renderInWorldCoords(ViewportAPI viewport) {
        for (weaponTracker weapon : weapons) {
            if (!weapon.render) continue;
            Vector2f barLoc = Vector2f.add(weapon.weapon.getLocation(), new Vector2f(offset, 0), null);
            background.renderAtCenter(barLoc.x, barLoc.y);
            border.renderAtCenter(barLoc.x, barLoc.y);
            bar.setColor(weapon.canShoot ? positive : negative);
            bar.renderRegionAtCenter(barLoc.x, barLoc.y, 0, 0, 1, weapon.barFraction);
            if (weapon.reloadBarFraction > 0){
                bar.setColor(negative);
                bar.renderRegionAtCenter(barLoc.x, barLoc.y, 0, weapon.barFraction, 1, weapon.reloadBarFraction);
            }
            if (weapon.drawStriae){
                for (int i = 1; i <= weapon.striaeAmount; i++){
                    stria.renderAtCenter(barLoc.x, barLoc.y + (bar.getHeight() * -0.5f) + (weapon.distanceBetween * i));
                }
            }
        }
    }

    static class weaponTracker {
        weaponTracker(WeaponAPI weapon) {
            this.weapon = weapon;
            reloadTime = weapon.getAmmoTracker().getReloadSize() / weapon.getAmmoTracker().getAmmoPerSecond();
            amountOfMags = weapon.getMaxAmmo() / weapon.getAmmoTracker().getReloadSize();
            drawStriae = amountOfMags <= 6 && reloadTime >= 1;
            striaeAmount = (int) Math.ceil(amountOfMags) - 1;
        }

        WeaponAPI weapon;
        Boolean canShoot = false;
        float barFraction = 0;
        float reloadBarFraction = 0;
        boolean burst = false;
        boolean render = true;
        float reloadTime;
        float amountOfMags;
        int striaeAmount;
        boolean drawStriae;
        float distanceBetween = 0;
    }
}