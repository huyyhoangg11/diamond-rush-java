package main.object;

import java.awt.Color;
import java.awt.Graphics2D;

import main.core.GamePanel;

public class Door extends GameObject {

    public Door(int worldX, int worldY) {
        this.name = "Door";
        this.worldX = worldX;
        this.worldY = worldY;
        this.collision = true;
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        if (!active) {
            return;
        }
        g2.setColor(new Color(139, 69, 19));
        g2.fillRect(worldX, worldY, gp.tileSize, gp.tileSize);
    }
}
