#!/usr/bin/env python
# -*- coding: utf-8 -*-

# **********
# Filename:         CombatRunner.py
# Description:      A class that takes care of running battlecode headless
#                   and get the results in json
# Author:           Marc Vieira Cardinal
# Creation Date:    January 18, 2013
# Revision Date:    February 1, 2014
# **********


# Include the general libraries
import os
import json
import tempfile
import subprocess


# This requires a patched build.xml like so:
  # <target name="file" depends="build">
  #   <java
  #    classpathref="classpath.run"
  #    fork="yes"
  #    classname="battlecode.server.Main">
  #     <jvmarg value="-Dbc.server.mode=headless"/>
  #     <arg line="-c ${config}"/>                  <<<<<<<<<<<<<<<<<<<<<<<<<<<
  #   </java>
  # </target>


class CombatRunner:
    bcTemplateFile = os.path.expanduser("~/not-battlecode-2014/matchrunner/bc.conf.tpl")
    bcPath = os.path.expanduser("~/Battlecode2014/")

    def Run(self, config):
        # print "[CombatRunner][Run] With config [%s]" % config

        # Load the bc template config file
        # That template file should have placeholders
        # like: %(mapName)s  (note that the "s" after the parenthesis is required)
        # that mapName value would be replaced by the value of config['mapName']
        with open(self.bcTemplateFile, "r") as fh:
            templateStr = fh.read()

        # Generate a temp config file
        fh, filename = tempfile.mkstemp()

        # Populate the config file based on template and the config param
        with open(filename, "w") as fh:
            fh.write(templateStr % config)

        # Run the combat and parse the results
        result = self.ParseAntRunResult(self.AntRunHeadless(self.bcPath, filename))

        print "%s vs %s on %s -> %s wins in %d rounds" % \
            (config['bc.game.team-a'], config['bc.game.team-b'],
            config['bc.game.maps'], result['winnerName'], result['maxRound'])

        # Delete the temp config file
        fh.close()
        #os.remove(filename)

        return result

    def ParseAntRunResult(self, antResult):
        result = { "maxRound": 0,
                   "winnerName": "",
                   "winnerTeam": "",
                   "raw": antResult }

        for line in antResult:
            line = line.strip()
            roundStr = "[TRAIN] Round "

            if (line.startswith("[java] [server]")
                and "wins" in line
                and line.endswith(")")):
                winnerItems = line.split(" ")
                print "****"
                print winnerItems
                print "****"
                result['winnerName'] = winnerItems[-5]
                result['winnerTeam'] = winnerItems[-4][1]
            elif line.count(roundStr) > 0:
                split = line.split(" ")

                # This will technically be run multiple times... but wtv.. the
                # last entry is always the max one anyway
                result['maxRound'] = int(split[len(split) - 1])

        return result

    def AntRunHeadless(self, path, configFile):
        command = "cd %s && ant file -Dconfig=%s" % (path, configFile)
        print "Running command [%s]" % command
        return subprocess.check_output(command, shell=True).split("\n")
