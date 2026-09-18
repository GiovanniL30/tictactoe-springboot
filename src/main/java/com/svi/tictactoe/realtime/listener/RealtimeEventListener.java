package com.svi.tictactoe.realtime.listener;

import com.svi.tictactoe.realtime.event.RealtimeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class RealtimeEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onRealtimeEvent(RealtimeEvent event) {
        messagingTemplate.convertAndSend(
                event.topic().destination(event.destinationId()),
                event.payload()
        );
    }
}
