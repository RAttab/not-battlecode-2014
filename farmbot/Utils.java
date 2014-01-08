package farmbot;

import battlecode.common.*;
import java.util.*;

public class Utils
{
    public static Random rand;

    public static final Direction dirs[] = Direction.values();

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

        rand = new Random();
    }

    public static Direction randomDir() {
        return dirs[rand.nextInt(8)];
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
            return distTwoPoints(p, v);
        if (t > 1)
            return distTwoPoints(p, w);

        double d = 
            distTwoPoints((double)p.x, (double)p.y, v.x+t*(w.x-v.x), v.y+t*(w.y-v.y) );
        return Math.sqrt(d);
    }

    public static double distTwoPoints(MapLocation p, MapLocation q)
    {
        return Math.sqrt((p.x - q.x)*(p.x-q.x) + (p.y-q.y)*(p.y-q.y));
    }

    public static double distTwoPoints(
            double p_x, double p_y, double q_x, double q_y)
    {
        return Math.sqrt((p_x - q_x)*(p_x-q_x) + (p_y-q_y)*(p_y-q_y));
    }

    public static double getRealDist (MapLocation origin, MapLocation dest) {
        // TODO
        // return the actual distance to the given location with A*. 
        // Should only be called for small distances
        return 0;
    }

    public static Robot[] nearbyEnemies(RobotController rc)
        throws GameActionException
    {
        return rc.senseNearbyGameObjects(
                Robot.class, rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
    }

    public static Robot[] nearbyAllies(RobotController rc)
        throws GameActionException
    {
        return rc.senseNearbyGameObjects(
                Robot.class, rc.getType().sensorRadiusSquared, rc.getTeam());
    }
}
