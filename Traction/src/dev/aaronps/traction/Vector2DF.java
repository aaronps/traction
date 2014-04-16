package dev.aaronps.traction;

public class Vector2DF
{
	public static final float PI2 = (float)(Math.PI*2);
	public float x;
	public float y;
	
	public Vector2DF() { x = y = 0f; }
	public Vector2DF(final float x, final float y) { this.x = x; this.y = y; }
	
	final public float mag()
	{
		return (float)Math.sqrt(x*x + y*y);
	}
	
	final public float angle()
	{
		final float angle = (float)Math.atan2(y, x);
    
	    if (angle < 0)
	    {
	    	return angle + PI2;
	    }
	     
	    return angle;
	}
	
	final public float dot(final Vector2DF a)
	{
		return x * a.x + y * a.y;
	}
	
	final public float angle(final Vector2DF a)
	{
		return (float)Math.acos( dot(a) / (mag() * a.mag()));
	}
	
	final public void normalize()
	{
		final float m = mag();
		x /= m;
		y /= m;
	}
	
	final public void normalRight(final Vector2DF out)
	{
		out.x = -y;
		out.y = x;
		// or y, -x;
	}
	
	final public void normalLeft(final Vector2DF out)
	{
		out.x = y;
		out.y = -x;
	}
	
	public static void add(final Vector2DF a, final Vector2DF b, final Vector2DF out)
	{
		out.x = a.x + b.x;
		out.y = a.y + b.y;
	}
	
	public static void sub(final Vector2DF a, final Vector2DF b, final Vector2DF out)
	{
		out.x = a.x - b.x;
		out.y = a.y - b.y;
	}
	
	public static void mul(final Vector2DF a, final float v, final Vector2DF out)
	{
		out.x = a.x * v;
		out.y = a.y * v;
	}

}
