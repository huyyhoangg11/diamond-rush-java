package main.core;

import main.config.GameConfig;
import main.entity.Enemy;
import main.entity.Player;
import main.input.KeyHandler;
import main.map.MapLoader;
import main.object.Diamond;
import main.object.GameObject;
import main.util.AssetManager;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class GamePanel extends JPanel {

    public final int tileSize = GameConfig.TILE_SIZE;

    KeyHandler keyH = new KeyHandler();
    Player player;
    Enemy enemy;
    public MapLoader mapLoader;
    public GameObject[] objects = new GameObject[10];

    public GamePanel() {
        AssetManager.loadAssets();

        this.setPreferredSize(new Dimension(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(keyH);

        player = new Player(this, keyH);
        enemy = new Enemy(this, 12 * tileSize, 8 * tileSize);
        mapLoader = new MapLoader(this);

        setupObjects();
    }

    private void setupObjects() {
        objects[0] = new Diamond(6 * tileSize, 4 * tileSize);
        objects[1] = new Diamond(9 * tileSize, 8 * tileSize);
    }

    public void update() {
        player.update();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        mapLoader.draw(g2);

        for (GameObject object : objects) {
            if (object != null && object.isActive()) {
                object.draw(g2, this);
            }
        }

        enemy.draw(g2);
        player.draw(g2);

        g2.dispose();
    }
}
