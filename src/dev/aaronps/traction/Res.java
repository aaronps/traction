package dev.aaronps.traction;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import dev.aaronps.traction.ui.UIButton;
import dev.aaronps.traction.ui.UIImage;
import dev.aaronps.traction.ui.UINumber;

public class Res
{
    public static final int NUMBER_WIDTH = 16;
    public static final int NUMBER_HEIGHT = 32;
    public static final Rect number_src = new Rect(0, 0, 16, 32);
    public static final Rect number_dst = new Rect(0, 0, 16, 32);
   
    public static Bitmap begin_message_text = null;
    public static Bitmap death_message_text = null;
    public static Bitmap numbers_16x32 = null;
    public static Bitmap numbers_16x24 = null;
    public static Bitmap score_order = null;
    public static Bitmap buttons_text = null;
    
    public static Bitmap star_bitmap = null;
    public static Rect star_rect = new Rect( 0, 0, 16, 16);
    
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
    public static Bitmap scores_image = null;
    public static UIImage scores_bg = null;

    public static Bitmap pause = null;
    
    public static UIImage begin_message = null;
    public static UIImage death_message = null;
    
    public static UIButton button_start = null;
    public static UIButton button_config = null;
    public static UIButton button_exit = null;
    
    public static UIButton button_back = null;
    public static UIButton button_direct = null;
    public static UIButton button_direct_on = null;
    public static UIButton button_joystick = null;
    public static UIButton button_joystick_on = null;
    
    public static UIButton button_pause = null;
    
    public static UIButton button_continue = null;
    public static UIButton button_menu = null;
    
    public static UINumber fps_number = null;
    public static UINumber alive_time_number = null;
        
    private static Bitmap createMenuButton(final String text, final Paint paint)
    {
        final Bitmap b = Bitmap.createBitmap(Config.MENU_BUTTON_WIDTH,
                                             Config.MENU_BUTTON_HEIGHT,
                                             Bitmap.Config.ARGB_8888);

        final Canvas c = new Canvas(b);
//        c.drawColor(Config.MENU_BUTTON_BACKGROUND);
        c.drawText( text,
                    Config.MENU_BUTTON_WIDTH / 2,
                    Config.MENU_BUTTON_HEIGHT / 2 + Config.MENU_BUTTON_TEXT_SIZE / 3,
                    paint);
        return b;
    }

    private static Bitmap loadBitmap(final InputStream is) throws IOException
    {
        final Bitmap b = BitmapFactory.decodeStream(is);
        is.close();
        return b;
    }

    public static void prepareMenu()
    {
//        text_play = createMenuButton("开始", paint);
//        text_config = createMenuButton("设置", paint);
//        text_exit = createMenuButton("退出", paint);
       
        button_start  = new UIButton(buttons_text, new Rect(  0,  0,256,100), 100, 200);
        button_config = new UIButton(buttons_text, new Rect(256,  0,512,100), 100, 350);
        button_exit   = new UIButton(buttons_text, new Rect(  0,100,256,200), 100, 500);
        
        button_back     = new UIButton(buttons_text, new Rect(  0,200,256,300), 100, 200);
        button_direct   = new UIButton(buttons_text, new Rect(  0,300,256,400), 100, 350);
        button_direct_on= new UIButton(buttons_text, new Rect(256,300,512,400), 100, 350);
        button_joystick = new UIButton(buttons_text, new Rect(  0,400,256,500), 100, 500);
        button_joystick_on = new UIButton(buttons_text, new Rect(256,400,512,500), 100, 500);
        
        button_continue = new UIButton(buttons_text, new Rect(256,200,512,300), 100, 200);
        button_menu     = new UIButton(buttons_text, new Rect(256,100,512,200), 100, 500);
        
        begin_message = new UIImage(begin_message_text,
                        (480 / 2) - (begin_message_text.getWidth() / 2),
                        (800 / 2) - (begin_message_text.getHeight() * 2));
        
        death_message = new UIImage(death_message_text,
                        (480 / 2) - (death_message_text.getWidth() / 2),
                        (800 / 2) - (death_message_text.getHeight() * 2));
        
        button_pause = new UIButton(pause, new Rect(0,0,pause.getWidth(), pause.getHeight()), 0, 0);
        
        fps_number = new UINumber(0, 472, 0, 2);
        alive_time_number = new UINumber(3, 240, 0, 1);
        
        scores_bg = new UIImage(scores_image, 64, 64);
    }

    public static void loadResources(final AssetManager am) throws IOException
    {
        pause = loadBitmap(am.open("pause.png"));
        buttons_text = loadBitmap(am.open("texts.png"));
        begin_message_text = loadBitmap(am.open("begin-message.png"));
        death_message_text = loadBitmap(am.open("death-message.png"));
        

	// load pics
//        numbers_24x32 = loadBitmap(am.open("number-24x32.png"));
        numbers_16x32 = loadBitmap(am.open("number-16x32.png"));
        numbers_16x24 = loadBitmap(am.open("number-16x24-yellow.png"));
        scores_image = loadBitmap(am.open("scores.png"));
        score_order = loadBitmap(am.open("score-order.png"));
        ship = loadBitmap(am.open("ship4.png"));
        debril = loadBitmap(am.open("ball.png"));
        ship_aura = loadBitmap(am.open("ship-aura-active.png"));
        ship_aura_active = loadBitmap(am.open("ship-aura.png"));
        explosion = loadBitmap(am.open("explosion-8.png"));
        yepart = loadBitmap(am.open("yepart.png"));
        star_bitmap = loadBitmap(am.open("star.png"));

        prepareMenu();
        
        // setup some parameters
        ship_offset_x = ship.getWidth() / 2;
        ship_offset_y = ship.getHeight() / 2;
        shipMask = new CollisionMask64();
        shipMask.fromBitmap(ship);

        debril_offset_x = debril.getWidth() / 2;
        debril_offset_y = debril.getHeight() / 2;
        debrilMask = new CollisionMask64();
        debrilMask.fromBitmap(debril);

        ship_aura_offset_x = ship_aura.getWidth() / 2;
        ship_aura_offset_y = ship_aura.getHeight() / 2;
    }

}
