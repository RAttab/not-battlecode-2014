package blahbot;

import battlecode.common.*;

class Utils
{
    static final Direction dirs[] = Direction.values();

    static final int SelfDestructRangeSq = 2;

    static Team me;
    static Team him;

    static MapLocation myHq;
    static MapLocation hisHq;


    static void init(RobotController rc)
    {
        me = rc.getTeam();
        him = me.opponent();

        myHq = rc.senseHQLocation();
        hisHq = rc.senseEnemyHQLocation();
    }


    static int ceilDiv(int a, int b)
    {
        if (b == 0) return 1;
        return (a - 1) / b + 1;
    }

    static double distToLineBetween(
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

    static double distTwoPoints(
            double p_x, double p_y, double q_x, double q_y)
    {
        return Math.sqrt((p_x - q_x)*(p_x-q_x) + (p_y-q_y)*(p_y-q_y));
    }


    static boolean canMoveTo(RobotController rc, MapLocation dest, int max)
        throws GameActionException
    {
        final MapLocation pos = rc.getLocation();
        if (Math.sqrt(pos.distanceSquaredTo(dest)) > max)
            return false;

        MapLocation it = rc.getLocation();

        for (int i = 0; i < max; ++i) {
            it = it.add(pos.directionTo(dest));
            if (it.equals(dest)) return true;

            if (!rc.senseTerrainTile(it).isTraversableAtHeight(RobotLevel.ON_GROUND))
                return false;

            if (rc.senseObjectAtLocation(it) != null)
                return false;
        }

        return false;
    }
}
