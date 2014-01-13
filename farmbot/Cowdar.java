package farmbot;

import battlecode.common.*;
import java.util.*;

public class Cowdar
{
    public static Utils utils;
    public static RobotController rc;
    public static double[] x_vec = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0];
    public static double[] y_vec = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0];
    public static double[] weights = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0];
    public static int lastTurnUpdated = 0;
    public static int lastIndex = -1;

    public static void init(RobotController rc, Utils utils)
    {
        rc = rc;
        utils = utils;
    }

    public static void update(int maxByteCodes) {
        if (lastTurnUpdated == Clock.getRoundNum())
            return;

        int i = (lastIndex + 1) % 10;
        lastTurnUpdated = Clock.getRoundNum();

    }


}
