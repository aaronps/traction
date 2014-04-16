package dev.aaronps.traction;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView
{
    private SurfaceHolder holder;
    private GameLoopThread gameLoopThread;
    
    private Paint timePaint;
    private Paint dotPaint;
    private Paint fpsPaint;
    private boolean configured = false;
    
    public SoundPool soundPool;
    public int explosionSoundId = -1;
    public int shieldHitSoundId = -1;
    
    private Matrix viewMatrix = new Matrix();
    private Matrix identityMatrix = new Matrix();
    
    private static int[] static_decimals = new int[3];
    private static int[] static_inte = new int[20];
    
    private static int NUMBER_WIDTH = 24;
    private static int NUMBER_HEIGHT = 32;
    private static int DOT_WIDTH = 16;
    
    
    // need to set the font size here
    private Rect number_src = new Rect(0, 0, 24, 32);
    private Rect number_dst = new Rect(0, 0, 24, 32);
    
    private int surf_width = 0;
    private int surf_height = 0;
    
    
	public GameView(Context context)
	{
		super(context);
		
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
		final AssetManager am = context.getAssets();
		try
		{
			explosionSoundId = soundPool.load(am.openFd("DeathFlashCut.ogg"), 0);
			shieldHitSoundId = soundPool.load(am.openFd("shield-hit2.ogg"), 0);
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		dotPaint = new Paint();
		dotPaint.setColor(0xffddeedd);
		dotPaint.setStrokeWidth(0);
		
		timePaint = new Paint();
		timePaint.setColor(Color.WHITE);
		timePaint.setTextAlign(Paint.Align.CENTER);
		timePaint.setTextSize(42);
		
		fpsPaint = new Paint();
		fpsPaint.setColor(Color.WHITE);
		fpsPaint.setTextAlign(Paint.Align.RIGHT);
		fpsPaint.setTextSize(18);
		
		gameLoopThread = new GameLoopThread(this);
		holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback()
        {
           @Override
           public void surfaceDestroyed(SurfaceHolder holder)
           {
               gameLoopThread.setRunning(false);
               soundPool.release();
               
               while (true)
               {
            	   try
            	   {
            		   gameLoopThread.join();
                       break;
            	   } catch (InterruptedException e) {}
               }
               soundPool = null;
           }

           @Override
           public void surfaceCreated(SurfaceHolder holder)
           {
        	   System.out.println("Surface created");
        	   gameLoopThread.setRunning(true);
               gameLoopThread.start();
           }

           @Override
           public void surfaceChanged(  SurfaceHolder holder,
        		   						int format,
    		   							int width,
    		   							int height)
           {
        	   System.out.println(String.format("Surface changed: %dx%d", width, height));
        	   surf_width = width;
        	   surf_height = height;
        	   if ( ! configured )
        	   {
        		   viewMatrix.reset();
        		   viewMatrix.setTranslate(width/2, height/2);
        		   gameLoopThread.configureSize(width, height);
        		   configured = true;
        	   }
        	   
        	   System.gc();
           }
        });

        System.out.println("Loading assets");
        
        try {
			GameResources.loadResources(am);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	boolean pressing = false;
	float prev_x = 0;
	float prev_y = 0;
	
	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
//		float eventX = event.getX();
//	    float eventY = event.getY();

	    switch (e.getAction())
	    {
	    	case MotionEvent.ACTION_DOWN:
		    	prev_x = e.getX();
				prev_y = e.getY();
				
				gameLoopThread.onDown(prev_x, prev_y);
				
//				System.out.println(String.format("START: %f,%f", prev_x, prev_y));
				return true;
	    	
	    	case MotionEvent.ACTION_MOVE:
	    	{
	    		final float new_x = e.getX();
	    		final float new_y = e.getY();
	    		final float dx = prev_x - new_x;
	    		final float dy = prev_y - new_y;
	    		
//	    		System.out.println(String.format("MOVE: from %f,%f to %f,%f diff %f,%f",
//	    										  prev_x, prev_y, new_x, new_y, dx, dy));

	    		gameLoopThread.moveShip(dx, dy);

	    		prev_x = new_x;
	    		prev_y = new_y;
	    		
	    	}
    			break;
	    
	    	case MotionEvent.ACTION_UP:
//	    		System.out.println(String.format("END: %f,%f", e.getX(), e.getY()));
	    		break;
	    		
	    	default:
	    		return false;
	    }

		return true;
	}
	
    final void drawState(final Canvas canvas, final DrawState state)
	{
		canvas.drawColor(Color.BLACK);
		canvas.setMatrix(viewMatrix);
		final Sprite[] sprites = state.sprites;
		final int count = state.sprite_count;
		for ( int n = 0; n < count; n++ )
		{
			final Sprite s = sprites[n];
			canvas.drawBitmap( s.image, s.x, s.y, null);
		}
		
		if ( BitmapExplosionParticleSystem.active_particles_count > 0 )
		{
			state.explosions.draw(canvas);
		}
		
		if ( state.topLayerCount > 0 )
		{
			final int tc = state.topLayerCount;
			final Sprite[] ts = state.topLayer;
			for ( int n = 0; n < tc; n++ )
			{
				final Sprite s = ts[n];
				canvas.drawBitmap(s.image, s.x, s.y, null);
			}
		}
		
		canvas.setMatrix(identityMatrix);
		
		// TODO these two lines makes garbage to be collected, need to remove them
		
		{
			final int[] decimals = static_decimals;
			final int[] inte = static_inte;
			
			int maxinte = 0;
			int tmp = (int)state.alive_time;
	        
	        decimals[2] = tmp%10; tmp/=10;
	        decimals[1] = tmp%10; tmp/=10;
	        decimals[0] = tmp%10; tmp/=10;
	        
	        
	        do
	        {
	            inte[maxinte++] = tmp%10;
	            tmp /= 10;
	        } while ( tmp > 0 );
	        
	        final int total_chars = 3 + maxinte;
	        final int total_draw_len = total_chars * NUMBER_WIDTH + DOT_WIDTH;
	        
	        final Rect src = number_src;
	        final Rect dst = number_dst;
	        
	        final int soff = surf_width / 2 - total_draw_len / 2;
	        
	        dst.offsetTo(soff, 0);
	        
	        final Bitmap font = GameResources.numbers_24x32;
	        
	        // NOTE: offset by DOT_WIDTH makes numbers nicely packed!!
	        
	        do
	        {
	        	src.offsetTo(inte[--maxinte] * NUMBER_WIDTH, 0);
	        	canvas.drawBitmap(font, src, dst, null);
	        	dst.offset(DOT_WIDTH, 0);
	        } while ( maxinte > 0 );
	        
	        // note: some part of the dot is not drawn, but who cares, don't know about performance
        	src.offsetTo(10 * NUMBER_WIDTH, 0);
        	canvas.drawBitmap(font, src, dst, null);
        	dst.offset(DOT_WIDTH/2, 0);
        	
        	src.offsetTo(decimals[0] * NUMBER_WIDTH, 0);
        	canvas.drawBitmap(font, src, dst, null);
        	dst.offset(DOT_WIDTH, 0);
        	
        	src.offsetTo(decimals[1] * NUMBER_WIDTH, 0);
        	canvas.drawBitmap(font, src, dst, null);
        	dst.offset(DOT_WIDTH, 0);
        	
        	src.offsetTo(decimals[2] * NUMBER_WIDTH, 0);
        	canvas.drawBitmap(font, src, dst, null);
        	
        	
        	// DRAW FPS
        	
        	tmp = (int) state.last_fps;
        	do
	        {
	            inte[maxinte++] = tmp%10;
	            tmp /= 10;
	        } while ( tmp > 0 );
        	
	        final int fps_draw_len = maxinte * NUMBER_WIDTH + DOT_WIDTH;
        	
	        dst.offsetTo(surf_width - fps_draw_len, 0);
	        
	        do
	        {
	        	src.offsetTo(inte[--maxinte] * NUMBER_WIDTH, 0);
	        	canvas.drawBitmap(font, src, dst, null);
	        	dst.offset(DOT_WIDTH, 0);
	        } while ( maxinte > 0 );
	        
	        // end draw fps
        	
		}
		
		
		
//		canvas.drawText(String.format("%d.%03d", state.alive_time/1000, state.alive_time%1000), canvas.getWidth()/2, 40, timePaint);
//		canvas.drawText(String.format("fps: %d", state.last_fps), canvas.getWidth()-1, 20, fpsPaint);
		
	}

}
