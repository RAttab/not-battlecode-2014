package blahbot;

import battlecode.common.*;
import java.util.*;
import java.util.ArrayList;

class WayPoint
{
	public MapLocation loc;
	public int dist; 
	public boolean obstacle;
	
	public WayPoint back;

	public WayPoint right;
	public WayPoint left;
	// maybe left/right Direction, find next waypoints later?

	public WayPoint(MapLocation ml, int distance, boolean blocker) {
		loc = ml;
		dist = distance;
		obstacle = blocker;
		right = null;
		left = null;
	}

	public WayPoint(MapLocation ml, int distance, boolean blocker, WayPoint lastWp) {
		loc = ml;
		dist = distance;
		obstacle = blocker;
		back = lastWp;
		right = null;
		left = null;
	}
}

public class RangerPathing
{
	public static RobotController rc;
	public static MapLocation target;
	public static ArrayList<MapLocation> path;
	public static WayPoint start;

	public static void init(RobotController rc_) {
		rc = rc_;
	}

	public static WayPoint getNextWayPoint(WayPoint fromWp, Direction dir) {
		int dist = 0;
		MapLocation nextLoc = fromWp.loc.add(dir);
		TerrainTile thisTile = rc.senseTerrainTile(nextLoc);

		while (thisTile == TerrainTile.NORMAL || thisTile == TerrainTile.ROAD ) 
		{
			if (thisTile == TerrainTile.TOAD)
				dist++;
			if (thisTile == TerrainTile.NORMAL)
				dist += 2;

			nextLoc = nextLoc.add(dir);
			thisTile = rc.getTerrainTile(nextLoc);
		}

		if (thisTile == TerrainTile.OFF_MAP){
			Direction nextDir = nextClosestToward(dir, nextLoc.directionTo(target));
			if (nextDir == null)
				return null;
			// TODO:
			// do this stuff in waypoint constructor maybe?
			// return new WayPoint(fromWp, ...);
		}

		if (thisTile == TerrainTile.VOID) {
			WayPoint next = new WayPoint(nextLoc.add(dir.opposite()), dist, true);
			// TODO
			// next.right = new WayPoint(next, ...);
		}
	}

	public static WayPoint getNextWayPoint(WayPoint fromWp, Direction dir, Direction wallDir) {
		int dist = 0;
		MapLocation current = fromWp.loc.add(dir);
		TerrainTile thisTile = rc.senseTerrainTile(current);
		TerrainTile wallTile = rc.senseTerrainTile(current.add(wallDir));

		while ( (thisTile == TerrainTile.NORMAL || thisTile == TerrainTile.ROAD) 
					&& (wallTile != TerrainTile.NORMAL || wallTile != TerrainTile.ROAD) 
		{
			if (thisTile == TerrainTile.TOAD)
				dist++;
			if (thisTile == TerrainTile.NORMAL)
				dist += 2;
			
			wallTile = rc.getTerrainTile(current.add(wallDir));
			current = current.add(dir);
			thisTile = rc.getTerrainTile(current);
		}

		if (wallTile == TerrainTile.NORMAL || wallTile == TerrainTile.ROAD) {
			WayPoint next = new WayPoint()
			// find the right direction for the next wp
		}

		if (thisTile == TerrainTile.OFF_MAP){
			return null;
		}

		if (thisTile == TerrainTile.VOID) {
			WayPoint next = new WayPoint(current.add(dir.opposite()), dist, true);
		}
	}

	public static Direction nextClosestToward(MapLocation loc, Direction dir, Direction toward) {
		if ((dir.ordinal() - toward.ordinal()) % 8 == 4) {
			return null;
		}
		if (dir.ordinal() - toward.ordinal() < 0) {
			while (dir != toward) {
				dir.rotateRight();
				if (rc.senseTerrainTile(loc.add(dir)) != TerrainTile.VOID && 
							rc.senseTerrainTile(loc.add(dir)) != TerrainTile.OFF_MAP) {
					return dir;
				}
			}
			return null;
		}
		if (dir.ordinal() - toward.ordinal() < 0) {
			while (dir != toward) {
				dir.rotateLeft();
				if (rc.senseTerrainTile(loc.add(dir)) != TerrainTile.VOID && 
							rc.senseTerrainTile(loc.add(dir)) != TerrainTile.OFF_MAP) {
					return dir;
				}
			}
			return null;
		}
	}

	public static void computePath (int maxByteCodes) {
		// TODO
	}

	public static void setTarget(MapLocation loc) {
		target = loc;
		ready = false;
	}

	public static Direction next(){
		if (ready) {
			// TODO
			// return (the right direction)
		}

		// TODO : better fallback
        Direction dir = rc.getLocation().directionTo(dest);

        if (dir == Direction.OMNI)
            return Direction.NONE;

        for (int i=8; i-- > 0; ){
            if (rc.canMove(dir))
                return dir;
            dir = dir.rotateRight();
        }
        return Direction.NONE;
	}
}