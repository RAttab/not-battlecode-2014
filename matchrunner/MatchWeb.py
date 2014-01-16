#!/usr/bin/env python
# -*- coding: utf-8 -*-

# **********
# Filename:         MatchWeb.py
# Description:      An http based match frontend for battlecode
# Author:           Marc Vieira Cardinal
# Creation Date:    January 14, 2014
# Revision Date:    January 15, 2014
# **********


# Include the general libraries
import os
import sys
import time
import argparse

# Include bottle specifics
# (need pip install bottle bottle-sqlite)
import bottle
from bottle import request, response, redirect, route, get, post, delete, template
from bottle.ext import sqlite


@get("/")
def Main(db):
    cursor = db.cursor()

    tplData = {}

    tplData['matches'] = cursor.execute("""
        SELECT
            `id`,
            `schedon`,
            `startedon`,
            `endedon`,
            `bota`,
            `botb`,
            `map`,
            `winner`,
            `state`,
            `log`
        FROM `matches`
        ORDER BY
            `schedon` ASC,
            `startedon` ASC,
            `endedon` ASC
        """).fetchall()

    tplData['bots'] = cursor.execute("""
        SELECT
            `name`
        FROM `bots`
        ORDER BY `name` ASC
        """).fetchall()

    tplData['mapNames'] = cursor.execute("""
        SELECT
            `name`
        FROM `maps`
        ORDER BY `name` ASC
        """).fetchall()

    return template("main.tpl", tplData)

@post("/")
def SubmitMatch(db):
    cursor = db.cursor()

    botA = request.forms.botA
    botB = request.forms.botB
    mapName = request.forms.mapName

    if botA and botB and mapName:
        cursor.execute("""
            INSERT INTO `matches`
                (`schedon`, `bota`, `botb`, `map`, `state`)
            VALUES
                (?, ?, ?, ?, ?)
            """, (time.strftime("%Y-%m-%d %H:%M:%S"),
                  botA,
                  botB,
                  mapName,
                  'queued'))

    # Return our default page
    redirect("/")


@get("/match/<matchId:int>")
def MatchDetails(db, matchId):
    cursor = db.cursor()

    matchData = cursor.execute("""
        SELECT
            `id`,
            `schedon`,
            `startedon`,
            `endedon`,
            `bota`,
            `botb`,
            `winner`,
            `map`,
            `state`,
            `log`
        FROM `matches`
        WHERE `id` = ?
        """, [matchId]).fetchall()
    return { "match": [ dict(rows) for rows in matchData ] }

if __name__ == "__main__":
    argParser = argparse.ArgumentParser(prog = "MatchWeb",
                                        description = """An http based match frontend for battlecode""")
    argParser.add_argument("--bind",
                           dest = "bind",
                           action = "store",
                           help = "Address to bind to (default: 127.0.0.1)",
                           default = "127.0.0.1")
    argParser.add_argument("--port",
                           dest = "port",
                           action = "store",
                           help = "Port to listen on",
                           default = 1337,
                           type = int)
    argParser.add_argument("--db",
                           dest = "db",
                           action = "store",
                           help = "SQLite state database",
                           default = "state.db")

    args = vars(argParser.parse_args())

    if not args['bind'] or not args['port']:
        argParser.print_help()
        exit(1)

    bottle.TEMPLATE_PATH.insert(0, os.getcwd())
    app = bottle.app()
    pluginSqlite = sqlite.Plugin(dbfile = args['db'], keyword = 'db')
    app.install(pluginSqlite)
    bottle.run(app   = app,
           host  = args['bind'],
           port  = args['port'],
           debug = True)

    time.sleep(1)
    sys.stdout.write("Done\n\n")
    exit(0)
