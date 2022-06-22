package dev.badbird.serverlauncher.config;

import dev.badbird.serverlauncher.launcher.Launcher;
import dev.badbird.serverlauncher.launcher.impl.PaperLauncher;

public enum ServerDistro {
    PAPER;
    //FABRIC;

    public Launcher getLauncher() {
        switch (this) {
            case PAPER: {
                return new PaperLauncher();
            }
        }
        return null;
    }
}
