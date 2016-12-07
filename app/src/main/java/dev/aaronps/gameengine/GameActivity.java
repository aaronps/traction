package dev.aaronps.gameengine;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import dev.aaronps.traction.Config;
import dev.aaronps.traction.Res;
import dev.aaronps.traction.SoundManager;
import java.io.IOException;

/**
 *
 * @author krom
 */
public abstract class GameActivity
                extends Activity
                implements Runnable, SurfaceHolder.Callback
{
    private static final String PREFS_NAME = "GamePrefs";
    
    public abstract Screen getInitialScreen();
    public abstract SurfaceView getView(final Context context);
    public abstract void loadSettings(final SharedPreferences prefs);
    public abstract void saveSettings(final SharedPreferences prefs);

    private boolean running = false;
    private Thread thread = null;
    
    private Screen current_screen = null;
    private GameSurfaceView view = null;
    
    private final Matrix viewMatrix = new Matrix();
    private final Matrix uiMatrix = new Matrix();
    
    private int surf_width;
    private int surf_height;
    
    SurfaceHolder holder = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i("GameActivity", "onCreate");
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                              WindowManager.LayoutParams.FLAG_FULLSCREEN );

        loadSettings(getSharedPreferences(PREFS_NAME, 0));
        
        view = new GameSurfaceView(this);
        
        holder = view.getHolder();
        holder.setKeepScreenOn(true);
        holder.addCallback(this);
        
        System.out.println("Loading assets");

        try
        {
            Res.loadResources(getAssets());
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            Log.i("GameActivity", "Loading resources error", e);
        }
        Log.i("GameActivity", "Resources Loaded");
        
        System.out.println("Assets loaded");
        
        SoundManager.init(this);
        
        setContentView(view);
    }

    @Override
    protected void onResume()
    {
        Log.i("GameActivity", "onResume");
        super.onResume();
    }
    
    @Override
    protected void onPause()
    {
        Log.i("GameActivity", "onPause");
        super.onPause();
        
        if ( thread != null )
        {
            this.running = false;
            while (true)
            {
                try
                {
                    thread.join();
                    break;
                } catch (InterruptedException e) {}
            }

            thread = null;
        }

        saveSettings(getSharedPreferences(PREFS_NAME, 0));
        Log.i("GameActivity", "onPause end");
    }
    
    public void run()
    {
        int fps_count = 0;
        long last_frame_start = System.nanoTime();
        long accumulator = -1;
        long fps_count_start = last_frame_start;
        long last_second_fps_count = 0;

        while (running)
        {
            final Canvas c = holder.lockCanvas();
            if (c != null)
            try
            {
                final long frame_start = System.nanoTime();
                if (accumulator < 0)
                {
                    last_frame_start = frame_start;
                    accumulator = 0;
                }

                final long fps_time = frame_start - fps_count_start;
                if (fps_time >= 1000000000) // 1s in ns
                {
                    last_second_fps_count = fps_count;
                    fps_count_start = frame_start;
                    fps_count = 0;
                }

                final long last_frame_time = frame_start - last_frame_start;

                // from ns to s
                final float lftime = last_frame_time / 1000000000f;
//                doLogic( lftime );
//                interpol( 1.0f, last_frame_time );

                accumulator += last_frame_time;
                int loopcount = 0;
                while ( ++loopcount <= Config.MAX_LOGIC_LOOP
                        && accumulator >= Config.DELAY_BETWEEN_LOGICS )
                {
                    current_screen = current_screen.logic(Config.LOGIC_FRAMETIME_S);
                    accumulator -= Config.DELAY_BETWEEN_LOGICS;
                }

                current_screen.interpol(lftime);

                Res.fps_number.value = (int)last_second_fps_count;

                c.drawColor(Color.BLACK);
                c.setMatrix(viewMatrix);
                current_screen.drawGame(c);
                
                c.setMatrix(uiMatrix);
                current_screen.drawUI(c);

                ++fps_count;
                last_frame_start = frame_start;
            }
            finally
            {
                holder.unlockCanvasAndPost(c);

            }
        }
    }

    public void surfaceCreated(SurfaceHolder xholder)
    {
        Log.i("GameActivity", "Surface created =>> " + (holder == xholder));
    }

    public void surfaceChanged( SurfaceHolder xholder,
                                int format,
                                int width,
                                int height )
    {
        Log.i("GameActivity", String.format("Surface changed ==> %s: format %d size %dx%d", holder == xholder, format, width, height));
        surf_width = width;
        surf_height = height;

        viewMatrix.reset();
//        final float screen_ratio = height / (float)width;

        final float desired_x = 480f;
        final float x_ratio = width / desired_x;

        Config.screen_x_ratio = x_ratio;
        Config.screen_y_ratio = x_ratio;

        viewMatrix.setScale(x_ratio, x_ratio);
//        viewMatrix.setScale(1, 1);
        uiMatrix.set(viewMatrix);
        viewMatrix.postTranslate(width / 2, height / 2);
        
        System.gc();
        
        if ( thread == null )
        {
            current_screen = getInitialScreen();
            current_screen.enter();
            
            thread = new Thread(this);
            running = true;
            thread.start();
        }
        
    }

    public void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.i("GameActivity", "Surface Destroyed");
        
    }
    
}
