package dev.aaronps.traction;

public class State
{
    public Debril[] debrils = null;
    public int count = 0;
    public Ship ship;

    public State()
    {
        this.count = Config.MAX_DEBRILS;
        debrils = new Debril[count];
        for (int n = 0; n < count; n++)
        {
            debrils[n] = new Debril();
        }
        ship = new Ship();
    }
    
    final public void initDebrils(final int count)
    {
        float dist_start = 150f //width/3
            , dist_range = 150f //width/2;
            ;
        double angle, dist;

        double pi2 = 2 * Math.PI;

        ship.x = ship.y = 0;

        final int e = debrils.length;
        for (int n = 0; n < e; n++)
        {
            Debril d = debrils[n];

            angle = Math.random() * pi2;
            dist = Math.random() * dist_range + dist_start;

            d.x = (float) (Math.cos(angle) * dist);
            d.y = (float) (Math.sin(angle) * dist);

            final float dirangle = (float) (Math.random() * pi2);
            d.dir_x = (float) Math.cos(dirangle);
            d.dir_y = (float) Math.sin(dirangle);

            d.min_speed = Config.REDUCED_SPEED;
            d.max_speed = (float) Math.random() * (Config.MAX_SPEED / 2)
                        + Config.MAX_SPEED / 2;
            d.speed = 0;
            d.acceleration = Config.ACCEL_PER_SECOND;

        }
    }
    
    final public void calculateNewDirectionsAndSpeeds()
    {
        final double pi2 = 2 * Math.PI;

        final int e = debrils.length;
        for (int n = 0; n < e; n++)
        {
            Debril d = debrils[n];

            final float dirangle = (float) (Math.random() * pi2);
            d.dir_x = (float) Math.cos(dirangle);
            d.dir_y = (float) Math.sin(dirangle);

            d.min_speed = Config.REDUCED_SPEED;
            d.max_speed = (float) Math.random() * (Config.MAX_SPEED / 2)
                        + Config.MAX_SPEED / 2;
            if (d.speed > d.max_speed)
            {
                d.acceleration = 0;
                d.speed = d.max_speed;
            }
            else
            {
                d.acceleration = Config.ACCEL_PER_SECOND;
            }
        }
    }

}
