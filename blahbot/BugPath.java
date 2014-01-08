package blahbot;

import battlecode.common.*;

public class BugPath
{

    BugPath(RobotController rc, MapLocation dest)
    {
        this.rc = rc;
        target = dest;
    }

    /* \todo Always turning to the right isn't that great of an idea but we can
             improve later.

       \todo Avoid going around in circles.
    */
    Direction direction()
    {
        if (unreachable) rc.breakpoint();

        MapLocation pos = rc.getLocation();
        Direction targetDir = pos.directionTo(target);

        int ord = targetDir.ordinal();

        System.out.println("moving(" + target.toString() + ")"
                + ": pos=" + pos.toString()
                + ", dir=" + targetDir.toString()
                + ", ord=" + ord
                + ", backtrack=" + backtrackOrd);

        for (int i = 8; --i > 0; ord = (ord + 1) % 8) {

            String dbg = "- " + i + "-" + ord + ": ";

            if (ord == backtrackOrd) {
                System.out.println(dbg + "backtrack");
                continue;
            }

            Direction dir = Utils.dirs[ord];
            if (!rc.canMove(dir)) {
                System.out.println(dbg + "wall");
                continue;
            }

            System.out.println(dbg + "move");
            backtrackOrd = dir.opposite().ordinal();
            return dir;
        }

        System.out.println("=> stuck");
        return Direction.NONE;
    }

    void move() throws GameActionException
    {
        move(direction());
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

    void sneak() throws GameActionException
    {
        sneak(direction());
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

    int start;
    MapLocation target;

    int backtrackOrd = Direction.NONE.ordinal();
    boolean unreachable = false;

    RobotController rc;
}