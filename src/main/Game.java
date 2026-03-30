package main;

import main.core.GamePanel;
import main.core.GameLoop;
import javax.swing.JFrame;

public class Game {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Diamond Rush");

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack(); // Ép cửa sổ vừa khít với GamePanel

        window.setLocationRelativeTo(null); // Giữa màn hình
        window.setVisible(true);

        GameLoop gameLoop = new GameLoop(gamePanel);
        gameLoop.start(); // Kích hoạt động cơ
    }
}