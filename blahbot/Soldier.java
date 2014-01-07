package blahbot;

import battlecode.common.*;


public class Soldier
{

    public static void run(RobotController rc)
        throws GameActionException
    {
        while (true) {
            if (!rc.isActive()) { rc.yield(); continue; }
            ByteCode.Check bcCheck = new ByteCode.Check(rc);

            // Code goes here.

            bcCheck.debug_check("Soldier.end");
            rc.yield();
        }
    }

}