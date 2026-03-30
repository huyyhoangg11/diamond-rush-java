package main.core;

import main.config.GameConfig;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class GamePanel extends JPanel {

    // Sau này bạn sẽ khởi tạo MapLoader, Player, mảng Enemy, mảng Object ở đây

    public GamePanel() {
        this.setPreferredSize(new Dimension(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true); // Tối ưu render
        this.setFocusable(true); // Để nhận thao tác bàn phím
    }

    // Hàm này sẽ được GameLoop gọi 60 lần/giây để cập nhật tọa độ, logic
    public void update() {
        // player.update();
        // enemy.update();
    }

    // Hàm này sẽ được GameLoop gọi 60 lần/giây để vẽ hình ảnh mới
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // mapLoader.draw(g2);
        // player.draw(g2);

        g2.dispose();
    }
}