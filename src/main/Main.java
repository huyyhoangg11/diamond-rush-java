package main;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Tắt game khi bấm dấu X
        window.setResizable(false);
        window.setTitle("Diamond Rush - OOP Project");

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack();

        window.setLocationRelativeTo(null); // Hiển thị giữa màn hình
        window.setVisible(true);

        gamePanel.startGameThread(); // Bật vòng lặp game lên
    }
}