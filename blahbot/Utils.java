package blahbot;

import battlecode.common.*;

public class Utils
{
    public static final Direction dirs[] = Direction.values();

    public static final int SelfDestructRangeSq = 2;

    public static Team me;
    public static Team him;

    public static MapLocation myHq;
    public static MapLocation hisHq;


    public static void init(RobotController rc)
    {
        me = rc.getTeam();
        him = me.opponent();

        myHq = rc.senseHQLocation();
        hisHq = rc.senseEnemyHQLocation();
    }


    public static int ceilDiv(int a, int b)
    {
        if (b == 0) return 1;
        return (a - 1) / b + 1;
    }

    public static double distToLineBetween(
            MapLocation p, MapLocation v, MapLocation w)
    {
        double hqDists = (v.x - w.x)*(v.x - w.x) + (v.y - w.y)*(v.y - w.y);
        double t = ((p.x - v.x)*(w.x - v.x) + (p.y - v.y)*(w.y - v.y)) / hqDists;

        if (t < 0)
            return Math.sqrt(p.distanceSquaredTo(v));
        if (t > 1)
            return Math.sqrt(p.distanceSquaredTo(w));

        double d = 
            distTwoPoints((double)p.x, (double)p.y, v.x+t*(w.x-v.x), v.y+t*(w.y-v.y) );
        return Math.sqrt(d);
    }

    public static double distTwoPoints(
            double p_x, double p_y, double q_x, double q_y)
    {
        return Math.sqrt((p_x - q_x)*(p_x-q_x) + (p_y-q_y)*(p_y-q_y));
    }
}
