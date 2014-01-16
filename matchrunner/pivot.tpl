<html>
    <head>
        <title>Battlecode Match Web</title>
        <link rel="stylesheet" type="text/css" href="http://nicolas.kruchten.com/pivottable/examples/pivot.css">
        <script type="text/javascript" src="http://nicolas.kruchten.com/pivottable/examples/jquery-1.8.3.min.js"></script>
        <script type="text/javascript" src="http://nicolas.kruchten.com/pivottable/examples/jquery-ui-1.9.2.custom.min.js"></script>
        <script type="text/javascript" src="http://nicolas.kruchten.com/pivottable/examples/pivot.js"></script>
        <style>
            * {
                font-family: Verdana;
            }
        </style>
    </head>
    <body>
        <script type="text/javascript">
            $(function(){
                $("#output").pivotUI( {{! pivotData }} );
             });
        </script>
        <div id="output" style="margin: 10px;"></div>
    </body>
</html>
