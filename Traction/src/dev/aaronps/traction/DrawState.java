package dev.aaronps.traction;

import dev.aaronps.traction.gamelayers.BackgroundStarsParticleSystem;
import dev.aaronps.traction.gamelayers.BitmapExplosionParticleSystem;
import dev.aaronps.traction.gamelayers.GameLayer;
import dev.aaronps.traction.gamelayers.SparkParticleSystem;
import dev.aaronps.traction.gamelayers.ThrustParticleSystem;
import android.graphics.Bitmap;

public class DrawState
{
	public Sprite[] topLayer;
	public int topLayerCount;
	
	public long alive_time;
	public long last_fps;
	
	private static int MAX_GAME_LAYERS = 5;
	public GameLayer[] game_layers;
	public int game_layer_count;
	
	
	public DrawState(final int max_sprites, final int max_particles)
	{
	    game_layers = new GameLayer[MAX_GAME_LAYERS];
	    game_layer_count = 0;
	    
		topLayerCount = 0;
		topLayer = new Sprite[5];
		for ( int n = 0; n < 5; n++ )
		{
			topLayer[n] = new Sprite();
		}
		
		alive_time = 0;
		last_fps = 0;
	}
	
	public void reset() { game_layer_count = 0; topLayerCount = 0; }
	
	public void addLayer(final GameLayer layer)
	{
	    game_layers[game_layer_count++] = layer;
	}
	
	public void addUI(final Bitmap image, final float x, final float y)
	{
		final Sprite s = topLayer[topLayerCount++];
		s.image = image;
		s.x = x;
		s.y = y;
	}
}
