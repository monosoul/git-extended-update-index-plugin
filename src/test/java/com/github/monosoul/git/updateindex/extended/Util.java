package com.github.monosoul.git.updateindex.extended;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import lombok.val;

public final class Util {

    private Util() {
    }

    public static ExtendedUpdateIndexCommand getRandomSkipWorkTreeCommand() {
        val values = ExtendedUpdateIndexCommand.values();

        return values[nextInt(0, values.length)];
    }
}
