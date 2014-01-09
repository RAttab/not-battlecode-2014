package blahbot;

import battlecode.common.*;


public class Soldier
{

    public static void run(RobotController rc) throws GameActionException
    {
        BugPathing pathing = null;
        SoldierMicro micro = new SoldierMicro(rc);

        while (true) {
            if (!rc.isActive()) { rc.yield(); continue; }
            ByteCode.Check bcCheck = new ByteCode.Check(rc);

            if (micro.isMicro) {
                pathing = null;
                micro.exterminate();
            }

            else {
                if (pathing == null)
                    pathing = new BugPathing(rc, Utils.hisHq);
                pathing.move();
            }

            bcCheck.debug_check("Soldier.end");
            rc.yield();
        }
    }

}