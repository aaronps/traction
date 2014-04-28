package dev.aaronps.traction;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
	
	
	
	public static void loadResources(final AssetManager am) throws IOException
	{
		{
			final InputStream is = am.open("begin-message.png");
			begin_message = BitmapFactory.decodeStream(is);
			is.close();
		}
		
		{
			final InputStream is = am.open("death-message.png");
			death_message = BitmapFactory.decodeStream(is);
			is.close();
		}
		
		{
			final InputStream is = am.open("number-24x32.png");
			numbers_24x32 = BitmapFactory.decodeStream(is);
			is.close();
		}

		{
			final InputStream is = am.open("ship4.png");
			ship = BitmapFactory.decodeStream(is);
			is.close();
			ship_offset_x = ship.getWidth()/2;
			ship_offset_y = ship.getHeight()/2;
			shipMask = new CollisionMask64();
			shipMask.fromBitmap(ship);
		}
		
		{
			final InputStream is = am.open("ball.png");
			debril = BitmapFactory.decodeStream(is);
			is.close();
			debril_offset_x = debril.getWidth()/2;
			debril_offset_y = debril.getHeight()/2;
			debrilMask = new CollisionMask64();
			debrilMask.fromBitmap(debril);
		}
		
		{
//		    final InputStream is = am.open("aura1.png");
			final InputStream is = am.open("ship-aura-active.png");
			ship_aura = BitmapFactory.decodeStream(is);
			is.close();
			ship_aura_offset_x = ship_aura.getWidth()/2;
			ship_aura_offset_y = ship_aura.getHeight()/2;
		}
		
		{
			final InputStream is = am.open("ship-aura.png");
			ship_aura_active = BitmapFactory.decodeStream(is);
			is.close();
		}
		
		{
			final InputStream is = am.open("explosion-8.png");
			explosion = BitmapFactory.decodeStream(is);
			is.close();
		}
		
		{
		    final InputStream is = am.open("yepart.png");
		    yepart = BitmapFactory.decodeStream(is);
		    is.close();
		}
	}

}
