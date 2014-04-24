package dev.aaronps.traction;

import android.graphics.RectF;

public class Config
{
    /*
     * Notes about logic fps:
     * 
     * Each second, times per second which are rounded numbers:
     * 1000/50 = 20ms between
     * 1000/40 = 25ms between
     * 1000/25 = 40ms between
     * 1000/20 = 50ms between
     * 1000/10 = 100ms between
     * 1000/8  = 125ms between
     * 
     * rest are to small to take into account
     * 
     * Notes about sizes (from stackoverflow):
     *  xhdpi = 100% image
     *  hdpi = 75% image of xhdpi image
     *  mdpi = 50% image of xhdpi image
     *  ldpi = 50% image of hdpi image
     *  i.e :
     *  
     *  if you have 96 x 96 image in xhdpi then, you need to put
     *  
     *  72 x 72 in hdpi folder - ( 75 % of xhdpi )
     *  48 x 48 in mdpi folder - ( 50 % of xhdpi )
     *  36 x 36 in ldpi folder - ( 50 % of hdpi )
     * 
     * 
     * 
     */

    
    public static final long LPS = 60;
    public static final long DELAY_BETWEEN_LOGICS = 1000/LPS;
    public static final float ACCEL_PER_SECOND = 25.0f;
    public static final float ACCEL_PER_LOGIC = ACCEL_PER_SECOND / LPS;
    public static final float MAX_SPEED = 240f;
    public static final float MAX_SPEED_LOGIC = MAX_SPEED / LPS;
    public static final float REDUCED_SPEED = 15f;
    public static final float REDUCED_SPEED_LOGIC = REDUCED_SPEED / LPS;
        
    public static final float WORLD_MIN_X = -350f;
    public static final float WORLD_MIN_Y = -450f;
    public static final float WORLD_MAX_X = 350f;
    public static final float WORLD_MAX_Y = 450f;
    
    public static final RectF SHIP_MOVE_AREA = new RectF( -225f, -300f, 225f, 300f);

    public static final int MAX_DEBRIL_COUNT = 30;

    public static final float SHIELD_RADII = 32.0f;
    public static final float DEBRIL_RADII =  8.0f;
    public static final float COMBINED_RADII = SHIELD_RADII + DEBRIL_RADII;
    public static final float COMBINED_RADII_SQ = COMBINED_RADII * COMBINED_RADII;
    
    
    
}
