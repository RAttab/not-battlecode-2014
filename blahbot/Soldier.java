package blahbot;

import battlecode.common.*;


public class Soldier
{

    static void debug_dump()
    {
        if (Clock.getRoundNum() % 10 > 0) return;

        combat.debug_dump();
    }

    static void move(Direction dir) throws GameActionException
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

    static MapLocation reinforce() throws GameActionException
    {
        MapLocation[] spots = comm.spots(2);
        final MapLocation pos = rc.getLocation();

        for (int i = 0; i < spots.length; ++i) {
            if (spots[i] != null && spots[i].distanceSquaredTo(pos) < 400)
                return spots[i];
        }

        return null;
    }

    public static void run(RobotController rc) throws GameActionException
    {
        Soldier.rc = rc;
        Soldier.comm = new Comm(rc);
        Soldier.combat = new SoldierCombat(rc, comm);
        Soldier.pathing = new BugPathing(rc);

        while (true) {

            // \todo Might want to do some calcs in our off turns.
            if (!rc.isActive()) { rc.yield(); continue; }

            ByteCode.Check bcCheck = new ByteCode.Check(rc);

            if (combat.isCombat()) {
                System.out.println("soldier.combat");
                pathing.setTarget(combat.exterminate());
                if (pathing.getTarget() != null)
                    move(pathing.direction());
            }

            else {
                MapLocation pos;

                if ((pos = reinforce()) != null) {
                    pathing.setTarget(pos);
                    System.out.println("soldier.reinforce");
                }

                else if (comm.hasGlobalOrder() && (pos = comm.globalOrderPos()) != null) {
                    pathing.setTarget(pos);
                    System.out.println("soldier.orders: " + pos.toString());
                }

                else if (pathing.getTarget() == null) {
                    pathing.setTarget(pos = comm.getRallyPoint());
                    System.out.println("soldier.rally: " + pos.toString());
                }

                Direction dir = pathing.direction();
                System.out.println("soldier.move: " +
                        pathing.getTarget() + " -> " + dir.toString());
                move(dir);
            }

            debug_dump();
            bcCheck.debug_check("Soldier.end");
            rc.yield();
        }
    }

    static RobotController rc;
    static BugPathing pathing;
    static Comm comm;
    static SoldierCombat combat;
}