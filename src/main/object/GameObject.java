package main.object;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import main.core.GamePanel;

public abstract class GameObject {
    public BufferedImage image;
    public String name;
    public boolean collision = false; // Có đi xuyên qua được không?
    public int worldX, worldY;
    public Rectangle solidArea = new Rectangle(0, 0, 48, 48);

    public abstract void draw(Graphics2D g2, GamePanel gp);
}