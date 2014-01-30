package blahbot;

import battlecode.common.*;

public class Tower
{
    static int attackRange =
        (int) Math.sqrt(RobotType.NOISETOWER.attackRadiusMaxSquared);
    static int senseRange =
        (int) Math.sqrt(RobotType.NOISETOWER.sensorRadiusSquared);


    static MapLocation pastr() throws GameActionException
    {
        MapLocation[] pastrs = rc.sensePastrLocations(Utils.me);
        if (pastrs.length == 0) return null;

        int minDist = 50;
        MapLocation pastr = null;
        MapLocation pos = rc.getLocation();

        for (int i = pastrs.length; i-- > 0;) {
            int dist = pos.distanceSquaredTo(pastrs[i]);
            if (dist > minDist) continue;

            minDist = dist;
            pastr = pastrs[i];;
        }

        return pastr;
    }


    static boolean inPastr(MapLocation pos, MapLocation pastr)
        throws GameActionException
    {
        return pos.distanceSquaredTo(pastr) < GameConstants.PASTR_RANGE;
    }


    static int[] bestSectors = { -1, -1, -1, -1, -1, -1, -1, -1 };
    static void analyze(MapLocation pastr) throws GameActionException
    {
        ByteCode.Profiler prof = new ByteCode.Profiler();

        double[][] growth = rc.senseCowGrowth();
        double[] scores = new double[8];

        int maxDist = attackRange - 1;

        int xMax = Math.min(pos.x + maxDist, rc.getMapWidth());
        int xMin = Math.max(pos.x - maxDist, 0);

        int yMax = Math.min(pos.y + maxDist, rc.getMapWidth());
        int yMin = Math.max(pos.y - attackRange, 0);

        // calculate a score for each sectors.
        for (int i = xMax; i-- > xMin;) {
            for (int j = yMax; j-- > yMin;) {
                MapLocation center = new MapLocation(i, j);
                if (!rc.canAttackSquare(center)) continue;
                if (inPastr(center, pastr)) continue;

                Direction dir = pastr.directionTo(center);
                scores[dir.ordinal()] += growth[i][j];
            }
        }

        // Find the 3 best sectors 
        for (int i = scores.length; i-- > 0;) {
            int dir = i;

            for (int j = bestSectors.length; j-- > 0;) {
                if (bestSectors[j] < 0) {
                    bestSectors[j] = dir;
                    continue;
                }

                if (scores[bestSectors[j]] >= scores[dir]) continue;

                int tmp = bestSectors[j];
                bestSectors[j] = dir;
                dir = tmp;
            }
        }

        String dbg = "";
        for (int j = bestSectors.length; j-- > 0;)
            dbg += "<" + j + ", " + Utils.dirs[bestSectors[j]] + ", " + scores[bestSectors[j]] + "> ";
        System.out.println("BestDirs: [ " + dbg + " ]");

        prof.debug_dump("tower.analyse");
    }


    static ByteCode.ProfilerDist nearbyProf = new ByteCode.ProfilerDist();
    static MapLocation findNearbyTarget(MapLocation pastr)
        throws GameActionException
    {
        nearbyProf.debug_start();

        final int dist = senseRange - 1;

        double bestScore = 500;
        MapLocation bestTarget = null;

        for (int i = 8; i-- > 0;) {
            MapLocation center = pos.add(Utils.dirs[i], dist);
            if (!rc.canSenseSquare(center)) continue;
            if (inPastr(center, pastr)) continue;
            double score = rc.senseCowsAtLocation(center);

            int dir = pos.directionTo(center).ordinal() + 1;
            for (int j = 4; j-- > 0; dir += 2) {
                MapLocation side = center.add(Utils.dirs[j % 8], 1);
                if (!rc.canSenseSquare(side)) continue;
                score += rc.senseCowsAtLocation(side);
            }

            System.out.println("  center: " + center + " -> " + score);

            if (bestScore >= score) continue;
            bestScore = score;
            bestTarget = center;
        }

        nearbyProf.debug_stop();
        
        System.out.println("nearby: " + bestTarget);
        return bestTarget;
    }

    static int currentSector = 0;
    static int currentSectorStep = -1;
    static MapLocation findSectorTarget(MapLocation pastr)
        throws GameActionException
    {
        final int stepCount = 5;
        if (++currentSectorStep == stepCount) {
            currentSector = (currentSector + 1) % 8;
            currentSectorStep = 0;
        }

        int minRange = GameConstants.PASTR_RANGE + 4;
        int maxRange = attackRange;
        int step = (maxRange - minRange) / stepCount;
        int dist = (stepCount - currentSectorStep) * step;

        MapLocation target = pos.add(Utils.dirs[currentSector], dist);
        System.out.println("sector: step=" + step + "curStep=" + currentSectorStep 
                + " -> " + target);
        return inPastr(target, pastr) ? null : target;
    }


    static void shoot(MapLocation pastr) throws GameActionException
    {
        final int attackRd = RobotType.NOISETOWER.attackRadiusMaxSquared;
        final int heavySplash = GameConstants.NOISE_SCARE_RANGE_LARGE;
        final int lightSplash = GameConstants.NOISE_SCARE_RANGE_SMALL;

        System.out.println("");
        System.out.println("====================== SCAN ======================");
        System.out.println("pastr: " + pastr);

        // MapLocation target = findNearbyTarget(pastr);
        MapLocation target = null;
        while (target == null) target = findSectorTarget(pastr);

        Direction dir = pastr.directionTo(target);
        int dist = (int) Math.sqrt(pastr.distanceSquaredTo(target));

        boolean heavy = dist > (attackRange / 2);
        int splash = (int) Math.sqrt(heavy ? heavySplash : lightSplash);

        System.out.println("target: target=" + target 
                + ", dir=" + dir + ", dist=" + dist 
                + ", heavy=" + heavy + ", splash=" + splash);

        target = target.add(dir, splash / 2);

        System.out.println("nudged: dist=" + dist + ", target=" + target
                + ", canAttack=" + rc.canAttackSquare(target));

        // For loop is to bound the check.
        for (int i = 10; !rc.canAttackSquare(target) && i-- > 0; )
            target = target.add(dir, -1);

        System.out.println("adj: target=" + target);

        if (!rc.canAttackSquare(target)) {
            rc.breakpoint();
            return;
        }

        if (heavy) rc.attackSquare(target);
        else rc.attackSquareLight(target);

        System.out.println("=================================================");
        System.out.println("");
    }

    static void spot() throws GameActionException
    {
        Robot[] enemies = rc.senseNearbyGameObjects(
                Robot.class, RobotType.PASTR.sensorRadiusSquared, Utils.him);
        if (enemies.length == 0) return;
        comm.spot(rc.getLocation());
    }

    static void debug_dump()
    {
        if (Clock.getRoundNum() % 100 > 0) return;
        nearbyProf.debug_dump("tower.nearbyProf");
    }

    public static void run(RobotController rc) throws GameActionException
    {
        Tower.rc = rc;
        Tower.comm = new Comm(rc);
        Tower.pos = rc.getLocation();

        analyze(pastr());
        rc.yield();

        while (true) {
            spot();

            MapLocation pastr = pastr();

            // No use keeping this tower alive so reclaim our supply.
            if (pastr == null) {
                System.out.println("Game over man! Game over!");
                rc.selfDestruct();
                return;
            }

            if (rc.isActive()) shoot(pastr);

            debug_dump();
            rc.yield();
        }
    }

    static RobotController rc;
    static Comm comm;
    static MapLocation pos;
}
