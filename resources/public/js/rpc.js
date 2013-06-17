var sess;
var calcClearFirst = false;
var $calcDisplay;

// Initialize on load
$(function() {
    $calcDisplay = $('#calcdisplay > div');

    // Connect to WebSocket
    ab.connect(
        WS_URI,
        // Connection callback
        function (session) {
            sess = session;
            console.log("Connected to " + WS_URI, sess.sessionid());
            sess.prefix("rpc", "http://clj-wamp-example/rpc#");

            $('#error-modal').modal('hide');
            // Enable below to show the change username modal upon connection
            //setTimeout(function () { $('#change-username-modal').modal('show'); }, 500);
        },
        // Disconnection callback
        function (code, reason) {
            sess = null;
            console.log("Connection lost (" + reason + ")");

            $('#error-modal .reason').text(reason);
            $('#error-modal').modal('show');
        },
        //          Http-kit does not currently support sub-protocol headers
        // Options                                          Important!  vvvv
        {'maxRetries': 60, 'retryDelay': 30000, 'skipSubprotocolCheck': true}
    );

    $('#error-modal').modal({ show: false, backdrop: 'static', keyboard: false });

    // Echo example
    $('#call-echo-form').submit(function (e) {
        var $msgInput   = $('#call-echo-input'),
            msg         = $msgInput.val(),
            $msgOutput  = $('#call-echo-output'),
            $echoErrors = $('#echo-errors');

        if (msg.length > 0) {
            // Publish a chat message
            sess.call("rpc:echo", msg).then(
                function (res) {
                    $echoErrors.hide();
                    $msgOutput.show().text('Server says, "' + res + '"');
                },
                function (res) {
                    console.log("Echo error", res);
                    $msgOutput.hide();
                    $echoErrors.show()
                        .text("[" + res.desc.toUpperCase() + "] " + res.detail);
                }
            );
        }

        $msgInput.val(''); // Clear message after sending
        return false; // Cancel form submit
    });

    // Error examples
    var $errorErrors = $('#error-errors');
    $('#err-not-found-btn').click(function() {
        sess.call("rpc:not-found").then(
            function (res) {
                console.log(res); // this shouldn't happen
            },
            function (res) {
                console.log("Not found error", res);
                $errorErrors.show()
                    .text("[" + res.desc.toUpperCase() + "] " + res.detail);
            }
        );
    });
    $('#err-exception-btn').click(function() {
        sess.call("rpc:throw").then(
            function (res) {
                console.log(res); // this shouldn't happen
            },
            function (res) {
                console.log("Exception error", res);
                $errorErrors.show()
                    .text("[" + res.desc.toUpperCase() + "] " + res.detail);
            }
        );
    });

    // Calculator digits update the display
    $('#calc .digit').click(function() {
        var val = $calcDisplay.text();
        var d = $(this).text();

        if (calcClearFirst) {
            val = (d == ".") ? "0." : d;
            calcClearFirst = false;
        } else {
            if (val.length > 20) {
                return
            }
            if (d == ".") {
                if (val.indexOf(".") == -1) {
                    val += ".";
                }
            }
            else {
                val = (val == "0") ? d : val + d;
            }
        }
        $calcDisplay.text(val);
    });

    // Calculator operations send call to server and display the result
    $('#calc .op').click(function() {
        var op = $(this).text();
        var val = $calcDisplay.text();
        if (val == 'Error') {
            $calcDisplay.text("0");
            return;
        }
        $('#calc-errors').hide();

        sess.call("rpc:calc", op, val).then(
            function (res) {
                $calcDisplay.text(res);
            },
            function (res) {
                console.log("Calc error", res);
                $calcDisplay.text('Error');
                $('#calc-errors')
                    .show()
                    .text("[" + res.desc.toUpperCase() + "] " + res.detail);
            }
        );

        calcClearFirst = true;
    });
});