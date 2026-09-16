package com.svi.tictactoe.mapper;

import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.response.game.JoinGameResponse;
import com.svi.tictactoe.dto.response.player.ParticipantResponse;
import com.svi.tictactoe.entity.ParticipantEntity;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class PlayerMapper {

    private PlayerMapper() {
    }

    public static JoinGameResponse toJoinGameResponse(
            ParticipantEntity participant,
            UUID gameId,
            String message) {
        return new JoinGameResponse(message, gameId, toParticipantResponse(participant));
    }

    public static ParticipantResponse toParticipantResponse(ParticipantEntity entity) {
        return new ParticipantResponse(
                entity.getPlayerName(),
                entity.getScore() == null ? 0 : entity.getScore(),
                toSymbol(entity.getSymbol()),
                PlayerType.valueOf(entity.getPlayerType())
        );
    }

    public static ParticipantSummary summarize(List<ParticipantEntity> participants) {
        List<ParticipantResponse> players = participants.stream()
                .filter(participant -> PlayerType.PLAYER.name().equals(participant.getPlayerType()))
                .sorted(Comparator.comparingInt(participant -> Symbol.fromString(participant.getSymbol()).ordinal()))
                .map(PlayerMapper::toParticipantResponse)
                .toList();

        int spectatorCount = (int) participants.stream()
                .filter(participant -> PlayerType.SPECTATOR.name().equals(participant.getPlayerType()))
                .count();

        return new ParticipantSummary(players, spectatorCount);
    }

    private static Symbol toSymbol(String value) {
        return value == null || value.isBlank() ? null : Symbol.fromString(value);
    }

    public record ParticipantSummary(
            List<ParticipantResponse> players,
            int spectatorCount) {
    }
}
