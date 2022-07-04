package dev.badbird.serverlauncher.config;

import dev.badbird.serverlauncher.launcher.Launcher;
import dev.badbird.serverlauncher.launcher.impl.MultiPaperLauncher;
import dev.badbird.serverlauncher.launcher.impl.PaperLauncher;

public enum ServerDistro {
    PAPER,
    MULTI_PAPER;
    //FABRIC;

    public Launcher getLauncher() {
        switch (this) {
            case PAPER: {
                return new PaperLauncher();
            }
            case MULTI_PAPER:
                return new MultiPaperLauncher();
        }
        return null;
    }
}
