package dev.aaronps.traction;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.SurfaceView;
import dev.aaronps.gameengine.GameActivity;
import dev.aaronps.gameengine.Screen;
import dev.aaronps.traction.screens.Screens;

public class Main extends GameActivity
{

    @Override
    public Screen getInitialScreen()
    {
        Screens.MENU_MAIN.ctx = this;
        return Screens.MENU_MAIN;
    }
    
    @Override
    public SurfaceView getView(final Context context)
    {
        return null;
    }

    @Override
    public void loadSettings(final SharedPreferences prefs)
    {
        InputManager.working_mode = prefs.getInt("control_mode", 0);
    }

    @Override
    public void saveSettings(SharedPreferences prefs)
    {
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("control_mode", InputManager.working_mode);
        
        editor.commit();
    }
    
}
