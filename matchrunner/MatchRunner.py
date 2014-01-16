#!/usr/bin/env python
# -*- coding: utf-8 -*-

# **********
# Filename:         MatchRunner.py
# Description:      A daemon for running battlecode matches
# Author:           Marc Vieira Cardinal
# Creation Date:    January 14, 2014
# Revision Date:    January 15, 2014
# **********


# Include the general libraries
import os
import sys
import time
import sqlite3
import argparse

# Application specific imports
from daemon import Daemon
from CombatRunner import CombatRunner


gDBFile = "/home/ncode/not-battlecode-2014/matchrunner/state.db"
gSourceLoc = os.path.expanduser("~/not-battlecode-2014/")


def DBSetRunningToError():
    con = sqlite3.connect(gDBFile, isolation_level=None)
    con.row_factory = sqlite3.Row
    cursor = con.cursor()
    
    cursor.execute("""
        UPDATE `matches`
        SET `state` = 'error'
        WHERE `state` = 'running'
        """)
    
    con.close()


def DBGetQueuedMatchList():
    con = sqlite3.connect(gDBFile, isolation_level=None)
    con.row_factory = sqlite3.Row
    cursor = con.cursor()

    matches = cursor.execute("""
        SELECT
            `id`,
            `bota`,
            `botb`,
            `map`
        FROM `matches`
        WHERE `state` = 'queued'
        ORDER BY
            `schedon` ASC
        """).fetchall()
    print matches
    matches = [ dict(rows) for rows in matches ]

    con.close()
    return matches


def DBStartMatch(matchId):
    con = sqlite3.connect(gDBFile, isolation_level=None)
    con.row_factory = sqlite3.Row
    cursor = con.cursor()

    cursor.execute("""
        UPDATE `matches`
        SET
            `state` = 'running',
            `startedon` = ?
        WHERE `id` = ?
        """, (time.strftime("%Y-%m-%d %H:%M:%S"),
              matchId))
    con.close()


def DBEndMatch(matchId, log, winner):
    con = sqlite3.connect(gDBFile, isolation_level=None)
    con.row_factory = sqlite3.Row
    cursor = con.cursor()

    cursor.execute("""
        UPDATE `matches`
        SET
            `state` = ?,
            `log` = ?,
            `winner` = ?,
            `endedon` = ?
        WHERE `id` = ?
        """, ( ('error' if winner == "" else 'success'),
               str(log),
               winner,
               time.strftime("%Y-%m-%d %H:%M:%S"),
               matchId ))
    con.close()


class MatchRunner(Daemon):
    def run(self):

        # TODO: Make this better by detecting if it has already been done
        # or simply overwite the build.xml with a sample
        sys.stdout.write("""
            This requires a patched build.xml like so:
              <target name="file" depends="build">
                <java
                 classpathref="classpath.run"
                 fork="yes"
                 classname="battlecode.server.Main">
                  <jvmarg value="-Dbc.server.mode=headless"/>
                  <arg line="-c ${config}"/>    <<<<<<<<<<<<<<<<<<<<<<<<<<<
                </java>
              </target>\n\n""")

        # Since we just started the MatchRunner,
        # set the matches that were already running to "error"
        DBSetRunningToError()

        while True:

            # Fetch a list of matches to run
            matches = DBGetQueuedMatchList()
            sys.stdout.write("Matches to run %s\n" % matches)

            sys.stdout.write("Deploying latest version of the bots\n")
            os.system("cd %s ; git fetch ; git rebase ; make all" % gSourceLoc)

            sys.stdout.write("Running matches...\n")
            for match in matches:
                DBStartMatch(match["id"])

                cbRun = CombatRunner()
                result = cbRun.Run({ "bc.game.team-a": match['bota'],
                                     "bc.game.team-b": match['botb'],
                                     "bc.game.maps":   match['map'],
                                     "bc.server.save-file": "match-%s-%s.rms"
                                        % (time.strftime("%Y%m%d-%H%M%S"),
                                           match['id']) })
                print result

                DBEndMatch(match["id"], result['raw'], result['winnerName'])

            sys.stdout.write("--- Checkpoint %s\n"
                % time.strftime("%Y-%m-%d %H:%M:%S"))
            sys.stdout.flush()
            time.sleep(60)


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
