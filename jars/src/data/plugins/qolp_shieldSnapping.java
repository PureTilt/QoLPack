package data.plugins;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.qolp_getSettings;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

import static org.lazywizard.lazylib.opengl.ColorUtils.glColor;
import static org.lwjgl.opengl.GL11.*;

public class qolp_shieldSnapping extends BaseEveryFrameCombatPlugin {

    int snapIfTarget = 59;
    int rememberTarget = 60;
    int shipSide = 61;
    int direction = 62;
    int menuKey = 59;

    float skippedFrames = 0;

    boolean shieldSelectorActive = false;
    boolean renderText = true;
    boolean legacyMode = false;
    boolean resetMouse = false;

    int menuTheme = 1;


    CombatEngineAPI engine;
    private static shieldMods mode;

    LazyFont fontdraw;

    LazyFont.DrawableString q1Text;
    LazyFont.DrawableString q2Text;
    LazyFont.DrawableString q3Text;
    LazyFont.DrawableString q4Text;

    Vector2f q1TextCords = new Vector2f(0, 0);
    Vector2f q2TextCords = new Vector2f(0, 0);
    Vector2f q3TextCords = new Vector2f(0, 0);
    Vector2f q4TextCords = new Vector2f(0, 0);

    int segments = 14 * 4;
    float outerR = 75f;
    float innerR = 25f;
    float ringR = outerR - innerR;
    float speedMulti = 0.05f;

    @Override
    public void init(CombatEngineAPI engine) {

        {
            try {
                fontdraw = LazyFont.loadFont("graphics/fonts/orbitron20aa.fnt");
            } catch (FontException e) {
                throw new RuntimeException(e);
            }
        }


        snapIfTarget = qolp_getSettings.getInt("snapIfTarget");
        rememberTarget = qolp_getSettings.getInt("rememberTarget");
        shipSide = qolp_getSettings.getInt("shipSide");
        direction = qolp_getSettings.getInt("direction");

        menuTheme = qolp_getSettings.getInt("ShieldSnappingMenuTheme");
        innerR = qolp_getSettings.getInt("ShieldSnappingMenuInnerR");
        outerR = qolp_getSettings.getInt("ShieldSnappingMenuRingThinknes") + innerR;
        renderText = qolp_getSettings.getBoolean("ShieldSnappingMenuTextRender");
        legacyMode = qolp_getSettings.getBoolean("EnableLegacyShieldSnapping");
        resetMouse = qolp_getSettings.getBoolean("ShieldSnappingResetMouse");
        speedMulti = (100 - qolp_getSettings.getInt("ShieldSnappingSlowDown")) * 0.01f;

        q1Text = fontdraw.createText();
        q2Text = fontdraw.createText();
        q3Text = fontdraw.createText();
        q4Text = fontdraw.createText();


//        q1Text.setRenderDebugBounds(true);
//        q2Text.setRenderDebugBounds(true);
//        q3Text.setRenderDebugBounds(true);
//        q4Text.setRenderDebugBounds(true);
//TextAlignment.CENTER and TextAlignment.RIGHT adds empty space to right of text causing positioning issues will have to do with default for now
        float textRadius = (innerR + 10f);
        double q1Angle = Math.PI * 0.75f;
        q1TextCords = new Vector2f((float) (Math.cos(q1Angle) * textRadius), (float) (Math.sin(q1Angle) * textRadius));
        q1Text.setText("Snap If\nTarget");
        q1Text.setAnchor(LazyFont.TextAnchor.BOTTOM_RIGHT);
        //q1Text.setAlignment(LazyFont.TextAlignment.CENTER);

        double q2Angle = Math.PI * 0.25f;
        q2TextCords = new Vector2f((float) (Math.cos(q2Angle) * textRadius), (float) (Math.sin(q2Angle) * textRadius));
        q2Text.setText("Remember\nTarget");
        q2Text.setAnchor(LazyFont.TextAnchor.BOTTOM_LEFT);
        //q2Text.setAlignment(LazyFont.TextAlignment.RIGHT);

        double q3Angle = Math.PI * -0.25f;
        q3TextCords = new Vector2f((float) (Math.cos(q3Angle) * textRadius), (float) (Math.sin(q3Angle) * textRadius));
        q3Text.setText("Ship\nSide");
        q3Text.setAnchor(LazyFont.TextAnchor.TOP_LEFT);
        //q3Text.setAlignment(LazyFont.TextAlignment.CENTER);

        double q4Angle = Math.PI * -0.75f;
        q4TextCords = new Vector2f((float) (Math.cos(q4Angle) * textRadius), (float) (Math.sin(q4Angle) * textRadius));
        q4Text.setText("In World\nDirection");
        q4Text.setAnchor(LazyFont.TextAnchor.TOP_RIGHT);
        //q4Text.setAlignment(LazyFont.TextAlignment.CENTER);

        this.engine = engine;

        int defaultMode = qolp_getSettings.getInt("DefaultShieldSnapMode");
        boolean saveMode = qolp_getSettings.getBoolean("SaveModeBetweenEncounters");

        if (!saveMode || mode == null) {
            switch (defaultMode) {
                case 0:
                    mode = shieldMods.none;
                    break;
                case 1:
                    mode = shieldMods.snapIfTarget;
                    break;
                case 2:
                    mode = shieldMods.rememberTarget;
                    break;
                case 3:
                    mode = shieldMods.shipSide;
                    break;
                case 4:
                    mode = shieldMods.direction;
                    break;
            }
        }
    }

