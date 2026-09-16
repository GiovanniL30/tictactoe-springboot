package com.svi.tictactoe.service.support;

import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.entity.GameEntity;
import com.svi.tictactoe.entity.ParticipantEntity;
import com.svi.tictactoe.entity.PlayerCatalogEntity;
import com.svi.tictactoe.entity.PlayerGameEntity;
import com.svi.tictactoe.repository.cassandra.PlayerCatalogRepository;
import com.svi.tictactoe.repository.cassandra.PlayerGameRepository;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.svi.tictactoe.util.PlayerNameUtil.normalize;

@Component
public class PlayerGameSynchronizer {

    private final PlayerCatalogRepository playerCatalogRepository;
    private final PlayerGameRepository playerGameRepository;

    public PlayerGameSynchronizer(
            PlayerCatalogRepository playerCatalogRepository,
            PlayerGameRepository playerGameRepository) {
        this.playerCatalogRepository = playerCatalogRepository;
        this.playerGameRepository = playerGameRepository;
    }

    public void sync(GameEntity game, List<ParticipantEntity> participants) {
        participants.stream()
                .filter(participant -> PlayerType.PLAYER.name().equals(participant.getPlayerType()))
                .forEach(participant -> {
                    playerCatalogRepository.save(new PlayerCatalogEntity(
                            PlayerCatalogEntity.ALL_PLAYERS,
                            participant.getNormalizedPlayerName(),
                            participant.getPlayerName()
                    ));

                    playerGameRepository.save(new PlayerGameEntity(
                            participant.getNormalizedPlayerName(),
                            game.getGameId(),
                            game.getRoomCode(),
                            participant.getSymbol(),
                            game.getWinner() != null
                                    && normalize(game.getWinner()).equals(participant.getNormalizedPlayerName())
                    ));
                });
    }
}
