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
	public static boolean ready;
	public static boolean optimal;

	public static void init(RobotController rc_) {
		rc = rc_;
		ready = false;
		optimal = false;
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
			// Direction nextDir = nextClosestToward(dir, nextLoc.directionTo(target));
			// if (nextDir == null)
			// 	return null;

			Logger.log(0, "WARNING! Pathing hit map edge in a sitation that seems impossible!");
			Logger.log(1, "maybe it's trying to get to an off-map target location?");
			Logger.log(2, "Target: (" + target.x + ", " + taget.y + ")");

			return null;
		}

		if (thisTile == TerrainTile.VOID) {
			nextLoc = nextLoc.add(dir.opposite());
			WayPoint next = new WayPoint(nextLoc, dist, true);

			Direction toGoal = nextLoc.directionTo(target);
			Direction right = nextClosestToward(nextLoc, dir.rotateRight(), toGoal);
			if (right != null)
				next.right = new getNextWayPoint(next, right, right.rotateLeft());

			Direction left = nextClosestToward(nextLoc, dir.rotateLeft(), toGoal);
			if (left != null)
				next.left = new getNextWayPoint(next, left, left.rotateRight());
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
			WayPoint next = new WayPoint(nextLoc, dist, false);
		}

		if (thisTile == TerrainTile.OFF_MAP){
			// this means we are closed off
			return null;
		}

		if (thisTile == TerrainTile.VOID) {
			nextLoc = nextLoc.add(dir.opposite());
			Direction toGoal = nextLoc.directionTo(target);
			WayPoint next = new WayPoint(nextLoc, dist, true);
		}
	}

	public static Direction nextClosestToward(MapLocation loc, Direction dir, Direction toward) {
		if ((dir.ordinal() - toward.ordinal()) % 8 == 4) {
			return null;
		}
		if (isRightOf(toward, dir)) {
			while (dir != toward) {
				dir.rotateRight();
				if (rc.senseTerrainTile(loc.add(dir)) != TerrainTile.VOID && 
							rc.senseTerrainTile(loc.add(dir)) != TerrainTile.OFF_MAP) {
					return dir;
				}
			}
			return null;
		} else {
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

	public static boolean isRightOf(Direction first, Direction second) {
		// TODO : I am tired, there's probably a better way to do this
		// whether first is right of second
		if (first == second.rotateRight())
			return true;
		if (first == second.rotateRight().rotateRight())
			return true;
		if (first == second.rotateRight().rotateRight().rotateRight())
			return true;
		return false;
	}
	public static boolean isLeftOf(Direction first, Direction second) {
		// TODO : I am tired, there's probably a better way to do this
		// whether first is Left of second
		if (first == second.rotateLeft())
			return true;
		if (first == second.rotateLeft().rotateLeft())
			return true;
		if (first == second.rotateLeft().rotateLeft().rotateLeft())
			return true;
		return false;
	}

	public static void computeSubPaths (int maxByteCodes) {
		// TODO
	}

	public static void setTarget(MapLocation loc) {
		target = loc;
		ready = false;
		optimal = false;
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