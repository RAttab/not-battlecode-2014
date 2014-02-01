package blahbot;

import battlecode.common.*;

class LookAheadPathing
{

    LookAheadPathing(RobotController rc)
    {
        this.rc = rc;
    }


    MapLocation getTarget() { return target; }
    void setTarget(MapLocation pos)
    {
        if (pos == null) target = null;
        else if (!pos.equals(target)) target = pos;
    }

    void reset()
    {
        prev = null;
        wallHugging = false;
    }

    Direction direction() throws GameActionException
    {
        prof.debug_start();

        final MapLocation pos = rc.getLocation();
        Direction targetDir = pos.directionTo(target);
        Direction facing = prev == null ? targetDir : prev.directionTo(pos);

        // System.out.println("");
        // System.out.println("[ pathing ]=========================================");
        // System.out.println("pos: " + pos);
        // System.out.println("prev: " + prev);
        // System.out.println("facing: " + facing);
        // System.out.println("target: " + target);
        // System.out.println("targetDir: " + targetDir);

        if (!wallHugging) {
            if (rc.canMove(targetDir)) {
                prof.debug_stop();
                return targetDir;
            }

            wallHugging = rc.senseObjectAtLocation(pos.add(targetDir)) == null;
            MapLocation right = lookAhead(pos, facing, true);
            MapLocation left = lookAhead(pos, facing, false);

            rotateRight =
                right.distanceSquaredTo(target) <=
                left.distanceSquaredTo(target);
        }

        Direction dir = hug(pos, facing, rotateRight);
        wallHugging = wallHugging && dir != targetDir;

        prof.debug_stop();
        return dir;
    }


    MapLocation lookAhead(MapLocation pos, Direction facing, boolean rotateRight)
        throws GameActionException
    {
        lookAheadProf.debug_start();

        MapLocation prev;
        for (int i = 2; i-- > 0;) {
            prev = pos;
            pos = pos.add(hug(pos, facing, rotateRight));
            facing = prev.directionTo(pos);
        }

        lookAheadProf.debug_stop();
        return pos;
    }

    Direction rotate(Direction dir, boolean right)
    {
        return right ? dir.rotateRight() : dir.rotateLeft();
    }

    boolean canMove(MapLocation pos, Direction dir) throws GameActionException
    {
        if (pos.equals(rc.getLocation())) return rc.canMove(dir);

        pos = pos.add(dir);
        TerrainTile tile = rc.senseTerrainTile(pos);
        if (!tile.isTraversableAtHeight(RobotLevel.ON_GROUND)) return false;
        return rc.senseObjectAtLocation(pos) == null;
    }

    Direction hug(MapLocation pos, Direction facing, boolean rotateRight)
        throws GameActionException
    {
        hugProf.debug_start();

        if (facing.equals(pos.directionTo(target)) && canMove(pos, facing)) {
            hugProf.debug_stop();
            return facing;
        }

        // System.out.println ("hug: facing=" + facing + ", right=" + rotateRight);

        Direction dir = rotate(facing, !rotateRight);
        for (int i = 8; i-- > 0;) {
            // System.out.println("  step: " + dir);

            if (canMove(pos, dir)) {
                hugProf.debug_stop();
                return dir;
            }
            dir = rotate(dir, rotateRight);
        }

        hugProf.debug_stop();
        return Direction.NONE;
    }


    void move(Direction dir) throws GameActionException
    {
        if (rc.getActionDelay() >= 1.0) return;
        prev = rc.getLocation();
        rc.move(dir);
    }

    void sneak(Direction dir) throws GameActionException
    {
        if (rc.getActionDelay() >= 1.0) return;
        prev = rc.getLocation();
        rc.sneak(dir);
    }

    void debug_dump()
    {
        prof.debug_dump("lookAheadPathing");
        lookAheadProf.debug_dump("lookAheadPathing.lookahead");
        hugProf.debug_dump("lookAheadPathing.hug");
    }

    RobotController rc;

    MapLocation prev = null;
    MapLocation target = null;

    boolean rotateRight;
    boolean wallHugging = false;

    ByteCode.ProfilerDist prof = new ByteCode.ProfilerDist();
    ByteCode.ProfilerDist hugProf = new ByteCode.ProfilerDist();
    ByteCode.ProfilerDist lookAheadProf = new ByteCode.ProfilerDist();
}