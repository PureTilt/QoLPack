package data.plugins;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.util.DoNotObfuscate;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Calendar;
import java.util.Date;

public class qolp_clock implements EveryFrameScript {

    qolp_clock (boolean USTime){
        this.USTime = USTime;
    }

    private static LazyFont.DrawableString TODRAW14;
    private static final float UIScaling = Math.min(1, Global.getSettings().getScreenScaleMult());
    float
            displayHeight = Display.getHeight(),
            triPadHeight,
            height,
            alpha = 1;

    boolean USTime = false;
    

    static {
        try {
            LazyFont fontdraw = LazyFont.loadFont("graphics/fonts/orbitron20aa.fnt");
            TODRAW14 = fontdraw.createText();
            //TODRAW14.setFontSize(20f);
            if (UIScaling > 1f) { //mf
                TODRAW14.setFontSize(20f * UIScaling);
            }

        } catch (FontException ignored) {
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }


    @Override
    public void advance(float amount) {
        //It's to prevent double rendering due to fast forwarding
        if (Global.getSector().isFastForwardIteration()) return;

        boolean render = Global.getSector().getCampaignUI().getCurrentCoreTab() == CoreUITabId.FLEET ||
                (Global.getSector().getCampaignUI().getCurrentCoreTab() == null &&
                        !Global.getSector().getCampaignUI().isShowingDialog() &&
                        !Global.getSector().getCampaignUI().isShowingMenu());
        if (!render) {
            alpha = 0f;
            return;
        }
        SpriteAPI triPadArrow = Global.getSettings().getSprite("ui", "tripad_topleft");
        triPadArrow.setHeight(triPadArrow.getHeight() * -1f);
        triPadArrow.setSize(triPadArrow.getWidth() * UIScaling, triPadArrow.getHeight() * UIScaling);
        triPadHeight = triPadArrow.getHeight();

        Color main = Misc.getTooltipTitleAndLightHighlightColor();
        Color black = new Color(0,0,0, Math.round(255));

        if (alpha < 1){
            alpha += amount * 3;
            if (alpha > 1){
                alpha = 1;
            }
        }
        triPadArrow.setAlphaMult(alpha);


        Date currtime = new Date();
        Calendar data = Calendar.getInstance();
        int hours = data.get(Calendar.HOUR_OF_DAY);
        int minutes = data.get(Calendar.MINUTE);
        String inB = ":";
        if (minutes < 10) inB += "0";
        String text = "";
        if (USTime){
            if (hours >= 12){
                hours -= 12;
                text = hours + inB + minutes + " PM";
            } else {
                text = hours + inB + minutes + " AM";
            }
        } else {
            text = hours + inB + minutes;
        }

        //text = "12:06";


        openGL11ForText();

        height = Math.round(displayHeight - (-triPadHeight * 2 - 15) * UIScaling);

        TODRAW14.setText(text);
        triPadArrow.render(0, height);
        float width = TODRAW14.getWidth();
        int TextPosX = Math.round(((USTime ? 53 : 35) * UIScaling) - width * 0.5f);
        int TextPosY =  Math.round(height + TODRAW14.getHeight() * 0.5f + triPadHeight  * 0.5f + 4 * UIScaling);
        TODRAW14.setColor(black);
        TODRAW14.draw(TextPosX + 2, TextPosY - 1);
        TODRAW14.setColor(main);
        TODRAW14.draw(TextPosX, TextPosY);
        //Global.getLogger(qolp_clock.class).info(main.getAlpha());

        closeGL11ForText();
    }

    /**
     * GL11 to start, when you want render text of Lazyfont.
     */
    private static void openGL11ForText() {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
        GL11.glOrtho(0.0, Display.getWidth(), 0.0, Display.getHeight(), -1.0, 1.0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * GL11 to close, when you want render text of Lazyfont.
     */
    private static void closeGL11ForText() {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private Color changeAlpha (Color color, float alpha){
        return new Color(color.getRed(),color.getGreen(),color.getBlue(), Math.min(255, Math.max(0, Math.round(alpha))));
    }
}
