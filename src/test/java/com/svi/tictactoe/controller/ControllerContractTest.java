package com.svi.tictactoe.controller;

import com.svi.tictactoe.constants.PlayerType;
import com.svi.tictactoe.constants.Symbol;
import com.svi.tictactoe.dto.request.CreateRoomRequest;
import com.svi.tictactoe.dto.response.room.CreateRoomResponse;
import com.svi.tictactoe.dto.response.player.PlayerResponse;
import com.svi.tictactoe.exception.GameNotFoundException;
import com.svi.tictactoe.exception.GlobalExceptionHandler;
import com.svi.tictactoe.service.GameService;
import com.svi.tictactoe.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ControllerContractTest {

    private RoomService roomService;
    private GameService gameService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        roomService = mock(RoomService.class);
        gameService = mock(GameService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = standaloneSetup(
                new RoomController(roomService),
                new GameController(gameService)
        )
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createsRoomWithCreatedStatusAndResponseJson() throws Exception {
        UUID gameId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Instant createdAt = Instant.parse("2026-09-18T00:00:00Z");
        CreateRoomResponse response = new CreateRoomResponse(
                "Game created successfully.",
                "ROOM",
                gameId,
                createdAt,
                new PlayerResponse("Alice", 0, Symbol.X, PlayerType.PLAYER, createdAt)
        );
        when(roomService.createRoom(any(CreateRoomRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerName\":\"Alice\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomCode").value("ROOM"))
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
                .andExpect(jsonPath("$.player.symbol").value("X"));
    }

    @Test
    void returnsConsistentValidationJsonForBlankPlayerName() throws Exception {
        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerName\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed."))
                .andExpect(jsonPath("$.errors[0]").value("playerName is required."));

        verify(roomService, never()).createRoom(any(CreateRoomRequest.class));
    }

    @Test
    void returnsDomainErrorWithConfiguredHttpStatus() throws Exception {
        UUID gameId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        when(gameService.getGame(gameId)).thenThrow(new GameNotFoundException("Game was not found."));

        mockMvc.perform(get("/api/v1/games/{gameId}", gameId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Game was not found."));
    }

    @Test
    void returnsConsistentJsonForInvalidGameId() throws Exception {
        mockMvc.perform(get("/api/v1/games/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Path variable 'gameId' has an invalid value."));
    }

    @Test
    void returnsConsistentJsonForInvalidSymbol() throws Exception {
        UUID gameId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        mockMvc.perform(post("/api/v1/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"x\":0,\"y\":0,\"symbol\":\"Z\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("symbol must be either X or O."));
    }
}
