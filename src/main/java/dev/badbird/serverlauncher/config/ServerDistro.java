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
    //FABRIC;

    public Launcher getLauncher() {
        switch (this) {
            case PAPER: {
                return new PaperLauncher();
            }
            case MULTI_PAPER:
                return new MultiPaperLauncher();
            case PURPUR:
                return new PurpurLauncher();
            case CUSTOM:
                return new CustomLauncher();
        }
        return null;
    }
}
