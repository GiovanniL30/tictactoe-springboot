package com.svi.tictactoe.constants;

public enum MessageTopic {

    PLAYER_JOINED("/topic/rooms/%s/player-joined"),
    GAME_COMPLETED("/topic/rooms/%s/game-completed"),
    NEW_ROUND_STARTED("/topic/rooms/%s/new-round-started"),
    GAME_DELETED("/topic/rooms/%s/game-deleted"),
    MOVE_PLACED("/topic/games/%s/move-placed");

    private final String template;

    MessageTopic(String template) {
        this.template = template;
    }

    public String destination(String destinationId) {
        return template.formatted(destinationId);
    }
}
