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
                goal = new MapLocation(goalCoord - y, y);

                if (Utils.distTwoPoints(goal, myLoc) < 2){
                    rc.construct(RobotType.PASTR);
                } else {
                    Direction nextDir = move(rc, myLoc, goal);
                    if (nextDir != Direction.NONE)
                        rc.move(nextDir);
                }
            }

            bcCheck.debug_check("Soldier.end");
            rc.yield();
        }
    }


    public static Direction move(RobotController rc, MapLocation myLoc, MapLocation dest) 
    {
        // TODO: improve this
        Direction dir = myLoc.directionTo(dest);
        // Bug
        for (int i=8; --i > 0; ){
            if (rc.canMove(dir))
                return dir;
            dir = dir.rotateRight();
        }
        return Direction.NONE;
    }
}
