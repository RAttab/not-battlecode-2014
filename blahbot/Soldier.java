package blahbot;

import battlecode.common.*;


public class Soldier
{

    public static void run(RobotController rc) throws GameActionException
    {
        BugPathing pathing = new BugPathing(rc, Utils.hisHq);

        while (true) {
            if (!rc.isActive()) { rc.yield(); continue; }
            ByteCode.Check bcCheck = new ByteCode.Check(rc);

            pathing.move();

            bcCheck.debug_check("Soldier.end");
            rc.yield();
        }
    }

}