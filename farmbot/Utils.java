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

    public static int[] samples_5 = {-4, 2, 1, -1, 5, 3, -2, 3, -2, -5, -2, -1, -4, 5, 3, 2, -5, -4, 2, 1, -4, -2, 5, -3, 5, -5, -5, 4, -4, 1, 5, -5, -2, 3, -3, 4, 1, 2, -2, -1, 1, 5, -1, -2, 3, -1, 5, -3, -1, 3, 1, 4, -5, -2, -4, -3, 4, 3, -2, 1, -4, 4, -4, 3, 5, 5, -4, 1, 2, 2, 1, 2, 0, -3, 0, 5, 0, 5, 5, 1, -5, 0, 3, 2, 4, -1, -3, 3, -5, 0, 3, 4, 3, -4, 2, 5, -3, 2, 2, 0, -5, -3, -3, -5, 2, -1, 3, -3, -4, 5, 5, -3, -3, 2, 0, -4, 4, 0, 3, -4, -5, -5, 0, 4, 2, 1, -3, 3, 5, 5, -5, 4, -3, -3, -5, 1, 1, -4, -2, -5, -1, 0, -5, -1, -3, 4, 1, -1, -5, 0, -4, 2, 4, 2, 3, -5, 2, 4, 0, 5, 4, -2, -2, 5, -2, 4, 4, -4, 1, -3, -2, 5, 5, 3, 5, 3, -5, 1, -3, 1, -1, -4, 3, 3, 3, -1, 4, -4, -4, -1, 2, 0, 5, 1, 3, 2, -3, -5, -2, -4, -4, -2, -3, 0, 3, -1, -2, -4, -4, 1, -4, 3, 0, -1, 3, -3, 4, 0, 0, 4, 5, 3, 0, -4, -4, -5, 1, -4, 1, 4, -3, 4, 3, -3, -5, 3, 5, -3, 0, 2, -1, 2, 0, 0, -4, 4, 5, 2, 5, -1, 5, 2, 5, 3, 2, -3, -5, 4, -4, -1, -3, 4, -2, 1, 1, -4, -2, -2, -3, -1, 5, 1, 1, 4, -4, 2, 5, 1, -1, -3, -4, 4, -5, 1, 0, -2, 4, 3, -5, -5, 5, 4, 5, 1, 0, 5, 5, -5, -4, 4, -1, -3, -2, -2, -1, 0, 5, -5, -2, 3, 5, -1, -1, 2, 2, -3, -3, -4, 0, -4, 3, -4, 4, -1, -4, -4, 4, -5, -5, 1, -1, 3, 2, -5, 3, -2, -3, 5, -5, 1, -3, -4, -3, 5, 5, 3, 4, 0, -3, 1, -2, 3, -1, 2, 2, 0, 4, -4, 4, 0, 2, -3, -4, -2, 4, -1, 1, 0, 5, 4, 4, -4, -3, -4, 5, 0, -1, 5, -5, -4, 4, -3, 4, -4, -3, -1, 5, 0, 5, -1, -1, -5, -5, 3, 0, 5, -5, 1, 1, -3, -1, -4, 0, -1, -1, -1, 1, -2, -4, 2, -4, 4, 0, -5, 1, 2, 0, -2, 0, -2, 5, 1, -2, -5, -5, 5, 1, -2, 5, 4, 3, -1, -4, -2, 4, 3, 5, -1, -2, -2, -4, -1, 1, -4, 2, -5, 3, 4, 5, 2, -5, 2, 4, 5, 3, -5, -2, 3, 1, -3, -1, -4, -4, 0, 5, -5, 4, 4, 4, -1, 5, 2, -5, 5, 1, -1, 3, 4, -1, -3, 0, -3, -2, -3, 0, -1, 0, 1, 2, -2, -4, 5, 3, -1, 4, -2, 2, -2, 2, -1};
    public static int samples_5_i;


    public static void init(RobotController rc)
    {
        me = rc.getTeam();
        him = me.opponent();

        myHq = rc.senseHQLocation();
        hisHq = rc.senseEnemyHQLocation();

        rand = new Random();
        rand.setSeed((long)rc.getRobot().getID());

        samples_5_i = -1;
    }

    public static int rand_5() {
        samples_5_i = ++samples_5_i % 500;
        return samples_5[samples_5_i];
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
