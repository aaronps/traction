package dev.aaronps.traction;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import dev.aaronps.traction.gamelayers.GameLayer;

public class GameView extends SurfaceView
{
    SurfaceHolder holder;
    GameLoopThread gameLoopThread;

    boolean configured = false;

    Matrix viewMatrix = new Matrix();
    Matrix uiMatrix = new Matrix();

    static int[] static_decimals = new int[3];
    static int[] static_inte = new int[20];

    static int NUMBER_WIDTH = 24;
    static int NUMBER_HEIGHT = 32;
    static int DOT_WIDTH = 16;

    // need to set the font size here
    Rect number_src = new Rect(0, 0, 24, 32);
    Rect number_dst = new Rect(0, 0, 24, 32);

    int surf_width = 0;
    int surf_height = 0;

    final int virtual_width = 480;

    Context ctx = null;

    public GameView(Context context)
    {
        super(context);
        ctx = context;

        gameLoopThread = new GameLoopThread(this);
        holder = getHolder();
        holder.setKeepScreenOn(true);
        holder.addCallback(new SurfaceHolder.Callback()
        {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder)
            {
                gameLoopThread.setRunning(false);

                while (true)
                {
                    try
                    {
                        gameLoopThread.join();
                        break;
                    } catch (InterruptedException e) {}
                }

                SoundManager.deinit();
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder)
            {
                Log.i("GameView", "Surface created");
                SoundManager.init(ctx);
                gameLoopThread.setRunning(true);
                gameLoopThread.start();
            }

            @Override
            public void surfaceChanged( SurfaceHolder holder,
                                        int format,
                                        int width,
                                        int height)
            {
                System.out.println(String.format("Surface changed: format %d size %dx%d", format, width, height));
                surf_width = width;
                surf_height = height;
                if (!configured)
                {
                    viewMatrix.reset();
//        		   final float screen_ratio = height / (float)width;

                    final float desired_x = 480f;
                    final float x_ratio = width / desired_x;

                    Config.screen_x_ratio = x_ratio;
                    Config.screen_y_ratio = x_ratio;

                    viewMatrix.setScale(x_ratio, x_ratio);
                    uiMatrix.set(viewMatrix);
                    viewMatrix.postTranslate(width / 2, height / 2);
                    gameLoopThread.configureSize(width, height);
                    configured = true;
                    System.gc();
                }

            }
        });

        System.out.println("Loading assets");

        try
        {
            GameResources.loadResources(context.getAssets());
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    int pressing_id = -1;

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        final int a = e.getAction();
        final int action = a & MotionEvent.ACTION_MASK;
        final int pointer_index = (a & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointer_id = e.getPointerId(pointer_index);

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (pressing_id == -1)
                {
                    pressing_id = pointer_id;
                    InputManager.pointerPress(e.getX(), e.getY());
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: //doc says not do anything on cancel
                if (pressing_id == pointer_id)
                {
                    pressing_id = -1;
                    InputManager.pointerRelease(e.getX(), e.getY());
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (pressing_id != -1)
                {
                    final int c = e.getPointerCount();
                    for (int n = 0; n < c; n++)
                    {
                        if (e.getPointerId(n) == pressing_id)
                        {
                            InputManager.pointerMove(e.getX(n), e.getY(n));
                        }
                    }
                }
                break;

            default:
                return false;
        }

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // TODO Auto-generated method stub
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        // TODO Auto-generated method stub
        return super.onKeyUp(keyCode, event);
    }

    final void drawMenu(final Canvas canvas, final DrawState state)
    {
        canvas.drawColor(Color.BLACK);

        canvas.setMatrix(viewMatrix);

        final int e = state.game_layer_count;
        final GameLayer[] layers = state.game_layers;
        for (int i = 0; i < e; i++)
        {
            layers[i].draw(canvas);
        }

        canvas.setMatrix(uiMatrix);

        if (state.topLayerCount > 0)
        {
            final int tc = state.topLayerCount;
            final Sprite[] ts = state.topLayer;
            for (int n = 0; n < tc; n++)
            {
                final Sprite s = ts[n];
                canvas.drawBitmap(s.image, s.x, s.y, null);
            }
        }
    }

    final void drawState(final Canvas canvas, final DrawState state)
    {
        canvas.drawColor(Color.BLACK);
        canvas.setMatrix(viewMatrix);

        final int e = state.game_layer_count;
        final GameLayer[] layers = state.game_layers;
        for (int i = 0; i < e; i++)
        {
            layers[i].draw(canvas);
        }

        canvas.setMatrix(uiMatrix);

        if (state.topLayerCount > 0)
        {
            final int tc = state.topLayerCount;
            final Sprite[] ts = state.topLayer;
            for (int n = 0; n < tc; n++)
            {
                final Sprite s = ts[n];
                canvas.drawBitmap(s.image, s.x, s.y, null);
            }
        }

        {
            final int[] decimals = static_decimals;
            final int[] inte = static_inte;

            int maxinte = 0;
            int tmp = (int) (state.alive_time / 1000000); // from ns to ms

            decimals[2] = tmp % 10;
            tmp /= 10;
            decimals[1] = tmp % 10;
            tmp /= 10;
            decimals[0] = tmp % 10;
            tmp /= 10;

            do
            {
                inte[maxinte++] = tmp % 10;
                tmp /= 10;
            } while (tmp > 0);

            final int total_chars = 3 + maxinte;
            final int total_draw_len = total_chars * DOT_WIDTH + DOT_WIDTH;

            final Rect src = number_src;
            final Rect dst = number_dst;

            final int soff = virtual_width / 2 - total_draw_len / 2;

            dst.offsetTo(soff, 0);

            final Bitmap font = GameResources.numbers_24x32;

	        // NOTE: offset by DOT_WIDTH makes numbers nicely packed!!
            do
            {
                src.offsetTo(inte[--maxinte] * NUMBER_WIDTH, 0);
                canvas.drawBitmap(font, src, dst, null);
                dst.offset(DOT_WIDTH, 0);
            } while (maxinte > 0);

            // note: some part of the dot is not drawn, but who cares, don't know about performance
            src.offsetTo(10 * NUMBER_WIDTH, 0);
            canvas.drawBitmap(font, src, dst, null);
            dst.offset(DOT_WIDTH / 2, 0);

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
                inte[maxinte++] = tmp % 10;
                tmp /= 10;
            } while (tmp > 0);

            final int fps_draw_len = maxinte * DOT_WIDTH + DOT_WIDTH;

            dst.offsetTo(virtual_width - fps_draw_len, 0);

            do
            {
                src.offsetTo(inte[--maxinte] * NUMBER_WIDTH, 0);
                canvas.drawBitmap(font, src, dst, null);
                dst.offset(DOT_WIDTH, 0);
            } while (maxinte > 0);

	        // end draw fps
        }

    }

}
