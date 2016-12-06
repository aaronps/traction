package dev.aaronps.traction;

public class Ship
{
	public float x;
	public float y;

	public float dir_x;
	public float dir_y;
	
	public float speed;
	
	public boolean shield = false;
	public boolean shield_active = false;
	public int shield_counter = 0;
	
	public Ship()
	{
		x = y = dir_x = dir_y = speed = 0f;
	}
	
}
