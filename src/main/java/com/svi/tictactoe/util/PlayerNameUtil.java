package com.svi.tictactoe.util;

import java.util.Locale;

public final class PlayerNameUtil {

    private PlayerNameUtil() {
    }

    public static String normalize(String playerName) {
        return playerName.trim().toLowerCase(Locale.ROOT);
    }
}
