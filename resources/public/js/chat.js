var sess;
var wsuri = "ws://localhost:8080/ws";
var currentUsername = 'guest';
var messageId = 0;

// Initialize on load
$(function() {
    // Connect to WebSocket
    ab.connect(
        wsuri,
        // Connection callback
        function (session) {
            sess = session;
            console.log("Connected to " + wsuri, sess.sessionid());
            sess.prefix("event", "http://clj-wamp-example/event#"); // Add a CURI prefix
            sess.subscribe("event:chat", onEvent);                  // Subscribe to chat channel
        },
        // Disconnection callback
        function (code, reason) {
            sess = null;
            console.log("Connection lost (" + reason + ")");
        },
        // Options                                         Important!  vvvv
        {'maxRetries': 60, 'retryDelay': 5000, 'skipSubprotocolCheck': true}
    );

    // Message submit form handler
    $('#send-message-form').submit(function (e) {
        var $msgInput = $('#message-input'),
            chatMsg = $msgInput.val();

        if (chatMsg.length > 0) {
            // Publish a chat message
            sess.publish("event:chat", { type: 'message', message: chatMsg });
        }

        $msgInput.val(''); // Clear message after sending
        return false;      // Cancel form submit
    });

    // Change username form/modal
    $('#change-username-form').submit(function(e) {
        changeUsername($('#username').val());
        $('#change-username-modal').modal('hide');
        return false; // Cancel form submit
    });
    $('#change-username-modal')
        .modal({show: false})
        .on('hidden', function() {
            $('#message-input').focus();
        })
        .on('shown', function() {
            $('#username').val(currentUsername);
            $('#username').focus().select();
        });

    // System messages toggle
    $('#hide-system-msgs-btn').button().click(toggleSystemMessages);
    toggleSystemMessages();
});

// Handle WebSocket/WAMP Events

function onEvent(topic, event) {
    console.log("Received event", topic, event);
    switch (event.type) {
        case 'message':
            notifyChatMessage(event.clientId, event.username, event.message);
            break;
        case 'user-joined':
            addUser(event.clientId, event.username);
            notifyJoined(event.username);
            break;
        case 'user-left':
            delUser(event.clientId);
            notifyUserLeft(event.username);
            break;
        case 'user-list':
            resetUserList(event.users);
            break;
        case 'username':
            delUser(event.clientId);
            addUser(event.clientId, event.newUsername);
            notifyUsernameChanged(event.oldUsername, event.newUsername);
            break;
    }
}

// Chat Messaging

function addMessage(mid, msgStr) {
    var $msgWrap = $('#messages-wrap');
    // Get scroll position prior to adding elements.
    // Only keep chat scrolled to bottom when already scrolled to bottom.
    var scrollDiff = getScrollDiff($msgWrap);

    var messages = $('#messages').append(msgStr);
    // Trigger the CSS3 transition
    setTimeout(function() { $('#msg-' + mid).removeClass('new') }, 1);

    // Don't scroll if user has backscrolled (might be reading)
    if (scrollDiff < 5) { // 5px threshold
        scrollToBottom($msgWrap);
    }
}

function notifyChatMessage(clientId, username, message) {
    var mid = messageId++;
    addMessage(mid,
        formatTemplate('message-template', {
            "id":       "msg-" + mid,
            "username": username,
            "time":     getTimeFmt(),
            "message":  message,
            "type":     (clientId === sess.sessionid()) ? "self" : "user"
        }));
}

// User List Management

function addUser(clientId, username) {
    var templateId = (clientId === sess.sessionid())
        ? "user-self-template" : "user-template";

    var users = $('#users').append(
        formatTemplate(templateId, {
            "clientId": clientId,
            "username": username,
            "type":     (clientId === sess.sessionid()) ? "self" : "user"
        }));

    sortUsers();

    var $newUserEle = $('#user-' + clientId);
    // Trigger the CSS3 transition
    setTimeout(function() { $newUserEle.removeClass('new'); }, 1);

    if (clientId === sess.sessionid()) {
        currentUsername = username;
        $newUserEle.click(function(e) {
            $('#change-username-modal').modal('show');
        });
        $newUserEle.css('cursor','pointer');
    }
}

function sortUsers() {
    var tmpArr = $('#users li');
    $(tmpArr).detach();
    tmpArr.sort(function(a, b) {
        a = $('.label', a).text().toLowerCase();
        b = $('.label', b).text().toLowerCase();

        if (a < b) {
            return -1;
        } else if (a > b) {
            return 1;
        } else {
            return 0;
        }
    });
    $('#users').append(tmpArr);
}

function notifyJoined(username) {
    var message = escapeHtml(username) + " has joined the channel";
    var mid = messageId++;
    addMessage(mid,
        formatTemplate('system-message-template', {
            "id":       "msg-" + mid,
            "time":     getTimeFmt(),
            "message":  message
        }));
}

function delUser(clientId) {
    $('#user-' + clientId).remove();
}

function notifyUserLeft(username) {
    var message = escapeHtml(username) + " has left the channel";
    var mid = messageId++;
    addMessage(mid,
        formatTemplate('system-message-template', {
            "id":       "msg-" + mid,
            "time":     getTimeFmt(),
            "message":  message
        }));
}

function resetUserList(users) {
    $('#users').empty();
    for (var i=0; i < users.length; i++) {
        addUser(users[i].clientId, users[i].username);
    }
}

// Username Changes

function changeUsername(newUsername) {
    if (newUsername != currentUsername) {
        sess.publish("event:chat", { type: 'username', newUsername: newUsername });
    }
}

function notifyUsernameChanged(oldUsername, newUsername) {
    var message = escapeHtml(oldUsername) + " has changed username to: " + escapeHtml(newUsername);
    var mid = messageId++;
    addMessage(mid,
        formatTemplate('system-message-template', {
            "id":       "msg-" + mid,
            "time":     getTimeFmt(),
            "message":  message
        }));
}

// Utilities

function getTimeFmt() {
    return new Date().toTimeString().replace(/.*(\d{2}:\d{2}):\d{2}.*/, "$1");
}

function escapeHtml(str) {
    return $('<div/>').text(str).html();
}

function getScrollDiff(div) {
    return $(div)[0].scrollHeight - $(div).height() - $(div).scrollTop();
}
function scrollToBottom(div) {
    $(div).scrollTop($(div)[0].scrollHeight);
}

function formatTemplate(templateId, varMap) {
    var template = $('#' + templateId).html();
    return template.replace(/\${(.+?)}/g, function(match, key) {
        return (key in varMap) ? escapeHtml(varMap[key]) : '';
    });
}

function toggleSystemMessages() {
    var styleFilter = $('#filter-system-msgs-style');
    var disabled = styleFilter[0].disabled = !(styleFilter[0].disabled);
    if (disabled) {
        scrollToBottom($('#messages-wrap'));
    }
}