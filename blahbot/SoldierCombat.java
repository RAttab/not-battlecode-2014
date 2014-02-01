package blahbot;

import battlecode.common.*;

class SoldierCombat
{
    public enum CombatState {YES, NO, JUST_HQ};

    SoldierCombat(RobotController rc, Comm comm)
    {
        this.rc = rc;
        this.comm = comm;
    }

    CombatState isCombat() throws GameActionException
    {
        visibleEnemies = rc.senseNearbyGameObjects(
                Robot.class, RobotType.SOLDIER.sensorRadiusSquared, Utils.him);

        if (visibleEnemies.length == 0) {
            return CombatState.NO;
        } else if (visibleEnemies.length == 1) {
            if (rc.senseRobotInfo(visibleEnemies[0]).type == RobotType.HQ)
                return CombatState.JUST_HQ;
            return CombatState.YES;
        }
        return CombatState.YES;
    }


    void suicide(Robot[] suicideEnemies) throws GameActionException
    {
        final MapLocation pos = rc.getLocation();
        final double attackPw = RobotType.SOLDIER.attackPower;

        double health = rc.getHealth();
        double dmg = GameConstants.SELF_DESTRUCT_BASE_DAMAGE +
            health * GameConstants.SELF_DESTRUCT_DAMAGE_FACTOR;

        int killed = 0;
        double totalDmg = -health;
        RobotInfo target = null;

        int enemies = 0;
        int centerX = 0;
        int centerY = 0;

        // Survey the potential carnage.
        for (int i = suicideEnemies.length; i-- > 0;) {
            RobotInfo info = rc.senseRobotInfo(suicideEnemies[i]);
            if (info.type == RobotType.HQ) continue;

            totalDmg += Math.min(dmg, info.health);
            killed += dmg >= info.health ? 1 : 0;

            enemies++;
            centerX += info.location.x;
            centerY += info.location.y;

            if (target == null) {
                target = info;
                continue;
            }
            if (info.health < target.health) target = info;
        }

        // Will this hurt me more then it'll hurt you?
        if (totalDmg > attackPw) {
            Robot[] allies = rc.senseNearbyGameObjects(
                    Robot.class, Utils.SelfDestructRangeSq, Utils.me);

            for (int i = allies.length; i-- > 0;) {
                RobotInfo info = rc.senseRobotInfo(allies[i]);
                if (info.type == RobotType.HQ) continue;

                totalDmg -= Math.min(dmg, info.health);
                killed -= dmg >= info.health ? 1 : 0;
            }
        }

        // Worth it. Boom time.
        if (totalDmg > attackPw) {
            rc.selfDestruct();
            return;
        }

        // Something smells wrong...

        // We can kill him before boom time. Do so.
        if (enemies == 1 && target.health < attackPw) {
            rc.attackSquare(target.location);
            return;
        }

        // Back the fuck off.
        MapLocation center =
            new MapLocation(centerX / enemies, centerY / enemies);
        if (move(center.directionTo(pos))) return;

        // Well... crap... Let's go out shooting!
        rc.attackSquare(target.location);
    }


