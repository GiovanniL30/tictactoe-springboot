package com.svi.tictactoe.dto.response.error;

import java.util.List;

public record ValidationErrorResponse(
        int status,
        String message,
        List<String> errors
) {
}
