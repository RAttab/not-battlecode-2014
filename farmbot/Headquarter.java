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
        Direction enemyHQDir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
        Cowdar.init(rc);

        while (true) {
            ByteCode.Check bcCheck = new ByteCode.Check(rc);

            if (rc.isActive()) spawn(rc);
            else shoot(rc);

            // if (Clock.getBytecodeNum() < 9000) {
            //     Cowdar.search(9000 - Clock.getBytecodeNum());
            // }

            if (Clock.getRoundNum() > 2) {
                rc.broadcast(PASTRCHAN, 1000*Cowdar.bestSpot.loc.y + Cowdar.bestSpot.loc.x);
            }

            bcCheck.debug_check("Headquarter.end");
            rc.yield();
        }
    }

}
