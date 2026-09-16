package com.svi.tictactoe.realtime.event;

import com.svi.tictactoe.constants.MessageTopic;

public record RealtimeEvent<T>(
        String destinationId,
        MessageTopic topic,
        T payload) {
}
