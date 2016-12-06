package dev.aaronps.traction;

public class InputManager
{
    public static class MoveCommand
    {
        public float dir_x;
        public float dir_y;
        public float speed;
    }

    /**
     * Working modes of the input manager: 0 - normal 1 - joystick
     */
    public  static int working_mode = 0;
    private static float joystick_x = 0;
    private static float joystick_y = 0;
    public  static float pointer_x = 0;
    public  static float pointer_y = 0;
    private static float speed = 0;
    private static boolean pressed = false;
    public static boolean pressing = false;

    public static void setNormalMode()
    {
        working_mode = 0;
        speed = Config.POINTER_MOVE_RATIO;
    }

    public static void setJoystickMode()
    {
        working_mode = 1;
        speed = 0;
    }
    
    public static void noMove()
    {
        joystick_x = joystick_y = 0;
    }

    public static void pointerPress(final float x, final float y)
    {
        pressed = true;
        pressing = true;

        joystick_x = 0;
        joystick_y = 0;

        pointer_x = x;
        pointer_y = y;

        speed = 0;
    }

    public static void pointerRelease(final float x, final float y)
    {
        pressed = false;
        pressing = false;
        speed = 0;
    }

    public static void resetPress()
    {
        pressed = false;
        speed = 0;
    }

    public static boolean wasPressed()
    {
        return pressed;
    }

    public static void pointerMove(final float x, final float y)
    {
        if (working_mode == 0)
        {
            joystick_x += x - pointer_x;
            joystick_y += y - pointer_y;
            pointer_x = x;
            pointer_y = y;
        }
        else // mode 1
        {
            final float vx = x - pointer_x;
            final float vy = y - pointer_y;
            final float vdis = (float) Math.sqrt(vx * vx + vy * vy);

            if (vdis >= Config.JOYSTICK_MIN_DISTANCE)
            {
                final float ux = vx / vdis;
                final float uy = vy / vdis;

                // set direction
                joystick_x = ux;
                joystick_y = uy;

                final float final_dis = Math.min(vdis, Config.JOYSTICK_MAX_DISTANCE);
                final float speed_rate = final_dis / Config.JOYSTICK_MAX_DISTANCE;

                // set speed
                speed = Config.JOYSTICK_MAX_SPEED * speed_rate;
            }
            else
            {
                joystick_x = joystick_y = speed = 0;
            }
        }
    }

    public static void getMoveCommand(final MoveCommand mc)
    {
        if (working_mode == 0)
        {
            mc.dir_x = joystick_x / Config.screen_x_ratio;
            mc.dir_y = joystick_y / Config.screen_y_ratio;
            mc.speed = Config.POINTER_MOVE_RATIO;

            joystick_x = joystick_y = 0;
        }
        else
        {
            mc.dir_x = joystick_x;
            mc.dir_y = joystick_y;
            mc.speed = speed;
        }
    }

}
