package farmbot;

import battlecode.common.*;
import java.util.*;

class CowSpot
{
    public MapLocation loc;
    public double density;

    public CowSpot(RobotController rc, MapLocation loc) throws GameActionException {
        // Note: Do not create a CowSpot closer than 5 tiles from the map edge!

        // TODO: incorporate distance to HQ and EnemyHQ
        this.loc = loc;
        // System.out.println("CowSpot constructor called with loc: " + loc.x + "," + loc.y);
        // System.out.println(rc);
        if (rc.senseTerrainTile(loc) == TerrainTile.OFF_MAP 
                    || rc.senseTerrainTile(loc) == TerrainTile.VOID) {
            density = -100;
        } else {
            density = Cowdar.cowSexSum(loc.x - 5, loc.y - 5, loc.x + 5, loc.y + 5);

            for (int i=8; i-- > 0;) {
                MapLocation next = new MapLocation(loc.x + Utils.samples_5[i], 
                                                    loc.y + Utils.samples_5[i+10]);
                if (rc.senseTerrainTile(next) == TerrainTile.VOID)
                    density -= Weights.COWDAR_WALL_PENALTY;
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
    public static Direction awayFromEnemyHq;

    public static void init(RobotController _rc) throws GameActionException
    {
        rc = _rc;

        cowSex = rc.senseCowGrowth();
        hqLoc = rc.senseHQLocation();
        mapHeight = rc.getMapHeight();
        mapWidth = rc.getMapWidth();
        awayFromEnemyHq = hqLoc.directionTo(rc.senseEnemyHQLocation()).opposite();

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

        while (Clock.getBytecodeNum() - start < byteCodeLimit + 400){
            loc = new MapLocation(spot.loc.x + Utils.rand_5(), spot.loc.y + Utils.rand_5());
            
            // System.out.println("loc: " + loc.x + " " + loc.y);
            spot = new CowSpot(rc, loc);
            // System.out.println("spot.loc: " + spot.loc.x + " " + spot.loc.y);
            if (spot.density > bestSpot.density){
                System.out.println("New best spot found: (" + spot.loc.x + " " + spot.loc.y 
                                    + ") ~ " + spot.density + ">" + bestSpot.density);
                bestSpot = spot;
            }

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
