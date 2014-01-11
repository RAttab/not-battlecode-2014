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


    void suicide(Robot[] suicideEnemies) throws GameActionException
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
            return;
        }

        // Something smells wrong... Back the fuck off.
        // \todo Investigate a smarter way to back the fuck off.
        Direction dir = pos.directionTo(target.location);
        move(dir.opposite());
    }


    void attack(Robot[] reachableEnemies) throws GameActionException
    {
        final MapLocation pos = rc.getLocation();
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


        // KAMIKAZE!

        if (hisHealth > myHealth + attackPw && health <= minHealth) {
            MapLocation center = new MapLocation(centerX, centerY);

            // Well fuck. Our heuristic let us down...
            if (pos == center) {
                move(pos.directionTo(nearest.location));
                return;
            }

            // Move towards the center mass of enemies. Maximize the damage.
            // Why do I feel like a terrorist...?
            else if (canMoveTo(center, attackRd)) {
                move(pos.directionTo(center));
                return;
            }

            // Can't move to our target. No virgins for the lazy...
        }


        // Oh shit. We have guns? Pew pew!

        rc.attackSquare(target.location);
    }


    // \todo Don't step into attack range and give them first shot.
    Robot[] visibleEnemies;
    void visible() throws GameActionException
    {
        final MapLocation pos = rc.getLocation();

        int enemies = 0;
        RobotInfo base = null;
        int centerX = 0, centerY = 0;

        for (int i = visibleEnemies.length; i-- > 0; ) {
            RobotInfo info = rc.senseRobotInfo(visibleEnemies[i]);
            if (info.type == RobotType.HQ) continue;

            if (target == null) target = info;


            if (info.type != RobotType.SOLDIER) {
                if (base.type == RobotType.NOISETOWER) base = info;
                continue;
            }

            enemies++;
            centerX += info.location.x;
            centerY += info.location.y;
        }

        // Defense-less towers? How nice of you!
        if (enemies == 0) {
            move(pos.directionTo(base.location));
            return;
        }

        centerX /= enemies;
        centerY /= enemies;
        MapLocation center = new MapLocation(centerX, centerY);

        Robot[] allies  = rc.senseNearbyGameObjects(
                Robot.class, RobotType.SOLDIER.sensorRadiusSquared, Utils.me);

        if (enemies <= allies + 1) move(pos.directionTo(center));
        else move(center.directionTo(pos));
    }


    public void exterminate() throws GameActionException
    {
        Robot[] suicideEnemies = rc.senseNearbyGameObjects(
                Robot.class, Utils.SelfDestructRangeSq, Utils.him);
        if (suicideEnemies.length > 0) {
            suicideProf.debug_start();
            suicide(suicideEnemies);
            suicideProf.debug_stop();
            return;
        }

        Robot[] reachableEnemies = rc.senseNearbyGameObjects(
                Robot.class, RobotType.Soldier.attackRadiusMaxSquared, Utils.him);
        if (reachableEnemies.length > 0) {
            attackProf.debug_start();
            attack(reachableEnemies);
            attackProf.debug_stop();
            return;
        }

        visibleProf.debug_start();
        visible();
        visibleProf.debug_stop();
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

    void move(Direction dir)
    {
        final MapLocation pos = rc.getLocation();

        boolean mySide =
            Utils.myHq.distanceSquaredTo(pos) <=
            Utils.hisHq.distanceSquaredTo(pos);

        if (mySide)
            rc.sneak(dir);
        else rc.move(dir);
    }

    void debug_dump()
    {
        suicideProf.debug_dump("micro.suicide");
        attacckProf.debug_dump("micro.attack");
        visibleProf.debug_dump("micro.visible");
    }


    RobotController rc;

    ByteCode.ProfilerDist suicideProf = new ByteCode.ProfilerDist();
    ByteCode.ProfilerDist attackProf = new ByteCode.ProfilerDist();
    ByteCode.ProfilerDist visibleProf = new ByteCode.ProfilerDist();
}