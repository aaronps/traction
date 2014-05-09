package dev.aaronps.traction;

public class State
{
    public Debril[] debrils = null;
    public int count = 0;
    public Ship ship;

    public State(final int count)
    {
        this.count = count;
        debrils = new Debril[count];
        for (int n = 0; n < count; n++)
        {
            debrils[n] = new Debril();
        }
        ship = new Ship();
    }

}
