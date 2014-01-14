package blahbot;

import battlecode.common.*;

public class Comm
{
    static final int RallyPoint = 0;

    static final int GlobalOrder = 10;

    static final int SpotPos = 20;
    static final int MaxSpots = 4;

    Comm(RobotController rc) { this.rc = rc; }


    void setRallyPoint(MapLocation pos) throws GameActionException
    {
        rc.broadcast(RallyPoint + 0, pos.x);
        rc.broadcast(RallyPoint + 1, pos.y);
    }

    MapLocation getRallyPoint() throws GameActionException
    {
        return new MapLocation(
                rc.readBroadcast(RallyPoint + 0),
                rc.readBroadcast(RallyPoint + 1));
    }


    void setGlobalOrder(MapLocation pos, int ttl) throws GameActionException
    {
        rc.broadcast(GlobalOrder + 0, Clock.getRoundNum() + ttl);
        rc.broadcast(GlobalOrder + 2, pos.x);
        rc.broadcast(GlobalOrder + 3, pos.y);
    }

    boolean hasGlobalOrder() throws GameActionException
    {
        return Clock.getRoundNum() <= rc.readBroadcast(GlobalOrder + 0);
    }

    MapLocation globalOrderPos() throws GameActionException
    {
        MapLocation pos = new MapLocation(
                rc.readBroadcast(GlobalOrder + 2),
                rc.readBroadcast(GlobalOrder + 3));

        return pos.x == 0 && pos.y == 0 ? null : pos;
    }



    void spot(MapLocation pos) throws GameActionException
    {
        int index = rc.readBroadcast(SpotPos + 0) + 1;
        int offset = SpotPos + 1 + 3 * (index % MaxSpots);

        rc.broadcast(offset + 0, Clock.getRoundNum());
        rc.broadcast(offset + 1, pos.x);
        rc.broadcast(offset + 2, pos.y);
        rc.broadcast(SpotPos + 0, index);
    }

    MapLocation[] spots(int ttl) throws GameActionException
    {
        int index = rc.readBroadcast(SpotPos + 0);
        int n = index < MaxSpots ? index + 1 : MaxSpots;
        MapLocation result[] = new MapLocation[n];

        int minRound = Clock.getRoundNum() - ttl;
        for (int i = 0, j = 0; i < n; ++i) {
            int offset = 1 + 3 * i;

            if (rc.readBroadcast(offset + 0) <= minRound) continue;

            int x = rc.readBroadcast(offset + 1);
            int y = rc.readBroadcast(offset + 2);
            if (x == 0 && y == 0) continue;

            result[j++] = new MapLocation(x, y);
        }

        return result;
    }



    RobotController rc;
}