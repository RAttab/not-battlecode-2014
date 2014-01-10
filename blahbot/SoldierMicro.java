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
        visibleEnemies = rc.senseNearbyGameObjects(
                Robot.class, RobotType.Soldier.sensorRadiusSquared, Utils.him);

        if (visibleEnemies.length == 0) return false;
    }

    Robot[] suicideEnemies;
    boolean suicide() throws GameActionException
    {
        final MapLocation pos = rc.getLocation();
        final int attackPw = RobotType.Soldier.attackPower;

        double health = rc.getHealth();
        double dmg = GameConstants.SELF_DESTRUCT_BASE_DAMAGE +
            health * GameConstants.SELF_DESTRUCT_DAMAGE_FACTOR;

        int killed = 0;
        double totalDmg = -health;
        RobotInfo target = null;

        // Survey the potential carnage.
        for (int i = suicideEnemies.length; i-- > 0;) {
            RobotInfo info = target = rc.senseRobotInfo(nearbyBad[i]);
            if (info.type == RobotType.HQ) continue;

            totalDmg += Math.min(dmg, info.health);
            killed += dmg >= info.health;
        }

        // Will this hurt me more then it'll hurt you?
        if (totalDmg > attackPw) {
            Robot[] allies = rc.senseNearbyGameObjects(
                    Robot.class, Utils.SelfDestructRangeSq, Utils.me);

            for (int i = suicide.length; i-- > 0;) {
                RobotInfo info = rc.senseRobotInfo(nearbyBad[i]);
                if (info.type == RobotType.HQ) continue;

                totalDmg -= Math.min(dmg, info.health);
                killed -= dmg >= info.health;
            }
        }

        // Worth it. Boom time.
        if (totalDmg > attackPw) {
            rc.selfDestruct();
            return true;
        }

        // Something smells wrong... Back the fuck off.
        // \todo Investigate a smarter way to the fuck off.
        Direction dir = pos.directionTo(target.location);
        rc.move(dir.opposite());
        return true;
    }

    Robot[] reachableEnemies;
    boolean attack() throws GameActionException
    {
        final int attackPw = RobotType.Soldier.attackPower;
        final int attackRd = RobotType.Soldier.attackRadiusMaxSquared;

        int centerX = 0, centerY = 0;
        double hisHealth = 0.0;

        // Look for a target to shoot and gather some stats about the enemy.

        RobotInfo target = null;
        boolean targetShots = RobotType.Soldier.maxHealth + 1;

        RobotInfo nearest = null;
        int nearestDist = 0;

        for (int i = reachableEnemies.length; i-- > 0;) {
            RobotInfo info = rc.senseRobotInfo(nearbyBad[i]);
            if (info.type == RobotType.HQ) continue;

            centerX += info.location.x;
            centerY += info.location.y;
            hisHealth += info.health;

            int dist = pos.distanceSquaredTo(info.location);

            if (target == null) {
                target = nearest = info;
                targetSHots = info.health / attackPw;
                nearestDist = dist;
                continue;
            }

            if (dist < nearestDist) {
                nearest = info;
                nearestDist = dist;
            }

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
            targetShots = shots;
        }

        centerX /= nearbyBad.length;
        centerY /= nearbyBad.length;


        // Gather some stats about our allies.

        Robot[] reachableAllies = rc.senseNearbyGameObjects(
                Robot.class, attackRd, Utils.me);

        doubl health = rc.getHealth();
        double myHealth = health;
        double minHealth = RobotType.Soldier.health + 1;

        for (int i = reachableAllies.length; i-- > 0;) {
            RobotInfo info = rc.senseRobotInfo(nearbyBad[i]);
            if (info.type == RobotType.HQ) continue;

            hisHealth += info.health;
            minHealth = Math.min(minHealth, info.health);
        }


        // KAMIKAZY!

        if (hisHealth > myHealth + attackPw && health <= minHealth) {
            MapLocation center = new MapLocation(centerX, centerY);

            // Well fuck. Our heuristic let us down...
            if (pos == center) {
                rc.move(pos.directionTo(nearest.location));
                return true;
            }

            // Move towards the center mass of enemies. Maximize the damage.
            // Why does it feel like I'm doing terrorism...
            else if (canMoveTo(center, attackRd)) {
                rc.move(pos.directionTo(center));
                return true;
            }

            // Can't move to our target. No virgins for the lazy...
        }


        // Oh shit. We have guns? Pew pew!

        rc.attackSquare(target.location);
        return true;
    }

    Robot[] visibleEnemies;
    boolea visible() throws GameActionException
    {

    }

    boolean exterminate() throws GameActionException
    {
        suicideEnemies = rc.senseNearbyGameObjects(
                Robot.class, Utils.SelfDestructRangeSq, Utils.him);
        if (suicideEnemies.length > 0 && suicide()) return true;

        reachableEnemies = rc.senseNearbyGameObjects(
                Robot.class, RobotType.Soldier.attackRadiusMaxSquared, Utils.him);
        if (reachableEnemies.length > 0 && attack()) return true;

        return visible();
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

    RobotController rc;
}