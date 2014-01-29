package blahbot;

import battlecode.common.*;

public class Tower
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
