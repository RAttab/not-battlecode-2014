package blahbot;

import battlecode.common.*;

public class Headquarter
{

    private static void spawn(RobotController rc)
    {
        Direction spawnDir = Utils.myHq.directionTo(Utils.hisHq);

        for (int i = spawnDir.ordinal(); i < 8; i = (i + 1) % 8) {
            Direction dir = Utils.dirs[i];
            if (!rc.canMove(dir)) continue;
            rc.spawn(dir);
        }
    }

    private static void shoot(RobotController rc)
    {
        static final int SplashRd = 2;
        static final int AttackRd = RobotType.HQ.attackRadiusMaxSquared;

        Robot objs[] = senseNearbyGameObjects(Robot.class, AttackRd, Utils.him);

        Robot bestTarget = null;
        int bestScore = 50;

        for (int i = 0; i < objs.length(); ++i) {
            int score = RobotType.HQ.attackPower;

            Robot dmg[] = senseNearbyGameObjects(Robot.class, SplashRd, Utils.him);
            score += dmg.length() * RobotType.HQ.splashPower;

            Robot ff[] = senseNearbyGameObjects(Robot.class, SplashRd, Utils.me);
            score -= ff.length() * RobotType.HQ.splashPower;

            if (score < bestScore) continue;

            bestTarget = objs[i];
            bestScore = score;
        }

        if (bestTarget == null) return;

        rc.attackSquare(rc.senseLocationOf(objs[i]));
    }

    public static void run(RobotController rc) throws GameActionException
    {
        while (true) {
            ByteCode.Check bcCheck = new ByteCode.Check(rc);

            if (rc.isActive()) spawn();
            else shoot();

            bcCheck.debug_check("Headquarter.end");
            rc.yield();
        }
    }

}