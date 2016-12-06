package dev.aaronps.traction;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

public class GameResources
{
	public static Bitmap begin_message = null;
	public static Bitmap death_message = null;
	public static Bitmap numbers_24x32 = null;
	
	public static Bitmap ship = null;
	public static int ship_offset_x = 0;
	public static int ship_offset_y = 0;
	public static CollisionMask64 shipMask = null;
	
	public static Bitmap ship_aura = null;
	public static Bitmap ship_aura_active = null;
	public static int ship_aura_offset_x = 0;
	public static int ship_aura_offset_y = 0;
	
	public static Bitmap debril = null;
	public static int debril_offset_x = 0;
	public static int debril_offset_y = 0;
	public static CollisionMask64 debrilMask = null;
	
	public static Bitmap explosion = null;
	
	public static int shieldHitSound = -1;
	public static int explosionSound = -1;
	
	public static Bitmap yepart = null;
	
	public static Bitmap menu_play = null;
	public static Bitmap menu_config = null;
	public static Bitmap menu_exit = null;
	
	private static final Bitmap createMenuButton(final String text, final Paint paint)
	{
	    final Bitmap b = Bitmap.createBitmap( Config.MENU_BUTTON_WIDTH,
                                              Config.MENU_BUTTON_HEIGHT,
                                              Bitmap.Config.ARGB_8888 );

        final Canvas c = new Canvas( b );
        c.drawColor( Config.MENU_BUTTON_BACKGROUND );
        c.drawText( text,
                    Config.MENU_BUTTON_WIDTH/2,
                    Config.MENU_BUTTON_HEIGHT/2 + Config.MENU_BUTTON_TEXT_SIZE/3,
                    paint );
	    return b;
	}
	
	private static final Bitmap loadBitmap( final InputStream is ) throws IOException
	{
	    final Bitmap b = BitmapFactory.decodeStream(is);
	    is.close();
	    return b;
	}
	
	public static void prepareMenu()
	{
	    final Paint paint = new Paint();
	    paint.setTextSize( Config.MENU_BUTTON_TEXT_SIZE );
	    paint.setColor( 0xffefefef );
	    paint.setTextAlign( Paint.Align.CENTER );
	    paint.setAntiAlias( true );
	    
	    menu_play   = createMenuButton( "开始", paint );
	    menu_config = createMenuButton( "设置", paint );
	    menu_exit   = createMenuButton( "退出", paint );
	    
	}
	
	public static void loadResources(final AssetManager am) throws IOException
	{
	    prepareMenu();

	    // load pics
	    
	    begin_message     = loadBitmap( am.open("begin-message.png") );
		death_message     = loadBitmap( am.open("death-message.png" ) );
		numbers_24x32     = loadBitmap( am.open("number-24x32.png") );
		ship              = loadBitmap( am.open("ship4.png") );
		debril            = loadBitmap( am.open("ball.png") );
		ship_aura         = loadBitmap( am.open("ship-aura-active.png") );
		ship_aura_active  = loadBitmap( am.open("ship-aura.png") );
		explosion         = loadBitmap( am.open("explosion-8.png") );
		yepart            = loadBitmap( am.open("yepart.png") );

		// setup some parameters
		
		ship_offset_x = ship.getWidth()/2;
		ship_offset_y = ship.getHeight()/2;
		shipMask = new CollisionMask64();
		shipMask.fromBitmap(ship);

		debril_offset_x = debril.getWidth()/2;
		debril_offset_y = debril.getHeight()/2;
		debrilMask = new CollisionMask64();
		debrilMask.fromBitmap(debril);
		
		ship_aura_offset_x = ship_aura.getWidth()/2;
		ship_aura_offset_y = ship_aura.getHeight()/2;
	}

}
