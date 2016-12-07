package dev.aaronps.traction.screens;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import dev.aaronps.gameengine.Screen;
import dev.aaronps.traction.Config;
import dev.aaronps.traction.Debril;
import dev.aaronps.traction.GAME;
import dev.aaronps.traction.InputManager;
import dev.aaronps.traction.Res;
import dev.aaronps.traction.gamelayers.BackgroundStarsParticleSystem;
import dev.aaronps.traction.gamelayers.BitmapExplosionParticleSystem;
import dev.aaronps.traction.gamelayers.SparkParticleSystem;
import dev.aaronps.traction.ui.UIScore;

/**
 *
 * @author krom
 */
public class GameDeadScreen implements Screen
{
    UIScore score1 = new UIScore();
    UIScore score2 = new UIScore();
    UIScore score3 = new UIScore();
    UIScore score4 = new UIScore();
    UIScore score5 = new UIScore();
    UIScore score6 = new UIScore();
    UIScore score7 = new UIScore();
    UIScore score8 = new UIScore();
    UIScore score9 = new UIScore();
    UIScore score0 = new UIScore();
    
    static class DrawRect
    {
        Rect src;
        Rect dst;
        
        public DrawRect(int srcx, int w, int x, int y)
        {
            src = new Rect(srcx, 0, srcx+w, 26);
            dst = new Rect(x, y, x+w, y+26);
        }
        
        public void draw(Canvas c)
        {
            c.drawBitmap(Res.score_order, src, dst, null);
        }
    }
    
    DrawRect[] nums = new DrawRect[]
    {
        new DrawRect(  0, 24, 84, 247),
        new DrawRect( 24, 24, 84, 247+32*1),
        new DrawRect( 48, 24, 84, 247+32*2),
        new DrawRect( 72, 24, 84, 247+32*3),
        new DrawRect( 96, 24, 84, 247+32*4),
        new DrawRect(120, 24, 84, 247+32*5),
        new DrawRect(144, 24, 84, 247+32*6),
        new DrawRect(168, 24, 84, 247+32*7),
        new DrawRect(192, 24, 84, 247+32*8),
        new DrawRect(216, 32, 76, 247+32*9),
    };
    
    
    
    public GameDeadScreen()
    {
        score1.set("2014-01-021.234");
        score2.set("2014-01-0212.234");
        score3.set("2014-01-02391.234");
        score4.set("2014-01-020.234");
        score5.set("2014-01-02321.234");
        score6.set("2014-01-02321.234");
        score7.set("2014-01-02321.234");
        score8.set("2014-01-02321.234");
        score9.set("2014-01-02321.234");
        score0.set("2014-01-02321.234");
        
        final int x = 112; //108
        final int b = 248;
        final int h = 32;
        final int w = 284; // 288
        
        score1.setPosition(x, b, w);
        score2.setPosition(x, b+h*1, w);
        score3.setPosition(x, b+h*2, w);
        score4.setPosition(x, b+h*3, w);
        score5.setPosition(x, b+h*4, w);
        score6.setPosition(x, b+h*5, w);
        score7.setPosition(x, b+h*6, w);
        score8.setPosition(x, b+h*7, w);
        score9.setPosition(x, b+h*8, w);
        score0.setPosition(x, b+h*9, w);
    }
    
    public void enter()
    {
        BackgroundStarsParticleSystem.slowmo = true;
    }
    
    public final Screen logic(final float time)
    {
        if ( InputManager.wasPressed() )
        {
            InputManager.resetPress();
            final float x = InputManager.pointer_x / Config.screen_x_ratio;
            final float y = InputManager.pointer_y / Config.screen_y_ratio;
            if ( Res.button_pause.dst.contains(x, y) )
            {
                Screens.GAME_PAUSE.resume_to = this;
                Screens.GAME_PAUSE.enter();
                return Screens.GAME_PAUSE;
            }
            
            Screens.GAME_BEFORE.enter();
            return Screens.GAME_BEFORE;
        }
        
        GAME.beginStep();
        GAME.debrilMoveLogic(time);

        return this;
    }

    public final void interpol(final float time)
    {
        BackgroundStarsParticleSystem.logic(time);
        SparkParticleSystem.logic(time);
        BitmapExplosionParticleSystem.logic(time);
    }
    
    public final void drawGame(final Canvas c)
    {
        BackgroundStarsParticleSystem.draw(c);
        
        final Bitmap b = Res.debril;
        final Debril[] debrils = GAME.new_state.debrils;
        for ( int t = GAME.new_state.count; t > 0; )
        {
            Debril d = debrils[--t];
            c.drawBitmap(b, d.x - Res.debril_offset_x,
                            d.y - Res.debril_offset_y, null);
        }
        
        SparkParticleSystem.draw(c); // 2014-05-05 18:40
        BitmapExplosionParticleSystem.draw(c);
        
    }
    
    public final void drawUI(final Canvas c)
    {
        Res.button_pause.draw(c);
        Res.fps_number.draw(c);
//        Res.death_message.draw(c);
        Res.scores_bg.draw(c);
        
        score1.draw(c);
        score2.draw(c);
        score3.draw(c);
        score4.draw(c);
        score5.draw(c);
        score6.draw(c);
        score7.draw(c);
        score8.draw(c);
        score9.draw(c);
        score0.draw(c);
        
        for ( int n = 0; n < 10; n++) nums[n].draw(c);
                
        Res.alive_time_number.draw(c);
    }
}
