package dev.aaronps.traction;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class Main extends Activity
{
    private static final String PREFS_NAME = "GamePrefs";
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                              WindowManager.LayoutParams.FLAG_FULLSCREEN );

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        
        InputManager.working_mode = prefs.getInt("control_mode", 0);
        
        setContentView(new GameView(this));
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("control_mode", InputManager.working_mode);
        
        editor.commit();
    }

    
    
}
