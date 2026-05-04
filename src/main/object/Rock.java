package main.object;

import java.awt.Color;
import java.awt.Graphics2D;

import main.core.GamePanel;

public class Rock extends GameObject {

    public Rock(int worldX, int worldY) {
        this.name = "Rock";
        this.worldX = worldX;
        this.worldY = worldY;
        this.collision = true;
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        if (!active) {
            return;
        }
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(worldX, worldY, gp.tileSize, gp.tileSize);
    }
}
