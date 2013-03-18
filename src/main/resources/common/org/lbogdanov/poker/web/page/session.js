var Poker = (function() {

    var appendMsg = function(msg) {
        var chatLog = $("#chatLog");
        if (typeof msg === "string") { // otherwise msg is Element
            msg = $("<pre>").text(msg);
        }
        $(".mCSB_container", chatLog).append(msg);
        chatLog.mCustomScrollbar("update");
        chatLog.mCustomScrollbar("scrollTo", "last");
    };

    $(function() {
        // send chat messages on Ctrl / Meta + Enter, ignore single line break in a message input
        $("#chatMsg").keydown(function(evt) {
            if (evt.which === 10 || evt.which === 13) {
                if ($(this).val().length === 0) {
                    return false;
                }
                if (evt.ctrlKey || evt.metaKey) {
                    $("#chatSend").click();
                }
            }
        });
        // turn Bootstrap tooltips on
        $(".tip").tooltip();
        // turn custom scrollbars on
        $("#chatLog").mCustomScrollbar({
            theme: "dark",
            scrollButtons: {
                enable: true
            }
        });
    });

    return {
        msgSend: function(xhr) {
            var msg,
                chatMsg = $("#chatMsg");
            if (xhr.status === 200) {
                msg = $.i18n.printf("[%s]: %s", [$.i18n._("chat.me"), chatMsg.val()]);
                chatMsg.val("");
            } else {
                msg = $.i18n._("chat.sendError", [xhr.statusText]);
                msg = $("<div>").addClass("error").text(msg);
            }
            appendMsg(msg);
        }
    };
})();
