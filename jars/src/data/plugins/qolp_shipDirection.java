package data.plugins;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.util.List;

import static data.utils.qolp_getSettings.*;
import static org.lwjgl.opengl.GL11.*;

public class qolp_shipDirection extends BaseEveryFrameCombatPlugin {

    SpriteAPI
            arrow,
            arrowBack,
            arrowTarget;
    Color
            allyColor = Misc.getPositiveHighlightColor(),
            enemyColor = Misc.getNegativeHighlightColor();

    private CombatEngineAPI engine;

    private boolean
            customColors = false,
            isON = true,
            enemyIsOn = true,
            reCalcForPlayer = false,
            useShieldShip = true,
            useShieldTarget = true,
            disableOnPause = true,
            drawOnAll = false,
            drawOnForFighters = false;

    private ShipAPI
            target,
            playerShip;

    private float
            collRadShip,
            collRadTarget,
            phaseAngle;

    private float
            shipToggleKey,
            targetToggleKey,
            allToggleKey,
            fightersToggleKey;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        try {
            shipToggleKey = getInt("PlayerShipToggleButton");
            targetToggleKey = getInt("TargetShipToggleButton");
            allToggleKey = getInt("DrawOnAllShipsToggleButton");
            fightersToggleKey = getInt("IncludeFightersInDrawOnAllToggle");
            disableOnPause = getBoolean("DisableMarkerOnPause");
            drawOnAll = getBoolean("DrawOnAll");
            drawOnForFighters = getBoolean("DrawOnAllFighters");
            isON = getBoolean("PayerMarkerOnAtStartOfCombat");
            enemyIsOn = getBoolean("TargetMarkerOnAtStartOfCombat");
            customColors = getBoolean("customColors");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }


