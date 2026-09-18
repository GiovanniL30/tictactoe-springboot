package com.svi.tictactoe.realtime.event;

import com.svi.tictactoe.constants.MessageTopic;

public record RealtimeEvent(
        String destinationId,
        MessageTopic topic,
        RealtimePayload payload) {
}
