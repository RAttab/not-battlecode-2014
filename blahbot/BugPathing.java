package blahbot;

import battlecode.common.*;

class BugPathing
{

    BugPathing(RobotController rc)
    {
        this.rc = rc;
        touched = new boolean[rc.getMapWidth()][rc.getMapHeight()];
    }

    MapLocation getTarget() { return target; }
    void setTarget(MapLocation pos)
    {
        if (pos.equals(target)) return;
        target = pos;
    }

    void reset()
    {
        unreachable = false;
        backtrackOrd = Direction.NONE.ordinal();
    }

    /* \todo Always turning to the right isn't that great of an idea but we can
             improve later.
    */
    Direction direction()
    {
        if (unreachable || target == null) {
            rc.breakpoint();
            return Direction.NONE;
        }

        Direction result = Direction.NONE;
        MapLocation pos = rc.getLocation();
        if (pos.equals(target)) return result;

        Direction targetDir = pos.directionTo(target);
        int ord = targetDir.ordinal();

        for (int i = 8; --i > 0; ord = (ord + 1) % 8) {
            if (ord == backtrackOrd) continue;

            Direction dir = Utils.dirs[ord];
            if (!rc.canMove(dir)) continue;

            result = dir;
            break;
        }

        // got stuck.
        if (result == Direction.NONE) return result;

        MapLocation newPos = pos.add(result);
        if (touched[newPos.x][newPos.y]) {
            unreachable = true;
            return Direction.NONE;
        }

        touched[pos.x][pos.y] = true;
        backtrackOrd = result.opposite().ordinal();
        return result;
    }

    boolean move() throws GameActionException
    {
        Direction dir = direction();
        if (dir == Direction.NONE) return false;

        move(dir);
        return true;
    }

    void move(Direction dir) throws GameActionException
    {
        if (dir == Direction.NONE) {
            System.out.println("ERROR: Move to NONE");
            rc.breakpoint();
            return;
        }

        rc.move(dir);
    }

    boolean sneak() throws GameActionException
    {
        Direction dir = direction();
        if (dir == Direction.NONE) return false;

        sneak(dir);
        return true;
    }

    void sneak(Direction dir) throws GameActionException
    {
        if (dir == Direction.NONE) {
            System.out.println("ERROR: Sneak to NONE");
            rc.breakpoint();
            return;
        }

        rc.sneak(dir);
    }

    MapLocation target;

    int backtrackOrd = Direction.NONE.ordinal();

    boolean touched[][];
    boolean unreachable = false;

    RobotController rc;
}