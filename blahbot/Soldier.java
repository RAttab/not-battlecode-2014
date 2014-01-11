package blahbot;

import battlecode.common.*;


public class Soldier
{

    public static void debug_dump(SoldierMicro micro)
    {
        if (Clock.getRoundNum() % 10 > 0) return;

        micro.debug_dump();
    }

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

            debug_dump(micro);
            bcCheck.debug_check("Soldier.end");
            rc.yield();
        }
    }

}