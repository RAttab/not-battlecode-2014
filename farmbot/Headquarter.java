package farmbot;

import battlecode.common.*;

public class Headquarter
{
    public static final int PASTRCHAN = 1;

    private static void spawn(RobotController rc) throws GameActionException
    {
        Direction spawnDir = Utils.myHq.directionTo(Utils.hisHq);

        for (int i = spawnDir.ordinal(); i < 8; i++) {
            Direction dir = Utils.dirs[i % 8];
            if (!rc.canMove(dir)) continue;
            rc.spawn(dir);
            break;
        }
    }

    private static void shoot(RobotController rc) throws GameActionException
    {
        final int SplashRd = 2;
        final int AttackRd = RobotType.HQ.attackRadiusMaxSquared;

        Robot objs[] = rc.senseNearbyGameObjects(Robot.class, AttackRd, Utils.him);

        Robot bestTarget = null;
        double bestScore = 50;

        for (int i = 0; i < objs.length; ++i) {
            double score = RobotType.HQ.attackPower;

            Robot dmg[] = rc.senseNearbyGameObjects(Robot.class, SplashRd, Utils.him);
            score += dmg.length * RobotType.HQ.splashPower;

            Robot ff[] = rc.senseNearbyGameObjects(Robot.class, SplashRd, Utils.me);
            score -= ff.length * RobotType.HQ.splashPower;

            if (score < bestScore) continue;

            bestTarget = objs[i];
            bestScore = score;
        }

        if (bestTarget == null) return;

        rc.attackSquare(rc.senseLocationOf(bestTarget));
    }

    public static void run(RobotController rc) throws GameActionException
    {
        Direction enemyHQDir = rc.getLocation.directionTo(rc.senseEnemyHQLocation());
        MapLocation pastrLoc = null;

        while (true) {
            ByteCode.Check bcCheck = new ByteCode.Check(rc);

            if (rc.isActive()) spawn(rc);
            else shoot(rc);

            if (pastrLoc == null) {
                pastrLoc = getPastrLoc(rc, enemyHQDir);
                rc.broadcast(PASTRCHAN, 1000*pastrLoc.y + pastrLoc.x);
            }

            bcCheck.debug_check("Headquarter.end");
            rc.yield();
        }
    }

    public static MapLocation getPastrLoc(RobotController rc, Direction enemyHQDir) {
        // TODO
        // find the best location for the next PASTR
        MapLocation candidate = rc.getLocation().add(enemyHQDir.opposite(), 5);
        while (rc.senseTerrainTile(candidate) != NORMAL && rc.senseTerrainTile(candidate) != ROAD) {
            if (candidate.x < 0)
                candidate.x = 0;
            if (candidate.y < 0)
                candidate.y = 0;

            if (candidate.x <= getMapWidth())
                candidate.x = getMapWidth() - 1;
            if (candidate.y <= getMapHeight())
                candidate.y = getMapHeight() - 1;

            candidate.add(Utils.randomDir(), Utils.rand.nextInt[3] + 1);
        }
        return candidate;
    }

}