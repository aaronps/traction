package dev.aaronps.traction.screens;

import android.graphics.Canvas;
import dev.aaronps.gameengine.Screen;
import dev.aaronps.traction.Config;
import dev.aaronps.traction.InputManager;
import dev.aaronps.traction.Res;
import dev.aaronps.traction.gamelayers.BackgroundStarsParticleSystem;

/**
 *
 * @author krom
 */
public class ConfigScreen implements Screen
{
    public Screen resume_to = null;
    
    public void enter()
    {
        BackgroundStarsParticleSystem.init();
//        BackgroundStarsParticleSystem.slowmo = true;
    }
    
    public final Screen logic(final float time)
    {
//        BackgroundStarsParticleSystem.slowmo = true;
        if ( InputManager.wasPressed() )
        {
            InputManager.resetPress();
            final float x = InputManager.pointer_x / Config.screen_x_ratio;
            final float y = InputManager.pointer_y / Config.screen_y_ratio;

            if ( Res.button_back.dst.contains(x, y) )
            {
                return resume_to;
            }
            else if ( Res.button_direct.dst.contains(x, y) )
            {
                InputManager.setNormalMode();
            }
            else if ( Res.button_joystick.dst.contains(x, y) )
            {
                InputManager.setJoystickMode();
            }
        }

        return this;
    }

    public final void interpol(final float time)
    {
        resume_to.interpol(time);
    }
    
    public final void drawGame(final Canvas c)
    {
        resume_to.drawGame(c);
    }
    
    public final void drawUI(final Canvas c)
    {
        Res.button_back.draw(c);
        if ( InputManager.working_mode == 0 )
        {
            Res.button_direct_on.draw(c);
            Res.button_joystick.draw(c);
        }
        else
        {
            Res.button_direct.draw(c);
            Res.button_joystick_on.draw(c);
        }
        Res.fps_number.draw(c);
    }
}
