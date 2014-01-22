package blahbot;

import battlecode.common.*;
import java.util.*;

class CowSpot
{
    public MapLocation loc;
    public double density;

    public CowSpot(RobotController rc, MapLocation loc) 
            throws GameActionException {
        // Note: Do not create a CowSpot closer than 5 tiles from the map edge!

        this.loc = loc;
        Logger.log(10, "CowSpot constructor called with loc: " + loc.x + "," + loc.y);
        if (rc.senseTerrainTile(loc) == TerrainTile.OFF_MAP
                    || rc.senseTerrainTile(loc) == TerrainTile.VOID) {
            // can't build on walls!
            density = -10000;
        } else {
            // how sexy are the cows in this pasture?
            double sexyCows = Cowdar.cowSexSum(loc.x - 5, loc.y - 5, loc.x + 5, loc.y + 5);


            double wallPen = 0.0;
            // we want to stay away from walls if possible. sample for them!
            for (int i=8; i-- > 0;) {
                MapLocation next = new MapLocation(loc.x + Utils.samples_5[i], 
                                                    loc.y + Utils.samples_5[i+10]);
                if (rc.senseTerrainTile(next) == TerrainTile.VOID)
                    wallPen -= 5;
            }

            // we want to build closer to our HQ than the enemy's
            int enemyDistSqr = loc.distanceSquaredTo(Cowdar.enemyHqLoc);
            int homeDistSqr = loc.distanceSquaredTo(Cowdar.hqLoc);
            double coeffHq;

            if (homeDistSqr > enemyDistSqr) {
                coeffHq = (enemyDistSqr - homeDistSqr) * 0.1;

                Logger.log(10, "Density at (" + loc.x + ", " + loc.y + "): ");
                Logger.log(10, "enemyDist = " + enemyDistSqr);
                Logger.log(10, "homeDist = " + homeDistSqr);
                Logger.log(10, "closer to enemy hq than our own.");
                Logger.log(10, "coeffHq = " + coeffHq);
                Logger.log(10, "sexyCows = " + sexyCows);
                Logger.log(10, "wallPen = " + wallPen);
                density = coeffHq + sexyCows + wallPen;
                Logger.log(10, "...density equals [" + density + "]");
            } else {
                double d = (Math.pow(enemyDistSqr - homeDistSqr, 0.6));
                coeffHq = 5 * Cowdar.distBetweenHqs / (1 + d);
                Logger.log(10, "Density at (" + loc.x + ", " + loc.y + "): ");
                Logger.log(10, "coeffHq = " + coeffHq);
                Logger.log(10, "sexyCows = " + sexyCows);
                Logger.log(10, "wallPen = " + wallPen);
                Logger.log(10, "enemyDist = " + enemyDistSqr);
                Logger.log(10, "homeDist = " + homeDistSqr);
                Logger.log(10, "d = " + d);
                density = coeffHq + sexyCows + wallPen;
                Logger.log(10, "...density equals [" + density + "]");
            }
        }
    }
}

public class Cowdar
{

    public static double[][] cowSex;

    public static RobotController rc;

    public static CowSpot bestSpot;

    public static int mapWidth;
    public static int mapHeight;
    public static MapLocation hqLoc;
    public static MapLocation enemyHqLoc;
    public static Direction awayFromEnemyHq;
    public static double distBetweenHqs;

    public static void init(RobotController _rc) throws GameActionException
    {
        rc = _rc;

        cowSex = rc.senseCowGrowth();
        hqLoc = rc.senseHQLocation();
        enemyHqLoc = rc.senseEnemyHQLocation();
        mapHeight = rc.getMapHeight();
        mapWidth = rc.getMapWidth();
        awayFromEnemyHq = hqLoc.directionTo(rc.senseEnemyHQLocation()).opposite();
        distBetweenHqs = Math.sqrt(hqLoc.distanceSquaredTo(enemyHqLoc));

        bestSpot = new CowSpot(rc, getFirstLoc());

    }

    public static double cowSexSum(int xmin, int ymin, int xmax, int ymax) {
        
        if (xmin < 0)
            xmin = 0;
        else if (xmax >= mapWidth)
            xmax = mapWidth - 1;
        
        if (ymin < 0)
            ymin = 0;
        else if (ymax >= mapHeight)
            ymax = mapHeight - 1;

        double sum = 0.0;
        for (;xmin++ < xmax;) {
            for (;ymin++ < ymax;) {
                sum += cowSex[xmin][ymin];
            }
        }
        return sum;
    }

    public static void search(int byteCodeLimit) throws GameActionException {
        int start = Clock.getBytecodeNum();
        MapLocation loc;
        CowSpot spot = bestSpot;

        // starts at the last best spot seen and moves randomly in small increments from there
        while (Clock.getBytecodeNum() - start < byteCodeLimit + 800){
            int start_debug = Clock.getBytecodeNum();

            loc = new MapLocation(spot.loc.x + Utils.rand_5(), spot.loc.y + Utils.rand_5());
            
            Logger.log(10, "loc: " + loc.x + " " + loc.y);
            spot = new CowSpot(rc, loc);
            Logger.log(10, "spot.loc: " + spot.loc.x + " " + spot.loc.y);
            if (spot.density > bestSpot.density){
                System.out.println("New best spot found: (" + spot.loc.x + " " + spot.loc.y 
                                    + ") ~ " + spot.density + ">" + bestSpot.density);
                bestSpot = spot;
            }

            Logger.log(10, "loop used " + (Clock.getBytecodeNum() - start_debug) + " bc");

        }
        rc.breakpoint();
    }

    public static MapLocation getFirstLoc() {
        int x = hqLoc.x;
        int y = hqLoc.y;

        if (x < 6)
            x = 6;
        else if (x > mapWidth - 6)
            x = mapWidth - 6;
        
        if (y < 6)
            y = 6;
        else if (y > mapHeight - 6)
            y = mapWidth - 6;

        MapLocation loc = new MapLocation(x, y).add(awayFromEnemyHq);
        if (loc.equals(hqLoc))
            loc = loc.add(awayFromEnemyHq);

        return loc;
    }
}
