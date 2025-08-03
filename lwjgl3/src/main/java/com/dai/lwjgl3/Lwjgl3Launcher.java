package com.dai.lwjgl3;

import java.util.Arrays;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.dai.DAIGame;
import com.dai.DAIServer;
import com.dai.world.World;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {

        if(Arrays.asList(args).contains("--server")) {
            try {
                DAIServer server = new DAIServer();
                server.run();
            } catch(Exception e) {
                // TODO: Maybe handle this in a better way
                e.printStackTrace();
            }
        } else {
            if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
            createApplication(args);
        }
    }

    private static Lwjgl3Application createApplication(String[] args) {
        return new Lwjgl3Application(new DAIGame(Arrays.asList(args).contains("--offline")), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Dungeons & AI");
        //// Vsync limits the frames per second to what your hardware can display, and helps eliminate
        //// screen tearing. This setting doesn't always work on Linux, so the line after is a safeguard.
        configuration.useVsync(true);
        //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to try to match fractional
        //// refresh rates. The Vsync setting above should limit the actual FPS to match the monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.

        // configuration.setWindowedMode(800, 600);
        configuration.setWindowedMode(
                        (int)((World.WORLD_SIZE * World.TILE_SIZE) / World.CAMERA_ZOOM) + 500,
                        (int)((World.WORLD_SIZE * World.TILE_SIZE) / World.CAMERA_ZOOM) + 150);
        //// You can change these files; they are in lwjgl3/src/main/resources/ .
        //// They can also be loaded from the root of assets/ .
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}
