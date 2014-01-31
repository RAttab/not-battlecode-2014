package blahbot;

import battlecode.common.*;

public class Tower
{
    static int attackRange =
        (int) Math.sqrt(RobotType.NOISETOWER.attackRadiusMaxSquared);
    static int senseRange =
        (int) Math.sqrt(RobotType.NOISETOWER.sensorRadiusSquared);


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


    static boolean inPastr(MapLocation pos, MapLocation pastr)
        throws GameActionException
    {
        return pos.distanceSquaredTo(pastr) < GameConstants.PASTR_RANGE;
    }

    static int op = 3;
    static Direction sector = null;
    static MapLocation lastTarget = null;
    static void shoot(MapLocation pastr) throws GameActionException
    {
        if (lastTarget == null) {
            lastTarget = pos;
            sector = pastr.directionTo(pos);
        }

        if (--op < 0) {
            lastTarget = lastTarget.add(pastr.directionTo(lastTarget), -1);
            op = pastr.distanceSquaredTo(lastTarget) > 100 ? 2 : 0;
        }

        if (pastr.distanceSquaredTo(lastTarget) < GameConstants.PASTR_RANGE + 4) {
            sector = Utils.dirs[(sector.ordinal() + 1) % 8];
            lastTarget = pos.add(sector, attackRange);

            while (!rc.canAttackSquare(lastTarget))
                lastTarget = lastTarget.add(sector, -1);
        }

        MapLocation target = lastTarget;
        boolean light = false;
        if (op > 0) {
            int k = (op - 1) * 2 - 1;
            int dir = (sector.ordinal() + k) % 8;
            if (dir < 0) dir = 8 + dir;

            light = true;
            target = target.add(Utils.dirs[dir], -3);
        }

        while (!rc.canAttackSquare(target))
            target = target.add(sector, -1);

        if (light) rc.attackSquareLight(target);
        else rc.attackSquare(target);
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
        Tower.pos = rc.getLocation();

        // analyze(pastr());
        // rc.yield();

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

            rc.yield();
        }
    }

    static RobotController rc;
    static Comm comm;
    static MapLocation pos;
}
