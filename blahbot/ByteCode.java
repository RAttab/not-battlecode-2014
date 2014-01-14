package blahbot;

import battlecode.common.*;

public class ByteCode
{
    static class Check
    {
        int round;
        int start;

        RobotController rc;

        Check(RobotController rc)
        {
            this.rc = rc;
            round = Clock.getRoundNum();
            start = Clock.getBytecodeNum();
        }

        void debug_check(String str)
        {
            if (Clock.getRoundNum() == round) return;

            int stop = Clock.getBytecodeNum();
            System.out.println("bytecode overflow(" + str + "): "
                    + "(" + start + ", " + round + ") -> "
                    + "(" + stop + ", " + Clock.getRoundNum() + ")");
            rc.breakpoint();
        }
    }

    static class Profiler
    {
        int start = 0;
        int round = 0;

        static final int OVERHEAD = 5;

        Profiler()
        {
            round = Clock.getRoundNum();
            start = Clock.getBytecodeNum();
        }

        int get()
        {
            int stop = Clock.getBytecodeNum();
            int len;
            if (Clock.getRoundNum() > round)
                len = 10000 - start + stop;
            else len = stop - start;
            return len;
        }

        void debug_dump(String str)
        {
            System.out.println("profiler(" + str + "): " + get());
        }
    }

    static class ProfilerDist
    {
        ProfilerDist() {}

        void debug_start() { prof = new Profiler(); }
        void debug_stop()
        {
            int value = prof.get();
            n++;
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        void debug_dump(String str)
        {
            int mean;
            if (n==0)
                mean = 0;
            else
                mean = sum / n;
            System.out.println("profiler(" + str + "): " + n
                    + " [ " + min + ", " + mean + ", " + max + " ]");
        }

        Profiler prof;
        int n = 0, sum = 0, min = 100000 + 1, max = 0;
    }
}
