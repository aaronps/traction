package dev.aaronps.traction.screens;

import android.graphics.Canvas;
import dev.aaronps.gameengine.Screen;
import dev.aaronps.traction.Config;
import dev.aaronps.traction.InputManager;
import dev.aaronps.traction.Res;

/**
 *
 * @author krom
 */
public class GamePauseScreen implements Screen
{
    public Screen resume_to = null;

    public void enter()
    {
//        BackgroundStarsParticleSystem.slowmo = true;
    }
    
    public final Screen logic(final float time)
    {
        if ( InputManager.wasPressed() )
        {
            InputManager.resetPress();
            final float x = InputManager.pointer_x / Config.screen_x_ratio;
            final float y = InputManager.pointer_y / Config.screen_y_ratio;
            
            if ( Res.button_continue.dst.contains(x, y) )
            {
                return resume_to;
            }
            else if ( Res.button_config.dst.contains(x, y) )
            {
                Screens.CONFIG.resume_to = this;
                return Screens.CONFIG;
            }
            else if ( Res.button_menu.dst.contains(x, y) )
            {
                Screens.MENU_MAIN.enter();
                return Screens.MENU_MAIN;
            }
            
        }
        
        return this;
    }
    
    public final void interpol(final float time)
    {
        
    }
    
    public final void drawGame(final Canvas c)
    {
        resume_to.drawGame(c);
    }
    
    public final void drawUI(final Canvas c)
    {
        Res.fps_number.draw(c);
        Res.alive_time_number.draw(c);
        
        Res.button_continue.draw(c);
        Res.button_config.draw(c);
        Res.button_menu.draw(c);
        
    }
    
}
