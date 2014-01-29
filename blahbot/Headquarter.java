package blahbot;

import battlecode.common.*;

public class Headquarter
{

    static boolean spawn() throws GameActionException
    {
        if (rc.senseRobotCount() == GameConstants.MAX_ROBOTS) return false;

        Direction spawnDir = Utils.myHq.directionTo(Utils.hisHq);

        for (int i = spawnDir.ordinal(); i < 8; i++) {
            Direction dir = Utils.dirs[i % 8];
            if (!rc.canMove(dir)) continue;
            rc.spawn(dir);
            return true;
        }

        return false;
    }

    static boolean shoot() throws GameActionException
    {
        final int SplashRd = 2;
        final int AttackRd = RobotType.HQ.attackRadiusMaxSquared;

        Robot objs[] = rc.senseNearbyGameObjects(Robot.class, AttackRd, Utils.him);

        Robot bestTarget = null;
        double bestScore = 0;

        for (int i = objs.length; i-- > 0; ) {
            double score = RobotType.HQ.attackPower;

            Robot dmg[] = rc.senseNearbyGameObjects(Robot.class, SplashRd, Utils.him);
            score += dmg.length * RobotType.HQ.splashPower;

            Robot ff[] = rc.senseNearbyGameObjects(Robot.class, SplashRd, Utils.me);
            score -= ff.length * RobotType.HQ.splashPower;

            if (score <= bestScore) continue;

            bestTarget = objs[i];
            bestScore = score;
        }

        if (bestTarget == null) return false;

        rc.attackSquare(rc.senseLocationOf(bestTarget));
        return true;
    }

    public static void run(RobotController rc) throws GameActionException
    {
        Headquarter.rc = rc;
        Headquarter.comm = new Comm(rc);
        Headquarter.general = new General(rc, comm);
        Headquarter.pq = new ProgressQuest(rc);
        Cowdar.init(rc);

        // Respect the proper styling requirements.
        rc.wearHat();

        while (true) {
            ByteCode.Check bcCheck = new ByteCode.Check(rc);

            if (rc.isActive() && !shoot()) spawn();
            general.command();

            bcCheck.debug_check("Headquarter.end");
            rc.yield();
        }
    }

    static RobotController rc;
    static Comm comm;
    static General general;
    static ProgressQuest pq;

}