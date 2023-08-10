package dev.badbird.serverlauncher.config;

import dev.badbird.serverlauncher.launcher.Launcher;
import dev.badbird.serverlauncher.launcher.impl.*;

public enum ServerDistro {
    PAPER, MULTI_PAPER,
    MULTIPAPER_MASTER,
    PURPUR, WATERFALL,
    CUSTOM;

    public Launcher getLauncher() {
        switch (this) {
            case PAPER: return new PaperLauncher();
            case MULTI_PAPER: return new MultiPaperLauncher();
            case MULTIPAPER_MASTER: return new MultiPaperMasterLauncher();
            case PURPUR: return new PurpurLauncher();
            case WATERFALL: return new WaterfallLauncher();
            case CUSTOM: return new CustomLauncher();
        }
        return null;
    }
}
