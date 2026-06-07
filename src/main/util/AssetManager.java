package main.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public final class AssetManager {

    public static BufferedImage wall;
    public static BufferedImage plastic;
    public static BufferedImage dirt;
    public static BufferedImage bush;
    public static BufferedImage diamond;
    public static BufferedImage diamondPre;
    public static BufferedImage hammer;
    public static BufferedImage key;
    public static BufferedImage rock;
    public static BufferedImage door;
    public static BufferedImage playerWalkRightHead;
    public static BufferedImage[] playerWalkRightBodies;
    public static BufferedImage playerWalkUpHead;
    public static BufferedImage[] playerWalkUpBodies;
    public static BufferedImage[] playerHammerFrames;
    public static BufferedImage[] playerSpawnFrames;
    public static BufferedImage[] playerDieFrames;
    public static BufferedImage snake;
    public static BufferedImage snake1;
    public static BufferedImage snake2;
    public static BufferedImage spawn;
    public static BufferedImage statue;
    public static BufferedImage fire1;
    public static BufferedImage fire2;
    public static BufferedImage fire3;
    public static BufferedImage tileLock;
    public static BufferedImage phat0;
    public static BufferedImage phat1;
    public static BufferedImage phat2;
    public static BufferedImage phat3;
    public static BufferedImage lock;
    public static BufferedImage lock1;
    public static BufferedImage lock2;
    public static BufferedImage lockPre;
    public static BufferedImage knob;
    public static BufferedImage uiPlayer;
    public static BufferedImage uiDiamond;
    public static BufferedImage uiMenu0;
    public static BufferedImage uiMenu1;

    private static boolean loaded;

    private AssetManager() {
    }

    public static void loadAssets() {
        if (loaded) {
            return;
        }

        wall = loadImage("/tiles/wall.png");
        plastic = loadImage("/tiles/plastic.png");
        dirt = loadImage("/tiles/dirt.png");
        bush = loadImage("/tiles/bush.png");
        diamond = loadImage("/objects/diamond.png");
        diamondPre = loadImage("/objects/diamondPre.png");
        hammer = loadImage("/objects/hammer.png");
        key = loadImage("/objects/key.png");
        rock = loadImage("/objects/rock.png");
        door = loadImage("/objects/door.png");
        playerWalkRightHead = loadImage("/characters/player/walk/player_walk_right_head.png");
        playerWalkRightBodies = new BufferedImage[] {
                loadImage("/characters/player/walk/player_walk_right_0_body.png"),
                loadImage("/characters/player/walk/player_walk_right_1_body.png"),
                loadImage("/characters/player/walk/player_walk_right_2_body.png")
        };
        playerWalkUpHead = loadImage("/characters/player/walk/player_walk_up_head.png");
        playerWalkUpBodies = new BufferedImage[] {
                loadImage("/characters/player/walk/player_walk_up_body_0.png"),
                loadImage("/characters/player/walk/player_walk_up_body_1.png")
        };
        playerHammerFrames = new BufferedImage[] {
                loadImage("/characters/player/hammer/player_hammer_0.png"),
                loadImage("/characters/player/hammer/player_hammer_1.png"),
                loadImage("/characters/player/hammer/player_hammer_2.png")
        };
        playerSpawnFrames = new BufferedImage[] {
                loadImage("/characters/player/spawn/player_spawn_0.png"),
                loadImage("/characters/player/spawn/player_spawn_1.png"),
                loadImage("/characters/player/spawn/player_spawn_2.png")
        };
        playerDieFrames = new BufferedImage[] {
                loadImage("/characters/player/die/player_die_0.png"),
                loadImage("/characters/player/die/player_die_1.png"),
                loadImage("/characters/player/die/player_die_2.png"),
                loadImage("/characters/player/die/player_die_3.png"),
                loadImage("/characters/player/die/player_die_4.png")
        };
        snake1 = loadImage("/objects/snake1.png");
        snake2 = loadImage("/objects/snake2.png");
        snake = snake1;
        statue = loadImage("/objects/statue.png");
        fire1 = loadImage("/objects/fire1.png");
        fire2 = loadImage("/objects/fire2.png");
        fire3 = loadImage("/objects/fire3.png");
        tileLock = loadImage("/tiles/lock.png");
        phat0 = loadImage("/tiles/phat_0.png");
        phat1 = loadImage("/tiles/phat_1.png");
        phat2 = loadImage("/tiles/phat_2.png");
        phat3 = loadImage("/tiles/phat_3.png");
        lock = loadImage("/objects/lock.png");
        lock1 = loadImage("/objects/lock1.png");
        lock2 = loadImage("/objects/lock2.png");
        lockPre = loadImage("/objects/lockPre.png");
        knob = loadImageOrDefault("/objects/knob.png", lock1);
        spawn = loadImageOrDefault("/objects/spawn.png", dirt);
        uiPlayer = loadImageOrDefault("/ui/player.png", playerWalkRightHead);
        uiDiamond = loadImageOrDefault("/ui/diamond.png", diamond);
        uiMenu0 = loadImageOrDefault("/ui/ui_0.png", dirt);
        uiMenu1 = loadImageOrDefault("/ui/ui_1.png", uiMenu0);

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
