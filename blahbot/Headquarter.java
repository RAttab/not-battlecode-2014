package blahbot;

import battlecode.common.*;

public class Headquarter
{

    public static void run(RobotController rc)
        throws GameActionException
    {
        while (true) {
            if (!rc.isActive()) { rc.yield(); continue; }

            // code goes here.

            rc.yield();
        }
    }



}