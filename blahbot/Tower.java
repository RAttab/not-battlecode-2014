package blahbot;

import battlecode.common.*;

public class Tower
{
    static void analyse() throws GameActionException
    {

    }

    static void shoot() throws GameActionException
    {
        final int senseRd = RobotType.NOISETOWER.sensorRadiusSquared;
        final int attackRd = RobotType.NOISETOWER.attackRadiusMaxSquared;


    }

    static void spot() throws GameActionException
    {
        Robot[] enemies = rc.senseNearbyGameObjects(
                Robot.class, RobotType.PASTR.sensorRadiusSquared, Utils.him);
        if (enemies.length == 0) return;
        comm.spot(rc.getLocation());
    }

    public static void run(RobotController rc) throws GameActionException
    {
        Tower.rc = rc;
        Tower.comm = new Comm(rc);
        growth = rc.senseCowGrowth();

        while (true) {
            spot();

            if (rc.isActive()) shoot();
            else analyse();

            rc.yield();
        }
    }

    static RobotController rc;
    static Comm comm;
    static double[][] growth;
}
