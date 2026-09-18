package com.svi.tictactoe.realtime.event;

import com.svi.tictactoe.constants.MessageTopic;
import com.svi.tictactoe.dto.response.RealtimeResponse;

public record RealtimeEvent(
        String destinationId,
        MessageTopic topic,
        RealtimeResponse payload) {
}
