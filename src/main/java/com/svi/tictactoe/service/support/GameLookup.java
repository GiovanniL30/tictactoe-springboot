package com.svi.tictactoe.service.support;

import com.svi.tictactoe.constants.ErrorMessage;
import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.entity.GameEntity;
import com.svi.tictactoe.entity.GameRoundEntity;
import com.svi.tictactoe.entity.RoomEntity;
import com.svi.tictactoe.entity.RoomPlayerEntity;
import com.svi.tictactoe.exception.ApiException;
import com.svi.tictactoe.exception.GameNotFoundException;
import com.svi.tictactoe.exception.PlayerNotFoundException;
import com.svi.tictactoe.exception.RoomInactiveException;
import com.svi.tictactoe.repository.cassandra.GameRepository;
import com.svi.tictactoe.repository.cassandra.GameRoundRepository;
import com.svi.tictactoe.repository.cassandra.RoomPlayerRepository;
import com.svi.tictactoe.repository.cassandra.RoomRepository;
import com.svi.tictactoe.util.PlayerNameUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Component
public class GameLookup {

    private final RoomRepository roomRepository;
    private final GameRepository gameRepository;
    private final GameRoundRepository gameRoundRepository;
    private final RoomPlayerRepository roomPlayerRepository;

    public GameLookup(
            RoomRepository roomRepository,
            GameRepository gameRepository,
            GameRoundRepository gameRoundRepository,
            RoomPlayerRepository roomPlayerRepository) {
        this.roomRepository = roomRepository;
        this.gameRepository = gameRepository;
        this.gameRoundRepository = gameRoundRepository;
        this.roomPlayerRepository = roomPlayerRepository;
    }

    public RoomEntity requireRoom(String roomCode) {
        return roomRepository.findById(roomCode)
                .orElseThrow(() -> new GameNotFoundException(ErrorMessage.GAME_NOT_FOUND.format(roomCode)));
    }

    public GameEntity requireGame(UUID gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(ErrorMessage.GAME_ID_NOT_FOUND.format(gameId)));
    }

    public Map<UUID, GameEntity> requireGames(List<GameRoundEntity> rounds) {
        List<UUID> gameIds = rounds.stream()
                .map(GameRoundEntity::getGameId)
                .distinct()
                .toList();

        Map<UUID, GameEntity> gamesById = new HashMap<>();

        gameRepository.findAllById(gameIds)
                .forEach(game -> gamesById.put(game.getGameId(), game));

        for (UUID gameId : gameIds) {
            if (!gamesById.containsKey(gameId)) {
                throw new GameNotFoundException(ErrorMessage.GAME_ID_NOT_FOUND.format(gameId));
            }
        }

        return gamesById;
    }

    public GameRoundEntity requireRound(GameEntity game) {
        return gameRoundRepository
                .findByRoomCodeAndRoundNo(game.getRoomCode(), game.getRoundNo())
                .orElseThrow(() -> new GameNotFoundException(ErrorMessage.GAME_ID_NOT_FOUND.format(game.getGameId())));
    }

    public GameEntity requireActiveGame(UUID gameId) {
        GameEntity game = requireGame(gameId);
        RoomEntity room = requireRoom(game.getRoomCode());

        if (!room.getActiveGameId().equals(gameId)) {
            throw new GameNotFoundException(ErrorMessage.GAME_ID_NOT_FOUND.format(gameId));
        }

        return game;
    }

    public List<RoomPlayerEntity> requireActiveRoomPlayers(String roomCode) {
        List<RoomPlayerEntity> players = roomPlayerRepository.findAllByRoomCode(roomCode);

        boolean hasInactivePlayer = players.stream()
                .filter(player -> PlayerType.PLAYER.name().equals(player.getPlayerType()))
                .anyMatch(player -> !player.isActive());

        if (hasInactivePlayer) {
            throw new RoomInactiveException(ErrorMessage.ROOM_INACTIVE.getMessage());
        }

        return players;
    }

    public RoomPlayerEntity requireRoomPlayerEntity(List<RoomPlayerEntity> players, String playerName) {
        String normalized = PlayerNameUtil.normalize(playerName);
        return requirePlayer(players,
                player -> normalized.equals(player.getNormalizedPlayerName()),
                () -> new PlayerNotFoundException(ErrorMessage.PLAYER_NOT_FOUND.format(playerName)));
    }

    public RoomPlayerEntity requireOpponent(List<RoomPlayerEntity> players, RoomPlayerEntity leavingMember) {
        List<RoomPlayerEntity> playersOnly = players.stream()
                .filter(player -> PlayerType.PLAYER.name().equals(player.getPlayerType()))
                .toList();

        return requirePlayer(playersOnly,
                player -> !player.getNormalizedPlayerName().equals(leavingMember.getNormalizedPlayerName()),
                () -> new PlayerNotFoundException(ErrorMessage.OPPONENT_OF_PLAYER_NOT_FOUND.format(leavingMember.getPlayerName())));
    }

    private RoomPlayerEntity requirePlayer(List<RoomPlayerEntity> players, Predicate<RoomPlayerEntity> matcher, Supplier<? extends ApiException> exceptionSupplier) {
        return players.stream()
                .filter(matcher)
                .findFirst()
                .orElseThrow(exceptionSupplier);
    }
}
