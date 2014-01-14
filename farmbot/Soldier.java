package farmbot;

import battlecode.common.*;


public class Soldier
{

    public enum State { NONE, FARMING, MOVING, FIGHTING };

    public static void run(RobotController rc)
        throws GameActionException
    {
        MapLocation myLoc = rc.getLocation();
        Direction lastDir;
        MapLocation goal;
        State myState = State.MOVING;


        while (true) {
            if (!rc.isActive()) { rc.yield(); continue; }
            ByteCode.Check bcCheck = new ByteCode.Check(rc);
            myLoc = rc.getLocation();

            Robot[] enemies = Utils.nearbyEnemies(rc);
            // if (enemies.length) {
            //     //TODO
            // }


            if (myState == State.MOVING) {
                int goalCoord = rc.readBroadcast(Headquarter.PASTRCHAN);
                int y = goalCoord / 1000;

                goal = new MapLocation(goalCoord - (y*1000), y);
                int distSq = myLoc.distanceSquaredTo(goal);

                rc.setIndicatorString(0, "Goalcoord = (" + goal.x + ", " + goal.y + "), distSq = " + distSq);

                if (rc.sensePastrLocations(rc.getTeam()).length < 1) {
                    if (distSq < 1){
                        rc.setIndicatorString(1, "Got milk?");
                        rc.construct(RobotType.PASTR);
                    } else if (distSq < 25) {
                        rc.setIndicatorString(1, "Shhhh.....");
                        Direction nextDir = move(rc, myLoc, goal);
                        if (nextDir != Direction.NONE){
                            rc.sneak(nextDir);
                        }
                    } else {
                        rc.setIndicatorString(1, "Find bovine.");
                        Direction nextDir = move(rc, myLoc, goal);
                        if (nextDir != Direction.NONE){
                            rc.move(nextDir);
                        }
                    }
                } else {
                    if (distSq < 16){
                        rc.setIndicatorString(1, "Everybody's talkin' at me...");
                        farm();
                    } else if (distSq < 25) {
                        rc.setIndicatorString(1, "Ya esta!");
                        Direction nextDir = move(rc, myLoc, goal);
                        if (nextDir != Direction.NONE){
                            rc.sneak(nextDir);
                        }
                    } else {
                        rc.setIndicatorString(1, "Donde esta la mancha?");
                        Direction nextDir = move(rc, myLoc, goal);
                        if (nextDir != Direction.NONE){
                            rc.move(nextDir);
                        }
                    }
                }
            }

            bcCheck.debug_check("Soldier.end");
            rc.yield();
        }
    }
    public static void farm(){

    }

    public static Direction move(RobotController rc, MapLocation myLoc, MapLocation dest) 
    {
        // TODO: improve this
        Direction dir = myLoc.directionTo(dest);

        if (dir == Direction.OMNI)
            return Direction.NONE;

        // Bug
        for (int i=8; i-- > 0; ){
            if (rc.canMove(dir))
                return dir;
            dir = dir.rotateRight();
        }
        return Direction.NONE;
    }
}
