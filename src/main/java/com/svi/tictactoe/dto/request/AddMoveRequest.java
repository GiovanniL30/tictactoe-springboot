package com.svi.tictactoe.dto.request;

import com.svi.tictactoe.constants.Symbol;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record AddMoveRequest(
        @NotNull(message = "x is required.")
        @Range(min = 0, max = 2, message = "x must be between 0 and 2.")
        Integer x,

        @NotNull(message = "y is required.")
        @Range(min = 0, max = 2, message = "y must be between 0 and 2.")
        Integer y,

        @NotNull(message = "symbol is required.")
        Symbol symbol
) {
}
