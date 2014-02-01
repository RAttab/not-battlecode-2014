package blahbot;

import battlecode.common.*;


public class Soldier
{

    static void debug_dump()
    {
        if (Clock.getRoundNum() % 100 > 0) return;

        combat.debug_dump();
        pathing.debug_dump();
    }

    static void move(Direction dir) throws GameActionException
    {
        if (dir == Direction.NONE || dir == Direction.OMNI) {
            rc.breakpoint();
            return;
        }

        boolean sneak;

        final MapLocation pos = rc.getLocation();
        final MapLocation pastr = Utils.pastr(rc);

        if (pastr != null) {
            int pastrDist = pos.distanceSquaredTo(pastr);
            int pastrDir = pos.directionTo(pastr).ordinal();

            final int minRange =
                GameConstants.PASTR_RANGE +
                GameConstants.MOVEMENT_SCARE_RANGE;
            final int maxRange = minRange + 20;

            // heading towards pastr.

            int thresh = Math.abs(pastrDir - dir.ordinal()) <= 2 ? minRange : maxRange;
            sneak = pastrDist < thresh;
        }
        else {
            sneak =
                pos.distanceSquaredTo(Utils.myHq) <=
                pos.distanceSquaredTo(Utils.hisHq);
        }

        if (sneak)
            pathing.sneak(dir);
        else pathing.move(dir);
    }

    static MapLocation reinforce() throws GameActionException
    {
        MapLocation[] spots = comm.spots(2);
        final MapLocation pos = rc.getLocation();

        for (int i = 0; i < spots.length; ++i) {
            if (spots[i] != null && spots[i].distanceSquaredTo(pos) < 100)
                return spots[i];
        }

        return null;
    }

    static boolean build() throws GameActionException
    {
        Robot[] allies = rc.senseNearbyGameObjects(
                Robot.class, RobotType.SOLDIER.sensorRadiusSquared, Utils.me);
        if (allies.length == 0) return false;

        MapLocation pastrPos = null;

        for (int i = allies.length; i-- > 0;) {
            RobotInfo info = rc.senseRobotInfo(allies[i]);

            if (info.type == RobotType.NOISETOWER) return false;

            if (info.type == RobotType.PASTR)
                pastrPos = info.location;

            else if (info.isConstructing) {
                if (info.constructingType == RobotType.PASTR)
                    pastrPos = info.location;
                else return false;
            }
        }

        if (pastrPos == null) return false;
        return rc.getLocation().distanceSquaredTo(pastrPos) < GameConstants.PASTR_RANGE;
    }

    static boolean atTarget(MapLocation myPos)
    {
        if (pathing.getTarget() == null) return true;
        return myPos.distanceSquaredTo(pathing.getTarget()) < 5;
    }

    public static void run(RobotController rc) throws GameActionException
    {
        Soldier.rc = rc;
        Soldier.comm = new Comm(rc);
        Soldier.combat = new SoldierCombat(rc, comm);
        Soldier.pathing = new LookAheadPathing(rc);

        while (true) {

            // \todo Might want to do some calcs in our off turns.
            if (!rc.isActive() || rc.getActionDelay() >= 1.0) {
                rc.yield();
                continue;
            }

            ByteCode.Check bcCheck = new ByteCode.Check(rc);

            if (combat.isCombat() == SoldierCombat.CombatState.YES) {
                rc.setIndicatorString(0, "soldier.combat");
                combat.exterminate();
                pathing.reset();
            }

            else if (build())
                rc.construct(RobotType.NOISETOWER);

            else {
                MapLocation pos;
                MapLocation myPos = rc.getLocation();

                if ((pos = reinforce()) != null) {
                    pathing.setTarget(pos);
                    rc.setIndicatorString(0, "soldier.reinforce");
                }

                else if (rc.getLocation().equals(comm.getRallyPoint())) {
                    rc.construct(RobotType.PASTR);
                    rc.setIndicatorString(0, "soldier.construct");
                }

                else if (comm.hasGlobalOrder() && (pos = comm.globalOrderPos()) != null) {
                    pathing.setTarget(pos);
                    rc.setIndicatorString(0, "soldier.orders: " + pos.toString());
                }

                else if (atTarget(myPos)) {
                    pathing.setTarget(pos = comm.getRallyPoint());
                    rc.setIndicatorString(0, "soldier.rally: " + pos.toString());
                }

                Direction dir = pathing.direction();
                rc.setIndicatorString(0, "soldier.move: " +
                        myPos + ", " + pathing.getTarget() + " -> " + dir.toString());
                move(dir);
            }

            debug_dump();
            bcCheck.debug_check("Soldier.end");
            rc.yield();
        }
    }

    static RobotController rc;
    static LookAheadPathing pathing;
    static Comm comm;
    static SoldierCombat combat;
}