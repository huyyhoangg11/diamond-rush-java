package main.core;

import main.config.GameConfig;
import main.entity.Player;
import main.entity.Snake;
import main.input.KeyHandler;
import main.map.MapLoader;
import java.awt.Rectangle;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class GamePanel extends JPanel {

    // Khởi tạo Hệ thống Bắt phím
    KeyHandler keyH = new KeyHandler();
    
    // Khởi tạo Nhân vật chính
    Player player;
    
    // Khởi tạo mảng Kẻ địch (Quái)
    Snake[] snakes;
    
    // Khởi tạo UI
    main.ui.UI ui;

    public GamePanel() {
        this.setPreferredSize(new Dimension(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true); // Tối ưu render
        this.setFocusable(true); // Để nhận thao tác bàn phím
        
        // Thêm bộ lắng nghe phím vào panel này
        this.addKeyListener(keyH);
        
        // Gọi hàm setup Môi trường Game lần đầu tiên
        resetGame();
    }
    
    // Hàm này dùng để Set Khởi tạo (hoặc Hồi sinh) cấu hình game
    public void resetGame() {
        player = new Player(keyH);
        snakes = new Snake[5];
        ui = new main.ui.UI(this);
        
        // Gắn rắn con vào bản đồ tại ô tọa độ tùy ý
        snakes[0] = new Snake(5, 5, true);  // Đi ngang
        snakes[1] = new Snake(10, 3, false); // Đi dọc
    }

    // Hàm này sẽ được GameLoop gọi 60 lần/giây để cập nhật tọa độ, logic
    public void update() {
        // Kiểm tra xem Nhân vật còn sống không, nếu máu <= 0 thì Reset Game
        if (player.life <= 0) {
            resetGame();
            return; // Tránh chạy update đè lên obj cũ vừa bị xóa trong 1 frame
        }

        player.update();
        
        for (int i = 0; i < snakes.length; i++) {
            if (snakes[i] != null) {
                snakes[i].update();
                
                // Xét va chạm đơn giản (Sử dụng hình thoi/hình chữ nhật)
                Rectangle pRect = new Rectangle(player.x, player.y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
                Rectangle sRect = new Rectangle(snakes[i].x, snakes[i].y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
                
                if (pRect.intersects(sRect) && !player.invincible) {
                    player.takeDamage(snakes[i].baseDamage);
                }
            }
        }
    }

    // Hàm này sẽ được GameLoop gọi 60 lần/giây để vẽ hình ảnh mới
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Vẽ Map đen
        MapLoader.getInstance().draw(g2);
        
        // Vẽ Kẻ địch
        for(Snake s : snakes) {
            if(s != null) s.draw(g2);
        }
        
        // Vẽ Nhân vật
        player.draw(g2);
        
        // Vẽ UI đè lên cùng
        ui.draw(g2, player);

        g2.dispose();
    }
}