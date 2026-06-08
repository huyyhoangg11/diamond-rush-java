package main.object;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import main.core.GamePanel;
import main.util.AssetManager;

public class PhatTile extends GameObject {

    private final int variant;

    public PhatTile(int worldX, int worldY, int variant) {
        this.name = "PhatTile";
        this.worldX = worldX;
        this.worldY = worldY;
        this.variant = variant;
        this.collision = true;
    }

    public int getVariant() {
        return variant;
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        if (!active) {
            return;
        }
        g2.drawImage(getImage(), worldX, worldY, gp.tileSize, gp.tileSize, null);
    }

    private BufferedImage getImage() {
        return switch (variant) {
            case 1 -> AssetManager.phat1;
            case 2 -> AssetManager.phat2;
            case 3 -> AssetManager.phat3;
            default -> AssetManager.phat0;
        };
    }
}