    void attack(Robot[] reachableEnemies) throws GameActionException
    {
        final MapLocation pos = rc.getLocation();
        final double attackPw = RobotType.SOLDIER.attackPower;
        final int attackRd = RobotType.SOLDIER.attackRadiusMaxSquared;

        int centerX = 0, centerY = 0;
        double hisHealth = 0.0;

        // Look for a target to shoot and gather some stats about the enemy.

        RobotInfo target = null;
        double targetShots = RobotType.SOLDIER.maxHealth + 1;

        RobotInfo nearest = null;
        int nearestDist = 0;

        boolean hasHq = false;

        for (int i = reachableEnemies.length; i-- > 0;) {
            RobotInfo info = rc.senseRobotInfo(reachableEnemies[i]);
            if (info.type == RobotType.HQ) {
                hasHq = true;
                continue;
            }

            centerX += info.location.x;
            centerY += info.location.y;

            if (info.type == RobotType.SOLDIER && !info.isConstructing)
                hisHealth += info.health;

            int dist = pos.distanceSquaredTo(info.location);

            if (target == null) {
                target = nearest = info;
                targetShots = info.health / attackPw;
                nearestDist = dist;
                continue;
            }

            if (dist < nearestDist) {
                nearest = info;
                nearestDist = dist;
            }

            // Prefer non-constructing soldier robots.
            if (info.isConstructing && !target.isConstructing) continue;
            if (info.type != RobotType.SOLDIER && target.type == RobotType.SOLDIER)
                continue;

            // Prefer things that we can kill faster.
            double shots = info.health / attackPw;
            if (shots > targetShots) continue;
            if (shots < targetShots) {
                target = info;
                targetShots = shots;
                continue;
            }

            // Final tie breaker is which robots is next to attacks.
            if (info.actionDelay >= target.actionDelay) continue;

            target = info;
            targetShots = shots;
        }

        MapLocation center = new MapLocation(
                centerX / (reachableEnemies.length - (hasHq ? 1 : 0)),
                centerY / (reachableEnemies.length - (hasHq ? 1 : 0)));
        rc.setIndicatorString(1, "combat.attack.spot: " + center.toString());
        comm.spot(center);


        // Gather some stats about our allies.

        Robot[] allies = rc.senseNearbyGameObjects(
                Robot.class, attackRd, Utils.me);

        double health = rc.getHealth();
        double myHealth = health;
        double minHealth = RobotType.SOLDIER.maxHealth + 1;

        for (int i = allies.length; i-- > 0;) {
            RobotInfo info = rc.senseRobotInfo(allies[i]);
            if (info.type == RobotType.HQ) continue;

            myHealth += info.health;
            minHealth = Math.min(minHealth, info.health);
        }


        // Ruh Roh!
        if (hisHealth > myHealth + attackPw) {

            // KAMIKAZE!
            if (health < minHealth && pos != center) {

                // Move towards the center mass of enemies. Maximize the damage.
                // Why do I feel like a terrorist...?
                if (Utils.canMoveTo(rc, center, attackRd)) {
                    if (move(pos.directionTo(center))) return;
                }

                // Can't move to our target. No virgins for the lazy...
            }

            // Time to leave methink.
            if (move(center.directionTo(pos))) return;
        }

        // Oh shit. We have guns? Pew pew!

        if (target != null) rc.attackSquare(target.location);
    }


    // \todo Don't step into attack range and give them first shot.
    Robot[] visibleEnemies;
    void visible() throws GameActionException
    {
        final MapLocation pos = rc.getLocation();
        final int attackRange = RobotType.SOLDIER.attackRadiusMaxSquared;

        int enemies = 0;
        RobotInfo base = null;
        int centerX = 0, centerY = 0;

        int nearestDist = 100;
        MapLocation nearest = null;

        for (int i = visibleEnemies.length; i-- > 0; ) {
            RobotInfo info = rc.senseRobotInfo(visibleEnemies[i]);

            if (info.type != RobotType.SOLDIER) {
                if (base == null) base = info;
                else if (base.type == RobotType.NOISETOWER) base = info;
                continue;
            }

            int dist = pos.distanceSquaredTo(info.location);
            if (dist < nearestDist) {
                nearest = info.location;
                nearestDist = dist;
            }

            enemies++;
            centerX += info.location.x;
            centerY += info.location.y;
        }

        // Defense-less towers? How nice of you!
        if (enemies == 0) {
            if (!move(pos.directionTo(base.location)))
                move(base.location.directionTo(pos));
            return;
        }

        MapLocation center = new MapLocation(
                centerX / enemies, centerY / enemies);
        comm.spot(center);

        Robot[] allies  = rc.senseNearbyGameObjects(
                Robot.class, RobotType.SOLDIER.sensorRadiusSquared, Utils.me);

        // Outnumbered and outguned. Running away sounds like a good idea.
        if (enemies > allies.length) {
            move(center.directionTo(pos));
            return;
        }

        Direction dir = pos.directionTo(center);
        boolean inRange = pos.add(dir).distanceSquaredTo(nearest) <= attackRange;

        // Don't give em the first shot
        if (!inRange || enemies <= allies.length * 2) move(dir);
    }


    void exterminate() throws GameActionException
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
                Robot.class, RobotType.SOLDIER.attackRadiusMaxSquared, Utils.him);
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


    boolean move(Direction desired) throws GameActionException
    {
        if (rc.canMove(desired)) {
            rc.move(desired);
            return true;
        };

        Direction right = desired.rotateRight();
        if (rc.canMove(right)) {
            rc.move(right);
            return true;
        }

        Direction left = desired.rotateLeft();
        if (rc.canMove(left)) {
            rc.move(left);
            return true;
        }

        return false;
    }

    void debug_dump()
    {
        suicideProf.debug_dump("micro.suicide");
        attackProf.debug_dump("micro.attack");
        visibleProf.debug_dump("micro.visible");
    }


    RobotController rc;
    Comm comm;

    ByteCode.ProfilerDist suicideProf = new ByteCode.ProfilerDist();
    ByteCode.ProfilerDist attackProf = new ByteCode.ProfilerDist();
    ByteCode.ProfilerDist visibleProf = new ByteCode.ProfilerDist();
}