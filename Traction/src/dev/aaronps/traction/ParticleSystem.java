package dev.aaronps.traction;

import android.graphics.Canvas;

public interface ParticleSystem
{
	public void logic(final long time);
	public void draw(final Canvas canvas);
}
