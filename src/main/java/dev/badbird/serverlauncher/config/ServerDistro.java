package dev.badbird.serverlauncher.config;

import dev.badbird.serverlauncher.launcher.Launcher;
import dev.badbird.serverlauncher.launcher.impl.CustomLauncher;
import dev.badbird.serverlauncher.launcher.impl.MultiPaperLauncher;
import dev.badbird.serverlauncher.launcher.impl.PaperLauncher;
import dev.badbird.serverlauncher.launcher.impl.PurpurLauncher;

public enum ServerDistro {

    PAPER,
    MULTI_PAPER,
    PURPUR,
    CUSTOM;

    public Launcher getLauncher() {
        return switch (this) {
            case PAPER -> new PaperLauncher();
            case MULTI_PAPER -> new MultiPaperLauncher();
            case PURPUR -> new PurpurLauncher();
            case CUSTOM -> new CustomLauncher();
        };
    }
}
