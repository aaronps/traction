package dev.aaronps.traction;

import android.graphics.Bitmap;
import dev.aaronps.traction.gamelayers.GameLayer;

public class DrawState
{
    public static final int TOP_LAYER_MAX_SPRITES = 5;
    public Sprite[] topLayer;
    public int topLayerCount;

    public long alive_time;
    public long last_fps;

    private static final int MAX_GAME_LAYERS = 5;
    public GameLayer[] game_layers;
    public int game_layer_count;

    public DrawState(final int max_sprites, final int max_particles)
    {
        game_layers = new GameLayer[MAX_GAME_LAYERS];
        game_layer_count = 0;

        topLayerCount = 0;
        topLayer = new Sprite[TOP_LAYER_MAX_SPRITES];
        for (int n = 0; n < TOP_LAYER_MAX_SPRITES; n++)
        {
            topLayer[n] = new Sprite();
        }

        alive_time = 0;
        last_fps = 0;
    }

    public final void reset()
    {
        game_layer_count = 0;
        topLayerCount = 0;
    }

    public final void addLayer(final GameLayer layer)
    {
        game_layers[game_layer_count++] = layer;
    }

    public final void addUI(final Bitmap image, final float x, final float y)
    {
        final Sprite s = topLayer[topLayerCount++];
        s.image = image;
        s.x = x;
        s.y = y;
    }
}
