package blahbot;

import battlecode.common.*;


public class RobotPlayer
{

    public static void run(RobotController rc)
    {
        Utils.init(rc);

        while (true) {
            try {
                if (rc.getType() == RobotType.SOLDIER) Soldier.run(rc);
                if (rc.getType() == RobotType.HQ) Headquarter.run(rc);
                if (rc.getType() == RobotType.NOISETOWER) Tower.run(rc);
                else Pastr.run(rc);
            }
            catch(Exception e) {
                e.printStackTrace();
                rc.breakpoint();
            }
            rc.yield();
        }
    }

}
