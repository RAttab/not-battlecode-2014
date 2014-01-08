package farmbot;

import battlecode.common.*;


public class Soldier
{

    public static void run(RobotController rc)
        throws GameActionException
    {
        while (true) {
            if (!rc.isActive()) { rc.yield(); continue; }
            ByteCode.Check bcCheck = new ByteCode.Check(rc);

            

            bcCheck.debug_check("Soldier.end");
            rc.yield();
        }
    }


    public static void move_toward(RobotController rc, MapLocation loc) 
    {
        Direction dir = rc.direction
        if (rc.canMove())
    }
}