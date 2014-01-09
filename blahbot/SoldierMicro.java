package blahbot;

import battlecode.common.*;

public class SoldierMicro
{

    SoldierMicro(RobotController rc)
    {
        this.rc = rc;
    }

    boolean isMicro()
    {
        nearbyBad = rc.senseNearbyGameObjects(
                Robot.class, RobotType.Soldier.sensorRadiusSquared, Utils.him);

        if (nearbyBad.length == 0) return false;

        nearbyGood = rc.senseNearbyGameObjects(
                Robot.class, RobotType.Soldier.sensorRadiusSquared, Utils.me);
    }

    // \todo Need to split up the loop for inSenseRange and inAttackRange.
    void exterminate()
    {
        final MapLocation pos = rc.getLocation();
        final int attackRd = RobotType.Soldier.attackRadiusMaxSquared;
        final int attackPw = RobotType.Soldier.attackPower;

        int centerX = 0, centerY = 0;
        double hisHealth = 0.0;
        double destructDmg = 0.0;

        RobotInfo target = null;
        int targetReach = 100;
        boolean targetShots = 100;

        for (int i = nearbyBad.length; i-- > 0;) {
            RobotInfo info = rc.senseRobotInfo(nearbyBad[i]);
            if (info.type == RobotType.HQ) continue;

            // If there's nothing in attack range, prefer the closest bot.
            int dist = pos.distanceSquaredTo(info.location);
            int distToReach = Math.max(dist - attackRd, 0);
            if (distToReach <= targetReach) continue;
            if (distToReach != targetReach) {
                target = info;
                targetReach = distToReach;
                targetShots = info.health / attackPw;
                continue;
            }

            // target != null && targetReach = 0.

            centerX += info.location.x;
            centerY += info.location.y;
            hisHealth += info.health;
            if (dist < 2) destructDmg += info.health;

            // Prefer non-constructing robots.
            if (info.isConstructing && !target.isContructing) continue;
            if (info.type != RobotType.Soldier && target.type == RobotType.Soldier)
                continue;

            // Prefer things that we can kill faster.
            int shots = info.health / attackPw;
            if (shots > targetShots) continue;
            if (shots < targetShots) {
                target = info;
                targetShots = shots;
                continue;
            }

            // Final tie breaker is which robots is next to attacks.
            if (info.attackDelay >= target.attackDelay) continue;

            target = info;
            targetEasyKill = easyKill;
        }

        centerX /= nearbyBad.length;
        centerY /= nearbyBad.length;

        double myHealth = rc.getHealth();
        for (int i = nearbyGood.length; i-- > 0;) {
            RobotInfo info = rc.senseRobotInfo(nearbyBad[i]);
            if (info != RobotType.Soldier) continue;

            
        }
    }


    boolean canMoveTo(MapLocation dest, int max)
    {
        if (Math.sqrt(src.distanceSquaredTo(dest)) > max)
            return false;

        MapLocation it = rc.getLocation();

        for (int i = 0; i < max; ++i) {
            it = it.add(src.directionTo(dest));
            if (it.equals(dest)) return true;

            if (!rc.senseTerrainTile(it).isTraversableAtHeight(RobotLevel.ON_GROUND))
                return false;

            if (rc.senseObjectAtLocation(it) != null)
                return false;
        }

        return false;
    }


    Robot[] nearbyBad;
    Robot[] nearbyGood;

    RobotController rc;
}