<html>
    <head>
        <title>Battlecode Match Web</title>
        <style type='text/css'>
            table.result {
                border-width: 3px 3px 3px 3px;
                border-spacing: 0px;
                border-style: solid solid solid solid;
                border-color: black black black black;
                border-collapse: collapse;
                background-color: white;
            }
            table.result th {
                border-width: 2px 2px 2px 2px;
                padding: 1px 1px 1px 1px;
                border-style: solid solid solid solid;
                border-color: black black black black;
                background-color: #dddddd;
                -moz-border-radius: 0px 0px 0px 0px;
            }
            table.result td {
                border-width: 1px 1px 1px 1px;
                padding: 1px 1px 1px 1px;
                border-style: dotted dotted dotted dotted;
                border-color: black black black black;
                background-color: white;
                -moz-border-radius: 0px 0px 0px 0px;
            }
            #counter {
                font-weight:bold;
                font-family:courier new;
                font-size:12pt;
            }
        </style>
    </head>
    <body>
        <center>
            Next refresh in <span id=counter> </span> seconds.<br />
            Or click <a href="javascript:self.location.reload()">here</a> to refresh now.
        </center>

        <script>
            var counterObj = document.all ? counter : document.getElementById("counter");
            var countdownFrom = 120; //countdown period in seconds
            var currentSecond = counterObj.innerHTML = countdownFrom+1; 

            function countdown() {
                if (currentSecond > 1) {
                    currentSecond -= 1;
                    counterObj.innerHTML = currentSecond;
                } else {
                    self.location.reload();
                    return;
                }
                setTimeout("countdown()",1000)
            }
            countdown()
        </script>
        <form action="/" method="post">
            <table class="result">
                <tr>
                    <th>Id</th>
                    <th>Sched On</th>
                    <th>Started On</th>
                    <th>Ended On</th>
                    <th>Bot A</th>
                    <th>Bot B</th>
                    <th>Map</th>
                    <th>Winner</th>
                    <th>State</th>
                    <th>Logs</th>
                </tr>
                %for match in matches:
                    <tr>
                        <td>{{ match['id'] }}</td>
                        <td>{{ match['schedon'] }}
                        <td>{{ match['startedon'] }}</td>
                        <td>{{ match['endedon'] }}</td>
                        <td>{{ match['bota'] }}</td>
                        <td>{{ match['botb'] }}</td>
                        <td>{{ match['map'] }}</td>
                        <td>{{ match['winner'] }}</td>
                        <td>{{ match['state'] }}</td>
                        <td><a href="/match/{{ match['id'] }}">Details</a></td>
                    </tr>
                %end
                <tr>
                    <th colspan="4">New match:</th>
                    <th>
                        <select name="botA">
                            <option value="">- Bot A -</option>
                            %for bot in bots:
                                <option>{{ bot['name'] }}</option>
                            %end
                        </select>
                    </th>
                    <th>
                        <select name="botB">
                            <option value="">- Bot B -</option>
                            %for bot in bots:
                                <option>{{ bot['name'] }}</option>
                            %end
                        </select>
                    </th>
                    <th>
                        <select name="mapName">
                            <option value="">- Map Name -</option>
                            %for mapName in mapNames:
                                <option>{{ mapName['name'] }}</option>
                            %end
                        </select>
                    </th>
                    <th colspan="3">
                        <input type="submit" value="Submit" />
                    </th>
                </tr>
            </table>
        </form>
    </body>
</html>