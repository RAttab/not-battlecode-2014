package farmbot;

import battlecode.common.*;

public class Bases
{

    public static void run(RobotController rc) throws GameActionException
    {
        while (true) {
            Math.random();
            if (!rc.isActive()) { rc.yield(); continue; }

            // code goes here.

            rc.yield();
        }
    }
}
