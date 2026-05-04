package main.core;

import main.config.GameConfig;
import main.entity.Player;
import main.input.KeyHandler;
import main.map.MapLoader;
import main.object.Diamond;
import main.object.GameObject;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class GamePanel extends JPanel {

    // Sau này bạn sẽ khởi tạo MapLoader, Player, mảng Enemy, mảng Object ở đây
	
	public final int tileSize = GameConfig.TILE_SIZE;

	// Player
	KeyHandler keyH = new KeyHandler();
    Player player = new Player(this, keyH);
    public MapLoader mapLoader = new MapLoader(this);
    public GameObject[] objects = new GameObject[10];

	
    public GamePanel() {
        this.setPreferredSize(new Dimension(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true); // Tối ưu render
        this.setFocusable(true); // Để nhận thao tác bàn phím
        this.addKeyListener(keyH);

        setupObjects();
    }

    private void setupObjects() {
        objects[0] = new Diamond(6 * tileSize, 4 * tileSize);
        objects[1] = new Diamond(9 * tileSize, 8 * tileSize);
    }

    // Hàm này sẽ được GameLoop gọi 60 lần/giây để cập nhật tọa độ, logic
    public void update() {
        player.update();
        // enemy.update();
    }

    // Hàm này sẽ được GameLoop gọi 60 lần/giây để vẽ hình ảnh mới
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

        player.draw(g2);

        g2.dispose();
    }
}
