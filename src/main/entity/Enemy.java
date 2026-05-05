package main.entity;

import java.awt.Graphics2D;

import main.core.GamePanel;
import main.util.AssetManager;

public class Enemy extends Entity {


    private final GamePanel gp;

    public Enemy(GamePanel gp, int x, int y) {
        this.gp = gp;
        this.x = x;
        this.y = y;
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.drawImage(AssetManager.snake, x, y, gp.tileSize, gp.tileSize, null);
    }
}
