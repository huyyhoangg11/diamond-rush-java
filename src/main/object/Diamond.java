package main.object;

import java.awt.Graphics2D;

import main.core.GamePanel;
import main.util.AssetManager;

public class Diamond extends GameObject {

    public Diamond(int worldX, int worldY) {
        this.name = "Diamond";
        this.worldX = worldX;
        this.worldY = worldY;
        this.collision = false;
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        if (!active) {
            return;
        }
        g2.drawImage(AssetManager.diamond, worldX, worldY, gp.tileSize, gp.tileSize, null);
    }
}
