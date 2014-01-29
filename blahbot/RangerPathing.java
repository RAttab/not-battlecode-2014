package blahbot;

import battlecode.common.*;
import java.util.*;
import java.util.ArrayList;

class WayPoint
{
	public MapLocation loc;
	public int dist; 
	public boolean obstacle;
	public boolean isGoal;
	
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
		isGoal = ml.distanceSquaredTo(RangerPathing.target) < 5;
	}

	public WayPoint(MapLocation ml, int distance, boolean blocker, WayPoint lastWp) {
		loc = ml;
		dist = distance;
		obstacle = blocker;
		back = lastWp;
		right = null;
		left = null;
		isGoal = ml.distanceSquaredTo(RangerPathing.target) < 5;
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
	public static Deque<WayPoint> stack;

	public static void init(RobotController rc_) {
		rc = rc_;
		ready = false;
		optimal = false;
	}

	// find the next blocker or the goal
	public static WayPoint getNextWayPoint(WayPoint fromWp) {
		int dist = 0;
		
		Direction dir = fromWp.loc.directionTo(target);

		MapLocation nextLoc = fromWp.loc.add(dir);
		TerrainTile thisTile = rc.senseTerrainTile(nextLoc);

		while (thisTile == TerrainTile.NORMAL || thisTile == TerrainTile.ROAD ) 
		{
			if (thisTile == TerrainTile.ROAD)
				dist++;
			if (thisTile == TerrainTile.NORMAL)
				dist += 2;

			nextLoc = nextLoc.add(dir);
			thisTile = rc.senseTerrainTile(nextLoc);

			if (nextLoc.distanceSquaredTo(target) < 4) {
				// TODO
			}
		}

		if (thisTile == TerrainTile.OFF_MAP){
			// Direction nextDir = nextClosestToward(dir, nextLoc.directionTo(target));
			// if (nextDir == null)
			// 	return null;

			Logger.debug_log(0, "WARNING! Pathing hit map edge in a sitation that seems impossible!");
			Logger.debug_log(1, "maybe it's trying to get to an off-map target location?");
			Logger.debug_log(2, "Target: (" + target.x + ", " + target.y + ")");

			return null;
		}

		if (thisTile == TerrainTile.VOID) {
			nextLoc = nextLoc.add(dir.opposite());
			WayPoint next = new WayPoint(nextLoc, dist, true);

			Direction toGoal = nextLoc.directionTo(target);
			Direction right = nextClosestToward(nextLoc, dir.rotateRight(), toGoal);
			if (right != null)
				next.right = getNextWayPoint(next, right, right.rotateLeft());

			Direction left = nextClosestToward(nextLoc, dir.rotateLeft(), toGoal);
			if (left != null)
				next.left = getNextWayPoint(next, left, left.rotateRight());
		}
		return null; // so we can compile for now REMOVE THIS
	}

	// find the end of the blocker or return null
	public static WayPoint getNextWayPoint(WayPoint fromWp, Direction dir, Direction wallDir) {
		int dist = 0;
		MapLocation nextLoc = fromWp.loc.add(dir);
		TerrainTile thisTile = rc.senseTerrainTile(nextLoc);
		MapLocation wallLoc = nextLoc.add(wallDir);
		TerrainTile wallTile = rc.senseTerrainTile(wallLoc);

		while ( (thisTile == TerrainTile.NORMAL || thisTile == TerrainTile.ROAD) 
					&& (wallTile != TerrainTile.NORMAL || wallTile != TerrainTile.ROAD) )
		{
			if (thisTile == TerrainTile.ROAD)
				dist++;
			if (thisTile == TerrainTile.NORMAL)
				dist += 2;

			wallLoc = nextLoc.add(wallDir);
			wallTile = rc.senseTerrainTile(wallLoc);
			nextLoc = nextLoc.add(dir);
			thisTile = rc.senseTerrainTile(nextLoc);
		}

		// if we found the end of the wall
		if (wallTile == TerrainTile.NORMAL || wallTile == TerrainTile.ROAD) {
			int rotateTarget = wallDir.ordinal() - dir.ordinal();
			nextLoc = nextLoc.add(dir.opposite());
			
			Direction toGoal = nextLoc.directionTo(target);
			// OH GOD: diagonal and straight walls behave differently here
			// TODO: deal with head asplode
			WayPoint next = new WayPoint(nextLoc, dist, false);
			// next.right = getNextWayPoint(next, wallLoc.directionTo(target));
		}

		// if we are closed off
		else if (thisTile == TerrainTile.OFF_MAP){
			return null;
		}

		// if we hit a new wall
		else if (thisTile == TerrainTile.VOID) {
			// TODO: this isn't quite right
			int differential = dir.ordinal() - wallDir.ordinal();
			nextLoc = nextLoc.add(dir.opposite());
			Direction toGoal = nextLoc.directionTo(target);
			WayPoint next = new WayPoint(nextLoc, dist, true);
		}

		// SHOULD NEVER REACH THIS POINT
		else {
			Logger.debug_log(0, "WARNING! Max is retarded and messed up his pathing conditions!");
		}
		return null; // so we can compile for now REMOVE THIS
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
        Direction dir = rc.getLocation().directionTo(target);

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