        //float sizeMult = 1.5f;
        if (customColors) {
            try {
                allyColor = getColor("allyColor");
                enemyColor = getColor("enemyColor");
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        arrow = Global.getSettings().getSprite("marker", "direction2");
        arrow.setColor(allyColor);
        arrow.setSize(arrow.getWidth(), arrow.getHeight());
        arrow.setNormalBlend();

        arrowTarget = Global.getSettings().getSprite("marker", "direction2");
        arrowTarget.setColor(enemyColor);
        arrowTarget.setSize(arrow.getWidth(), arrow.getHeight());
        arrowTarget.setNormalBlend();

        arrowBack = Global.getSettings().getSprite("marker", "direction2");
        arrowBack.setColor(Color.BLACK);
        arrowBack.setSize(arrowBack.getWidth() * 1.2f, arrowBack.getHeight() * 1.2f);
        arrowBack.setNormalBlend();
    }

    @Override
    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
        if (engine == null) return;

        for (InputEventAPI e : events) {
            if (e.isConsumed()) continue;

            if (e.isKeyDownEvent() && e.getEventValue() == shipToggleKey) {
                isON = !isON;
            }
            if (e.isKeyDownEvent() && e.getEventValue() == targetToggleKey) {
                enemyIsOn = !enemyIsOn;
            }
            if (e.isKeyDownEvent() && e.getEventValue() == allToggleKey) {
                drawOnAll = !drawOnAll;
            }
            if (e.isKeyDownEvent() && e.getEventValue() == fightersToggleKey) {
                drawOnForFighters = !drawOnForFighters;
            }
        }
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {

        if (engine == null) return;
        if (engine.isPaused() && disableOnPause) return;
        if (engine.isUIShowingDialog()) return;
        if (engine.getCombatUI() != null && engine.getCombatUI().isShowingCommandUI()) return;
        if (engine.getPlayerShip() == null) return;
        if (engine.getPlayerShip().getLocation() == null) return;
        if (Global.getCurrentState() != GameState.COMBAT) return;
        if (engine.isCombatOver()) return;

        if (phaseAngle > 360) phaseAngle -= 360;
        phaseAngle += amount * 5f;

        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, Global.getSettings().getScreenWidth(), 0, Global.getSettings().getScreenHeight(), -1, 1);

        //check if player ship same as in last frame
        if (playerShip == null || playerShip != engine.getPlayerShip()) {
            playerShip = engine.getPlayerShip();
            reCalcForPlayer = true;
        }

        float zoom = engine.getViewport().getViewMult();

        Vector2f whereToDraw;
        //player part
        if (isON) {

            if (reCalcForPlayer) {
                float shieldRad = playerShip.getShieldRadiusEvenIfNoShield();
                float collRad = playerShip.getCollisionRadius();

                //chose what to use
                if (shieldRad > collRad * 1.5f || shieldRad < collRad * 0.3f) {
                    collRadShip = collRad;
                    useShieldShip = false;
                } else {
                    collRadShip = shieldRad + 20f;
                    useShieldShip = true;
                }
            }

            Vector2f playerSpeed = new Vector2f();
            playerShip.getVelocity().normalise(playerSpeed);
            //don't render is doesn't move
            if (!VectorUtils.isZeroVector(playerSpeed)) {

                Vector2f playerPos = CombatUtils.toScreenCoordinates((useShieldShip) ? playerShip.getShieldCenterEvenIfNoShield() : playerShip.getLocation());

                //Some calculations
                float angleShip = VectorUtils.getFacing(playerSpeed);
                whereToDraw = new Vector2f(playerPos.x + ((collRadShip * playerSpeed.x) / zoom), playerPos.y + ((collRadShip * playerSpeed.y) / zoom));

                //render body
                arrowBack.setAngle(angleShip);
                arrowBack.renderAtCenter(whereToDraw.x, whereToDraw.y);
                arrow.setAngle(angleShip);
                arrow.renderAtCenter(whereToDraw.x, whereToDraw.y);
            }
        }

        //targetPart
        if (enemyIsOn && !drawOnAll) {

            if (playerShip.getShipTarget() != null) {
                //check if target ship same as in last frame
                if (target == null || target != playerShip.getShipTarget()) {

                    target = engine.getPlayerShip().getShipTarget();

                    float shieldRad = target.getShieldRadiusEvenIfNoShield();
                    float collRad = target.getCollisionRadius();


                    //chose what to use
                    if (shieldRad > collRad * 1.5f || shieldRad < collRad * 0.3f) {
                        collRadTarget = collRad;
                        useShieldTarget = false;
                    } else {
                        collRadTarget = shieldRad + 20f;
                        useShieldTarget = true;
                    }
                }

                //change color
                if (target.getOwner() == playerShip.getOwner()) arrowTarget.setColor(allyColor);
                else arrowTarget.setColor(enemyColor);

                Vector2f targetSpeed = new Vector2f();
                target.getVelocity().normalise(targetSpeed);
                //don't render is doesn't move
                if (!VectorUtils.isZeroVector(targetSpeed)) {

                    Vector2f targetPos = CombatUtils.toScreenCoordinates((useShieldTarget) ? target.getShieldCenterEvenIfNoShield() : target.getLocation());

                    //Some calculations
                    float angleTarget = VectorUtils.getFacing(targetSpeed);
                    whereToDraw = new Vector2f(targetPos.x + ((collRadTarget * targetSpeed.x) / zoom), targetPos.y + ((collRadTarget * targetSpeed.y) / zoom));

                    //render body
                    arrowBack.setAngle(angleTarget);
                    arrowBack.renderAtCenter(whereToDraw.x, whereToDraw.y);
                    arrowTarget.setAngle(angleTarget);
                    arrowTarget.renderAtCenter(whereToDraw.x, whereToDraw.y);
                }
            }
        }

        if (drawOnAll) {
            for (ShipAPI ship : engine.getShips()) {
                if (!engine.isEntityInPlay(ship)) continue;
                if (isON && ship == playerShip) continue;
                if (!screenCheck(0.5f, ship.getLocation())) continue;
                if (!ship.isAlive()) continue;
                if (ship.isHulk()) continue;
                if (ship.getHullSize() == ShipAPI.HullSize.FIGHTER && !drawOnForFighters) continue;
                if (VectorUtils.isZeroVector(ship.getVelocity())) continue;

                float shieldRad = ship.getShieldRadiusEvenIfNoShield();
                float collRad = ship.getCollisionRadius();

                //change color
                if (ship.getOwner() == playerShip.getOwner()) {
                    arrowTarget.setColor(allyColor);
                } else arrowTarget.setColor(enemyColor);

                //chose what to use
                float collRadTarget;
                boolean useShieldTarget;
                if (shieldRad > collRad * 1.5f || shieldRad < collRad * 0.3f) {
                    collRadTarget = collRad;
                    useShieldTarget = false;
                } else {
                    collRadTarget = shieldRad + 20f;
                    useShieldTarget = true;
                }
                Vector2f targetPos = CombatUtils.toScreenCoordinates((useShieldTarget) ? ship.getShieldCenterEvenIfNoShield() : ship.getLocation());

                Vector2f targetSpeed = new Vector2f();
                ship.getVelocity().normalise(targetSpeed);

                //Some calculations
                float angleTarget = VectorUtils.getFacing(targetSpeed);
                whereToDraw = new Vector2f(targetPos.x + ((collRadTarget * targetSpeed.x) / zoom), targetPos.y + ((collRadTarget * targetSpeed.y) / zoom));

                //render body
                arrowBack.setAngle(angleTarget);
                arrowBack.renderAtCenter(whereToDraw.x, whereToDraw.y);
                arrowTarget.setAngle(angleTarget);
                arrowTarget.renderAtCenter(whereToDraw.x, whereToDraw.y);
            }
        }

        glEnd();
        glDisable(GL_BLEND);
        glPopAttrib();
        glColor4f(1, 1, 1, 1);
        glPopMatrix();
    }

