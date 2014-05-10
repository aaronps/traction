package dev.aaronps.traction;

import android.graphics.Bitmap;
import dev.aaronps.traction.gamelayers.GameLayer;
import dev.aaronps.traction.ui.UIElement;

public class DrawState
{
    public static final int MAX_UI_ELEMENTS = 5;
    public UIElement[] uiElements;
    public int uiElementCount;

    public float alive_time;
    public long last_fps;

    private static final int MAX_GAME_LAYERS = 5;
    public GameLayer[] game_layers;
    public int game_layer_count;

    public DrawState(final int max_sprites, final int max_particles)
    {
        game_layers = new GameLayer[MAX_GAME_LAYERS];
        game_layer_count = 0;

        uiElementCount = 0;
        uiElements = new UIElement[MAX_UI_ELEMENTS];

        alive_time = 0f;
        last_fps = 0;
    }

    public final void reset()
    {
        game_layer_count = 0;
        uiElementCount = 0;
    }

    public final void addLayer(final GameLayer layer)
    {
        game_layers[game_layer_count++] = layer;
    }

    public final void addUI(final UIElement ui)
    {
        uiElements[uiElementCount++] = ui;
    }
}
