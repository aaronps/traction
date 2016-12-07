package dev.aaronps.gameengine;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import dev.aaronps.traction.InputManager;

/**
 *
 * @author krom
 */
public class GameSurfaceView extends SurfaceView
{
    Context ctx = null;

    public GameSurfaceView(Context context)
    {
        super(context);
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
    
}
