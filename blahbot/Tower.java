package blahbot;

import battlecode.common.*;

public class Tower
{
    static MapLocation pastr() throws GameActionException
    {
        MapLocation[] pastrs = rc.sensePastrLocations(Utils.me);
        if (pastrs.length == 0) return null;

        int minDist = 50;
        MapLocation pastr = null;
        MapLocation pos = rc.getLocation();

        for (int i = pastrs.length; i-- > 0;) {
            int dist = pos.distanceSquaredTo(pastrs[i]);
            if (dist > minDist) continue;

            minDist = dist;
            pastr = pastrs[i];;
        }

        return pastr;
    }

    static void analyse(MapLocation pastr) throws GameActionException
    {

    }

    static void shoot(MapLocation pastr) throws GameActionException
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

            MapLocation pastr = pastr();

            // No use keeping this tower alive so reclaim our supply.
            if (pastr == null) {
                System.out.println("Game over man! Game over!");
                rc.selfDestruct();
                return;
            }

            if (rc.isActive()) shoot(pastr);
            else analyse(pastr);

            rc.yield();
        }
    }

    static RobotController rc;
    static Comm comm;
    static double[][] growth;
}
