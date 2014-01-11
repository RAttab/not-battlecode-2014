package blahbot;

import battlecode.common.*;


public class Soldier
{

    static void debug_dump(SoldierMicro micro)
    {
        if (Clock.getRoundNum() % 10 > 0) return;

        micro.debug_dump();
    }

    static void move(RobotController rc, Direction dir)
        throws GameActionException
    {
        if (dir == Direction.NONE || dir == Direction.OMNI) {
            rc.breakpoint();
            return;
        }

        final MapLocation pos = rc.getLocation();
        boolean mySide =
            pos.distanceSquaredTo(Utils.myHq) <=
            pos.distanceSquaredTo(Utils.hisHq);

        if (mySide)
            rc.sneak(dir);
        else rc.move(dir);
    }

    public static void run(RobotController rc) throws GameActionException
    {
        BugPathing pathing = null;
        SoldierMicro micro = new SoldierMicro(rc);

        while (true) {
            if (!rc.isActive()) { rc.yield(); continue; }
            ByteCode.Check bcCheck = new ByteCode.Check(rc);

            if (micro.isMicro()) {
                pathing = null;
                micro.exterminate();
            }

            else {
                if (pathing == null)
                    pathing = new BugPathing(rc, Utils.hisHq);

                move(rc, pathing.direction());
            }

            debug_dump(micro);
            bcCheck.debug_check("Soldier.end");
            rc.yield();
        }
    }

}