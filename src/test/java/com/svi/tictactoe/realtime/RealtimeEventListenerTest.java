package com.svi.tictactoe.realtime;

import com.svi.tictactoe.constants.MessageTopic;
import com.svi.tictactoe.realtime.event.RealtimeEvent;
import com.svi.tictactoe.realtime.listener.RealtimeEventListener;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RealtimeEventListenerTest {

    @Test
    void sendsThePayloadToTheRoomScopedDestination() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        RealtimeEventListener listener = new RealtimeEventListener(messagingTemplate);
        String payload = "updated board";

        listener.onRealtimeEvent(new RealtimeEvent<>(
                "1f0c5258-219a-4f76-adc0-38b08300317b",
                MessageTopic.MOVE_PLACED,
                payload
        ));

        verify(messagingTemplate).convertAndSend(
                "/topic/games/1f0c5258-219a-4f76-adc0-38b08300317b/move-placed",
                payload
        );
    }
}