    public static boolean screenCheck(float distance, Vector2f point) {
        float space = Global.getCombatEngine().getViewport().getVisibleWidth();
        space = space / 2.0F * (distance + 1.4F);
        return MathUtils.isWithinRange(point, Global.getCombatEngine().getViewport().getCenter(), space);
    }

    /*
    transient private SpriteAPI texture;
    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
        if (playerShip == null) return;

        float bandWidthInTexture = 256;
        float bandIndex;

        float radStart = playerShip.getCollisionRadius();
        float radEnd = radStart + 75f;

        float circ = (float) (Math.PI * 2f * (radStart + radEnd) / 2f);
        //float pixelsPerSegment = 10f;
        float pixelsPerSegment = circ / 720f;
        //float pixelsPerSegment = circ / 720;
        float segments = Math.round(circ / pixelsPerSegment);

//		segments = 360;
//		pixelsPerSegment = circ / segments;
        //pixelsPerSegment = 10f;

        float startRad = (float) Math.toRadians(0);
        float endRad = (float) Math.toRadians(360f);
        float spanRad = Math.abs(endRad - startRad);
        float anglePerSegment = spanRad / segments;

        Vector2f loc = playerShip.getLocation();
        float x = loc.x;
        float y = loc.y;


        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);

        //float zoom = viewport.getViewMult();
        //GL11.glScalef(zoom, zoom, 1);

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        if (texture == null) texture = Global.getSettings().getSprite("abilities", "neutrino_detector");
        texture.bindTexture();

        GL11.glEnable(GL11.GL_BLEND);
        //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        boolean outlineMode = false;
        //outlineMode = true;
        if (outlineMode) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            //GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        }

        float thickness = (radEnd - radStart) * 1f;
        float radius = radStart;

        float texProgress = 0f;
        float texHeight = texture.getTextureHeight();
        float imageHeight = texture.getHeight();
        float texPerSegment = pixelsPerSegment * texHeight / imageHeight * bandWidthInTexture / thickness;

        texPerSegment *= 1f;

        float totalTex = Math.max(1f, Math.round(texPerSegment * segments));
        texPerSegment = totalTex / segments;

        float texWidth = texture.getTextureWidth();
        float imageWidth = texture.getWidth();



        Color color = new Color(25,215,255,255);
        color = allyColor;
        //Color color = new Color(255,25,255,155);

        Vector2f playerSpeed = new Vector2f();
        playerShip.getVelocity().normalise(playerSpeed);
        float shipAngle = VectorUtils.getFacing(playerSpeed);
        float speedFraction = playerShip.getVelocity().length() / playerShip.getMaxSpeedWithoutBoost();
        float lengthMult = 2 + 9 * speedFraction;
        for (int iter = 0; iter < 2; iter++) {
            if (iter == 0) {
                bandIndex = 1;
            } else {
                //color = new Color(255,215,25,255);
                //color = new Color(25,255,215,255);
                bandIndex = 0;
                texProgress = segments/2f * texPerSegment;
                //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            }
            if (iter == 1) {
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            }
            //bandIndex = 1;

            float leftTX = (float) bandIndex * texWidth * bandWidthInTexture / imageWidth;
            float rightTX = (float) (bandIndex + 1f) * texWidth * bandWidthInTexture / imageWidth - 0.001f;

            GL11.glBegin(GL11.GL_QUAD_STRIP);
            for (float i = 0; i < segments + 1; i++) {

                float segIndex = i % (int) segments;

                //float phaseAngleRad = (float) Math.toRadians(phaseAngle + segIndex * 10) + (segIndex * anglePerSegment * 10f);
                float phaseAngleRad;
                if (iter == 0) {
                    phaseAngleRad = (float) phaseAngle + (segIndex * anglePerSegment * 29f);
                } else { //if (iter == 1) {
                    phaseAngleRad = (float) -phaseAngle + (segIndex * anglePerSegment * 17f);
                }


                float angle = (float) Math.toDegrees(segIndex * anglePerSegment);
                //if (iter == 1) angle += 180;


                float pulseSin = (float) Math.sin(phaseAngleRad);
                float pulseMax = thickness * 0.5f;

                pulseMax = thickness * 0.2f;
                pulseMax = 10f;

                //pulseMax *= 0.25f + 0.75f * noiseLevel;

                float pulseAmount = pulseSin * pulseMax;
                //float pulseInner = pulseAmount * 0.1f;
                float pulseInner = pulseAmount * 0.1f;

                float r = radius;

//				float thicknessMult = delegate.getAuroraThicknessMult(angle);
//				float thicknessFlat = delegate.getAuroraThicknessFlat(angle);

                float theta = anglePerSegment * segIndex;;
                float cos = (float) Math.cos(theta);
                float sin = (float) Math.sin(theta);

                float rInner = r;// - pulseInner;
                //if (rInner < r * 0.9f) rInner = r * 0.9f;

                //float rOuter = (r + thickness * thicknessMult - pulseAmount + thicknessFlat);
                float rOuter = r + thickness;// - pulseAmount;


                //rOuter += noiseLevel * 25f;

                float dif = Math.abs(MathUtils.getShortestRotation(shipAngle, angle));
                float grav = Math.max((15 - dif), 0 ) * lengthMult;
                //if (grav > 500) System.out.println(grav);
                //if (grav > 300) grav = 300;
                if (grav > 750) grav = 750;
                grav *= 250f / 750f;
                //grav *= 0.5f;
                //rInner -= grav * 0.25f;

                //rInner -= grav * 0.1f;
                rOuter += grav;
//				rInner -= grav * 3f;
//				rOuter -= grav * 3f;
                //System.out.println(grav);

                float alpha = 1f;
                alpha *= Math.min(grav / 50, 1f);
                //alpha *= 0.75f;

//
//
//
//				phaseAngleWarp = (float) Math.toRadians(phaseAngle - 180 * iter) + (segIndex * anglePerSegment * 1f);
//				float warpSin = (float) Math.sin(phaseAngleWarp);
//				rInner += thickness * 0.5f * warpSin;
//				rOuter += thickness * 0.5f * warpSin;



                float x1 = cos * rInner;
                float y1 = sin * rInner;
                float x2 = cos * rOuter;
                float y2 = sin * rOuter;

                x2 += (float) (Math.cos(phaseAngleRad) * pixelsPerSegment * 0.33f);
                y2 += (float) (Math.sin(phaseAngleRad) * pixelsPerSegment * 0.33f);


                GL11.glColor4ub((byte)color.getRed(),
                        (byte)color.getGreen(),
                        (byte)color.getBlue(),
                        (byte)((float) color.getAlpha() * alpha));

                GL11.glTexCoord2f(leftTX, texProgress);
                GL11.glVertex2f(x1, y1);
                GL11.glTexCoord2f(rightTX, texProgress);
                GL11.glVertex2f(x2, y2);

                texProgress += texPerSegment * 1f;
            }
            GL11.glEnd();

            //GL11.glRotatef(180, 0, 0, 1);
        }
        GL11.glPopMatrix();

        if (outlineMode) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        }
    }

     */
}
