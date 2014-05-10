package dev.aaronps.traction;

import java.io.IOException;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import dev.aaronps.traction.gamelayers.GameLayer;
import dev.aaronps.traction.ui.UIElement;

public class GameView extends SurfaceView
{
    SurfaceHolder holder;
    GameLoopThread gameLoopThread;

    boolean configured = false;

    Matrix viewMatrix = new Matrix();
    Matrix uiMatrix = new Matrix();

    int surf_width = 0;
    int surf_height = 0;

    final int virtual_width = 480;

    Context ctx = null;

    public GameView(Context context)
    {
        super(context);
        ctx = context;
        
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
        
        System.out.println("Assets loaded");

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

        if (state.uiElementCount > 0)
        {
            final int c = state.uiElementCount;
            final UIElement[] elements = state.uiElements;
            for (int n = 0; n < c; n++)
            {
                elements[n].draw(canvas);
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

        if (state.uiElementCount > 0)
        {
            final int c = state.uiElementCount;
            final UIElement[] elements = state.uiElements;
            for (int n = 0; n < c; n++)
            {
                elements[n].draw(canvas);
            }
        }
    }

}
