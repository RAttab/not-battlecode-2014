#!/usr/bin/env python
# -*- coding: utf-8 -*-

# **********
# Filename:         MatchRunner.py
# Description:      A daemon for running battlecode matches
# Author:           Marc Vieira Cardinal
# Creation Date:    January 14, 2014
# Revision Date:    January 14, 2014
# **********


# Include the general libraries
import os
import sys
import time
import sqlite3
import argparse

# Application specific imports
from daemon import Daemon


gDBFile = "/home/ncode/not-battlecode-2014/matchrunner/state.db"


def DBSetRunningToError():
    con = sqlite3.connect(gDBFile)
    con.row_factory = sqlite3.Row
    cursor = con.cursor()
    
    cursor.execute("""
        UPDATE `matches`
        SET `state` = 'error'
        WHERE `state` = 'running'
        """)
    
    con.close()


def DBGetQueuedMatchList():
    con = sqlite3.connect(gDBFile)
    con.row_factory = sqlite3.Row
    cursor = con.cursor()

    matches = cursor.execute("""
        SELECT
            `id`,
            `bota`,
            `botb`
        FROM `matches`
        WHERE `state` = 'queued'
        ORDER BY
            `schedon` ASC
        """).fetchall()
    print matches
    matches = [ dict(rows) for rows in matches ]

    con.close()
    return matches


class MatchRunner(Daemon):
    def run(self):

        # Since we just started the MatchRunner,
        # set the matches that were already running to "error"
        DBSetRunningToError()

        while True:

            # Fetch a list of matches to run
            matches = DBGetQueuedMatchList()
            sys.stdout.write("Matches to run %s\n" % matches)

            ## Put some code here to run the matches...

            sys.stdout.write("--- Checkpoint %s\n"
                % time.strftime("%Y-%m-%d %H:%M:%S"))
            sys.stdout.flush()
            time.sleep(10)


if __name__ == "__main__":
    # Parse the command line arguments
    argParser = argparse.ArgumentParser(prog = "MatchRunner",
                                        description = "A daemon for running battlecode matches")
    argParser.add_argument("action",
                           choices=('start','stop','restart'),
                           help='Interact with the daemon')
    args = vars(argParser.parse_args())
    
    daemon = MatchRunner('/tmp/matchRunner.pid',
                         '/dev/null',
                         '/tmp/matchRunner.log',
                         '/tmp/matchRunner.err')
    
    if "start" == args['action']:
        print "Starting daemon"
        daemon.start()
    elif "stop" == args['action']:
        print "Stopping daemon"
        daemon.stop()
    elif "restart" == args['action']:
        print "Restarting daemon"
        daemon.restart()
    else:
        print "Unknown command"
        sys.exit(1)

    sys.exit(0)
