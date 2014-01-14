package farmbot;

import battlecode.common.*;


public class RobotPlayer
{
    public static void run(RobotController rc)
    {
        Utils.init(rc);

        while (true) {
            try {

                // int start = Clock.getBytecodeNum();
                // System.out.println(Clock.getBytecodeNum() - start);
                // start = Clock.getBytecodeNum();
                // double[][] cows = rc.senseCowGrowth();
                // System.out.println(Clock.getBytecodeNum() - start);
                // for (int i = cows.length; i-- > 0; ) {
                //     for (int j = cows[i].length; j-- > 0; ) {
                //         System.out.print((int)cows[i][j] + " ");
                //     }
                //     System.out.println();
                // }

                if (rc.getType() == RobotType.SOLDIER) Soldier.run(rc);
                if (rc.getType() == RobotType.HQ) Headquarter.run(rc);
                else Bases.run(rc);
            }
            catch(Exception e) {
                e.printStackTrace();
                rc.breakpoint();
            }
            rc.yield();
        }
    }

}
