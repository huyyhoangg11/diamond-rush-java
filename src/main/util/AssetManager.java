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
    public static BufferedImage spawn;
    public static BufferedImage uiPlayer;
    public static BufferedImage uiDiamond;

    private static boolean loaded;

    private AssetManager() {
    }

    public static void loadAssets() {
        if (loaded) {
            return;
        }

        wall = loadImage("/tiles/wall.png");
        dirt = loadImage("/tiles/dirt.png");
        bush = loadImage("/tiles/bush.png");
        diamond = loadImage("/objects/diamond.png");
        rock = loadImage("/objects/rock.png");
        door = loadImage("/objects/door.png");
        playerDown = loadImage("/characters/player_down.png");
        snake = loadImage("/objects/snake.png");
        spawn = loadImageOrDefault("/objects/spawn.png", dirt);
        uiPlayer = loadImageOrDefault("/ui/player.png", playerDown);
        uiDiamond = loadImageOrDefault("/ui/diamond.png", diamond);

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

    private static BufferedImage loadImageOrDefault(String path, BufferedImage defaultImage) {
        try (InputStream inputStream = AssetManager.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                return defaultImage;
            }
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load asset: " + path, e);
        }
    }
}
