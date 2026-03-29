package main;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class GamePanel extends JPanel implements Runnable {
    // 1. CẤU HÌNH MÀN HÌNH (Grid system)
    final int originalTileSize = 16; // 16x16 pixels
    final int scale = 3;
    public final int tileSize = originalTileSize * scale; // 48x48 pixels (Kích thước 1 ô lưới)

    public final int maxScreenCol = 16;
    public final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol; // 768 pixels
    public final int screenHeight = tileSize * maxScreenRow; // 576 pixels

    // 2. BIẾN HỆ THỐNG
    int FPS = 60; // Khóa game ở 60 khung hình/giây
    Thread gameThread;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true); // Tối ưu hóa render hình ảnh
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // 3. VÒNG LẶP GAME (GAME LOOP) - Quản lý FPS ổn định
    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS; // Thời gian 1 frame tính bằng nanosecond
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update(); // Bước 1: Cập nhật thông tin (tọa độ, máu...)
                repaint(); // Bước 2: Vẽ lại màn hình
                delta--;
            }
        }
    }

    // Nơi bạn sẽ gọi các hàm update của nhân vật, quái vật...
    public void update() {
        // Ví dụ sau này: player.update();
    }

    // Nơi bạn sẽ vẽ đồ họa ra màn hình
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Vẽ thử 1 ô màu trắng để test màn hình (Sau này xóa đi)
        g2.setColor(Color.WHITE);
        g2.fillRect(100, 100, tileSize, tileSize);

        g2.dispose(); // Giải phóng bộ nhớ
    }
}