    @Override
    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
        if (engine == null || engine.getPlayerShip() == null) return;
        if (Global.getCurrentState() == GameState.TITLE) return;
        for (InputEventAPI e : events) {
            if (e.isConsumed()) continue;
            if (legacyMode) {
                if (e.isKeyDownEvent()) {
                    if (e.getEventValue() == snapIfTarget) {
                        if (mode.equals(shieldMods.snapIfTarget)) mode = shieldMods.none;
                        else mode = shieldMods.snapIfTarget;
                    } else if (e.getEventValue() == rememberTarget) {
                        if (mode.equals(shieldMods.rememberTarget)) mode = shieldMods.none;
                        else mode = shieldMods.rememberTarget;
                    } else if (e.getEventValue() == shipSide) {
                        if (mode.equals(shieldMods.shipSide)) mode = shieldMods.none;
                        else mode = shieldMods.shipSide;
                    } else if (e.getEventValue() == direction) {
                        if (mode.equals(shieldMods.direction)) mode = shieldMods.none;
                        else mode = shieldMods.direction;
                    }

                }
            } else {
                if (e.getEventValue() == menuKey) {
                    if (e.isKeyDownEvent()) {
                        shieldSelectorActive = true;
                        mousePos = new Vector2f(Global.getSettings().getMouseX(), Global.getSettings().getMouseY());

                    } else if (e.isKeyUpEvent()) {
                        shieldSelectorActive = false;
                        Vector2f newMousePos = new Vector2f(Global.getSettings().getMouseX(), Global.getSettings().getMouseY());
                        if (MathUtils.isWithinRange(mousePos, newMousePos, innerR)) {
                            mode = shieldMods.none;
                        } else {
                            if (newMousePos.x > mousePos.x) {
                                if (newMousePos.y > mousePos.y) {
                                    mode = shieldMods.rememberTarget;
                                } else {
                                    mode = shieldMods.shipSide;
                                }
                            } else {
                                if (newMousePos.y > mousePos.y) {
                                    mode = shieldMods.snapIfTarget;
                                } else {
                                    mode = shieldMods.direction;
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    Vector2f mousePos;

    @Override
    public void renderInUICoords(ViewportAPI viewport) {
        //Vector2f pos = new Vector2f(viewport.convertScreenXToWorldX(mousePos.x),viewport.convertScreenYToWorldY(mousePos.y));


        if (shieldSelectorActive) {
            float snort = innerR;

            Vector2f pos = mousePos;
            Vector2f newMousePos = new Vector2f(Global.getSettings().getMouseX(), Global.getSettings().getMouseY());
            Color ringColor = Misc.getDarkPlayerColor();
            Color borderCOlor = Misc.getBrightPlayerColor();
            Color highlightColor = Misc.getPositiveHighlightColor();

            switch (menuTheme) {
                case 2:
                    ringColor = Global.getCombatEngine().getPlayerShip().getShield().getInnerColor();
                    borderCOlor = Global.getCombatEngine().getPlayerShip().getShield().getRingColor();
                    break;
                case 3:
                    ringColor = Misc.getPositiveHighlightColor();
                    borderCOlor = Misc.getPositiveHighlightColor().darker();
                    highlightColor = Misc.getPositiveHighlightColor().brighter();
                    break;

            }


            glPushMatrix();
            glPushAttrib(GL_ALL_ATTRIB_BITS);
            glPushAttrib(GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT);
            glDisable(GL_TEXTURE_2D);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glBegin(GL_TRIANGLE_STRIP);
            for (int i = 0; i <= segments; i++) {
                double angle = 2 * Math.PI * i / segments;
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                // Inner vertex
                glColor(ringColor, 1);
                glVertex2f(pos.x + cos * innerR, pos.y + sin * innerR);

                // Outer vertex
                glColor(ringColor, 0f);
                glVertex2f(pos.x + cos * outerR, pos.y + sin * outerR);

            }
            glEnd();


            //highlight
            if (!MathUtils.isWithinRange(mousePos, newMousePos, innerR)) {
                //quadrant detection
                int type;
                if (newMousePos.x > mousePos.x) {
                    if (newMousePos.y > mousePos.y) {
                        type = 2;
                    } else {
                        type = 3;
                    }
                } else {
                    if (newMousePos.y > mousePos.y) {
                        type = 1;
                    } else {
                        type = 4;
                    }
                }
                //rendering highlight
                glBegin(GL_TRIANGLE_STRIP);
                for (int i = (segments / 4) * (type - 1); i <= (segments / 4) * type; i++) {
                    double angle = 2 * Math.PI * i / segments * -1 + Math.PI;
                    float cos = (float) Math.cos(angle);
                    float sin = (float) Math.sin(angle);

                    // Inner vertex
                    //glColor4f(0f, 1f, 0f, 0.5f);
                    glColor(highlightColor, 0.5f);
                    glVertex2f(pos.x + cos * innerR, pos.y + sin * innerR);


                    // Outer vertex
                    //glColor4f(0f, 1f, 0f, 0f);
                    glColor(highlightColor, 0f);
                    glVertex2f(pos.x + cos * outerR, pos.y + sin * outerR);


                }
                glEnd();
            } else {
                //central circle
                glColor(Misc.getNegativeHighlightColor());
                glBegin(GL_TRIANGLE_FAN);

                glVertex2f(pos.x, pos.y);

                for (int i = 0; i <= segments; i++) {
                    double angle = 2 * Math.PI * i / segments;
                    float x = (float) (pos.x + Math.cos(angle) * snort);
                    float y = (float) (pos.y + Math.sin(angle) * snort);

                    glVertex2f(x, y);
                }
                glEnd();

            }
            //outer border
            glEnable(GL_LINE_SMOOTH);
            glLineWidth(6f);
            glColor(Color.BLACK, 0.5f);

            // Inner border
            glBegin(GL_LINE_LOOP);
            for (int i = 0; i < segments; i++) {
                double angle = 2 * Math.PI * i / segments;
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                glVertex2f(pos.x + cos * snort, pos.y + sin * snort);
            }
            glEnd();
            //dividers
            glBegin(GL_LINES);

            for (int i = 0; i < 4; i++) {
                double angle = i * Math.PI / 2.0;
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                // from inner radius to outer radius
                glColor(Color.BLACK, 0.5f);
                glVertex2f(pos.x + cos * snort, pos.y + sin * snort);

                glColor(Color.BLACK, 0f);
                glVertex2f(pos.x + cos * outerR, pos.y + sin * outerR);
            }
            glEnd();

            //outer border
            glEnable(GL_LINE_SMOOTH);
            glLineWidth(2f);
            glColor(borderCOlor);
/*
            glColor(Misc.getPositiveHighlightColor());
            glBegin(GL_LINE_LOOP);
            for (int i = 0; i < segments; i++) {
                double angle = 2 * Math.PI * i / segments;
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                glVertex2f(pos.x + cos * outerR, pos.y + sin * outerR);
            }
            glEnd();*/

            // Inner border
            glBegin(GL_LINE_LOOP);
            for (int i = 0; i < segments; i++) {
                double angle = 2 * Math.PI * i / segments;
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                glVertex2f(pos.x + cos * snort, pos.y + sin * snort);
            }
            glEnd();
            //dividers
            glBegin(GL_LINES);

            for (int i = 0; i < 4; i++) {
                double angle = i * Math.PI / 2.0;
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                // from inner radius to outer radius
                glColor(borderCOlor);
                glVertex2f(pos.x + cos * snort, pos.y + sin * snort);

                glColor(borderCOlor, 0);
                glVertex2f(pos.x + cos * outerR, pos.y + sin * outerR);
            }
            glEnd();

            glPopAttrib();
            glPopMatrix();

            if (renderText) {
                q1Text.draw(Vector2f.add(q1TextCords, pos, null));
                q2Text.draw(Vector2f.add(q2TextCords, pos, null));
                q3Text.draw(Vector2f.add(q3TextCords, pos, null));
                q4Text.draw(Vector2f.add(q4TextCords, pos, null));
            }
        }
    }

    ShipAPI mode2Target;
    Float mode3Angle;
    Vector2f mode4Direction;

    boolean doOnce = false;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        //if (Global.getCurrentState() == GameState.TITLE) return;
        if (skippedFrames < 0.5f) {
            skippedFrames += amount;
            return;
        }
        if (engine == null || engine.getPlayerShip() == null) return;
        ShipAPI player = engine.getPlayerShip();
        if (shieldSelectorActive) {
            engine.getTimeMult().modifyMult("qolp_shieldSnapping", speedMulti);
            //engine.getViewport().setExternalControl(true);
            doOnce = true;
        } else {
            engine.getTimeMult().unmodify("qolp_shieldSnapping");
            if (resetMouse && doOnce){
                //engine.getViewport().setExternalControl(false);
                Mouse.setCursorPosition((int)mousePos.x,(int)mousePos.y);
                doOnce = false;
            }

        }
        if (player.getShield() == null || player.getShield().getType() != ShieldAPI.ShieldType.OMNI) return;
        if (player.getAI() != null) return;
        if (!mode.equals(shieldMods.rememberTarget)) mode2Target = null;
        if (!mode.equals(shieldMods.shipSide)) mode3Angle = null;
        if (!mode.equals(shieldMods.direction)) mode4Direction = null;
//        engine.getViewport().setExternalControl(true);
//        engine.getViewport().setCenter(player.getLocation());
        if (mode.equals(shieldMods.none)) return;
        if (mode.equals(shieldMods.snapIfTarget)) {
            engine.maintainStatusForPlayerShip("qolp_shieldSnapping", "graphics/icons/hullsys/qolp_shieldSnap.png", "Snapping mode", "1 snap if target", false);
            if (player.getShipTarget() != null) {
                player.setShieldTargetOverride(player.getShipTarget().getLocation().x, player.getShipTarget().getLocation().y);
            }
        } else if (mode.equals(shieldMods.rememberTarget)) {
            engine.maintainStatusForPlayerShip("qolp_shieldSnapping", "graphics/icons/hullsys/qolp_shieldSnap.png", "Snapping mode", "2 remember target", false);
            if (mode2Target != null && mode2Target.isHulk()) mode2Target = null;
            if (player.getShipTarget() != null && mode2Target == null) {
                mode2Target = player.getShipTarget();
            }
            if (mode2Target != null) {
                player.setShieldTargetOverride(mode2Target.getLocation().x, mode2Target.getLocation().y);
            } else {
                engine.maintainStatusForPlayerShip("qolp_shieldSnapping", "graphics/icons/hullsys/qolp_shieldSnap.png", "Snapping mode", "2 remember No target", true);
            }

        } else if (mode.equals(shieldMods.shipSide)) {
            engine.maintainStatusForPlayerShip("qolp_shieldSnapping", "graphics/icons/hullsys/qolp_shieldSnap.png", "Snapping mode", "3 ship side", false);
            if (mode3Angle == null) {
                mode3Angle = player.getShield().getFacing() - player.getFacing();
            } else {
                Vector2f side = Misc.getUnitVectorAtDegreeAngle(mode3Angle + player.getFacing());
                side.scale(1000);
                Vector2f target = new Vector2f(side.x + player.getShield().getLocation().x, side.y + player.getShield().getLocation().y);
                player.setShieldTargetOverride(target.x, target.y);
            }
            //engine.maintainStatusForPlayerShip("qolp_shieldSnappingInfo", "graphics/icons/hullsys/qolp_shieldSnap.png", "mode 3 info", player.getFacing() + "/" + mode3Angle + "/" + player.getShield().getFacing(), false);
        } else if (mode.equals(shieldMods.direction)) {
            engine.maintainStatusForPlayerShip("qolp_shieldSnapping", "graphics/icons/hullsys/qolp_shieldSnap.png", "Snapping mode", "4 direction", false);
            if (mode4Direction == null) {
                mode4Direction = Misc.getUnitVectorAtDegreeAngle(player.getShield().getFacing());
                mode4Direction.scale(1000);
            } else {
                Vector2f target = new Vector2f(mode4Direction.x + player.getShield().getLocation().x, mode4Direction.y + player.getShield().getLocation().y);
                player.setShieldTargetOverride(target.x, target.y);
            }
        }
    }

    protected enum shieldMods {
        none,
        snapIfTarget,
        rememberTarget,
        shipSide,
        direction
    }
}
