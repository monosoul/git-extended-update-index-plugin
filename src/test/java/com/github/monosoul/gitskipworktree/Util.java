package com.github.monosoul.gitskipworktree;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import lombok.val;

public final class Util {

    private Util() {
    }

    public static SkipWorkTreeCommand getRandomSkipWorkTreeCommand() {
        val values = SkipWorkTreeCommand.values();

        return values[nextInt(0, values.length)];
    }
}
