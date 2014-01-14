package blahbot;

import battlecode.common.*;

public class General
{
    General(RobotController rc, Comm comm)
    {
        this.rc = rc;
        this.comm = comm;
    }

    MapLocation furthestVulnerableFrom(MapLocation locations[], MapLocation base)
    {
        int maxDist = 0;
        MapLocation target = null;

        for (int i = 0; i < locations.length; ++i) {
            if (locations[i] == null) continue;

            int dist = locations[i].distanceSquaredTo(base);
            if (dist < RobotType.HQ.attackRadiusMaxSquared) continue;

            if (dist <= maxDist) continue;
            target = locations[i];
            maxDist = dist;
        }

        return target;
    }


    int lastLength = -1;
    MapLocation rallyPoint = null;
    void rally() throws GameActionException
    {
        MapLocation pastrs[] = rc.sensePastrLocations(Utils.me);

        if (pastrs.length == 0) {
            if (Clock.getRoundNum() < 8) {
                if (Clock.getBytecodeNum() < 9000) {
                    Cowdar.search(9000 - Clock.getBytecodeNum());
                }
            }

            rallyPoint = Cowdar.bestSpot.loc;
            
        } else {
            if (pastrs.length == lastLength) return;
            lastLength = pastrs.length;

            rallyPoint = furthestVulnerableFrom(pastrs, Utils.myHq);
            if (rallyPoint == null) {
                Direction rallyDir = Utils.myHq.directionTo(Utils.hisHq);
                rallyPoint = rc.getLocation().add(rallyDir, 2);
            }

        }



        System.out.println("general.rally: " + rallyPoint.toString());
        comm.setRallyPoint(rallyPoint);
    }

    boolean pastrs() throws GameActionException
    {
        MapLocation pastrs[] = rc.sensePastrLocations(Utils.him);
        MapLocation target = furthestVulnerableFrom(pastrs, Utils.hisHq);
        if (target == null) return false;

        System.out.println("general.pastr: " + target.toString());
        comm.setGlobalOrder(target, 4);
        return true;
    }

    boolean enemies() throws GameActionException
    {
        MapLocation comms[] = rc.senseBroadcastingRobotLocations(Utils.him);
        MapLocation targetComms = furthestVulnerableFrom(comms, Utils.hisHq);

        MapLocation spots[] = comm.spots(4);
        MapLocation targetSpots = furthestVulnerableFrom(spots, Utils.hisHq);

        if (targetComms == null && targetSpots == null) return false;

        MapLocation target = null;
        if (targetComms == null) target = targetSpots;
        if (targetSpots == null) target = targetComms;

        if (target == null) {
            int commsDist = targetComms.distanceSquaredTo(Utils.myHq);
            int spotsDist = targetSpots.distanceSquaredTo(Utils.myHq);
            target = commsDist < spotsDist ? targetComms : targetSpots;
        }

        System.out.println("general.enemies: " + target.toString());
        comm.setGlobalOrder(target, 4);
        return true;
    }

    boolean giveOrder()
    {
        Robot bots[] =
            rc.senseNearbyGameObjects(Robot.class, rallyPoint, 100, Utils.me);
        return bots.length >= 4;
    }

    void command() throws GameActionException
    {
        rally();
        if (!giveOrder()) return;

        if (pastrs()) return;
        if (enemies()) return;
    }


    RobotController rc;
    Comm comm;
}