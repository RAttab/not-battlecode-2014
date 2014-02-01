package blahbot;

import battlecode.common.*;

public class Pastr
{

    static void spot() throws GameActionException
    {
        Robot[] enemies = rc.senseNearbyGameObjects(
                Robot.class, RobotType.PASTR.sensorRadiusSquared, Utils.him);
        if (enemies.length == 0) return;
        comm.spot(rc.getLocation());
    }

    public static void run(RobotController rc) throws GameActionException
    {
        Pastr.rc = rc;
        Pastr.comm = new Comm(rc);
        ProgressQuest pq = new ProgressQuest(rc);

        while (true) {
            spot();
            pq.progress();
            rc.yield();
        }
    }

    static RobotController rc;
    static Comm comm;
}
