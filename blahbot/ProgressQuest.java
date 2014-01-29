package blahbot;

import java.lang.*;
import battlecode.common.*;

public class ProgressQuest
{

    public ProgressQuest(RobotController rc) { this.rc = rc; }

    public void progress()
    {
        int round = Clock.getRoundNum();
        if (round % ProgressRate > 0) return;

        progress++;
        rc.setIndicatorString(0, bar((progress / MaxProgress) % MaxProgress));
        rc.setIndicatorString(1, bar(progress % MaxProgress));

        int line = (progress / MaxProgress) % Lines.length;
        rc.setIndicatorString(2, Lines[line]);
        if (progress % MaxProgress == 0)
            System.out.println(Lines[line]);
    }

    private String bar(int progress)
    {
        int i;
        char bar[] = new char[MaxProgress];

        for (i = 0; i < progress; ++i) bar[i] = '|';
        for (; i < MaxProgress; ++i) bar[i] = ' ';

        return "[" + new String(bar) + "]";
    }

    private int progress = 0;
    private RobotController rc;

    private static final int ProgressRate = 1;
    private static final int MaxProgress = 45;

    private static final String Lines[] = {
        "Experiencing an enigmatic and foreboding night vision...",
        "Much is revealed about that wise old bastard you'd underestimated...",
        "A shocking series of events leaves you alone and bewildered, but resolute...",
        "Drawing upon an unrealized reserve of determination, you set out on a long and arduous journey..."
    };

};
