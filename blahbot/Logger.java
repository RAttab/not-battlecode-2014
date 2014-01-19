package blahbot;

import battlecode.common.*;

public class Logger {
	public static int[] ACTIVE_CHANNELS = {0,1,2};
	public static boolean TO_STDOUT = false;

	public static RobotController rc;

	public static void init(RobotController rc_) {
		rc = rc_;
	}

	public static void log(int channel, String msg) {
		for (int chan : ACTIVE_CHANNELS) {
			if (channel == chan) {
				if (TO_STDOUT) {
					System.out.println(msg);
				} else {
					rc.setIndicatorString(channel, msg);
				}
			}
		}
	}

}