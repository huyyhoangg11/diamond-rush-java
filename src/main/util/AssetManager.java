package main.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public final class AssetManager {

    public static BufferedImage wall;
    public static BufferedImage dirt;
    public static BufferedImage bush;
    public static BufferedImage diamond;
    public static BufferedImage rock;
    public static BufferedImage door;
    public static BufferedImage playerDown;
    public static BufferedImage snake;

    private static boolean loaded;

    private AssetManager() {
    }

    public static void loadAssets() {
        if (loaded) {
            return;
        }

        wall = loadImage("/sprites/wall.png");
        dirt = loadImage("/sprites/dirt.png");
        bush = loadImage("/sprites/bush.png");
        diamond = loadImage("/sprites/diamond.png");
        rock = loadImage("/sprites/rock.png");
        door = loadImage("/sprites/door.png");
        playerDown = loadImage("/sprites/player_down.png");
        snake = loadImage("/sprites/snake.png");

        loaded = true;
    }

    private static BufferedImage loadImage(String path) {
        try (InputStream inputStream = AssetManager.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("Asset not found: " + path);
            }
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load asset: " + path, e);
        }
    }
}
