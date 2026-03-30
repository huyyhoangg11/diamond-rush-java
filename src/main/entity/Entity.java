package main.entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public abstract class Entity {
    public int x, y; // Tọa độ
    public int speed; // Tốc độ di chuyển

    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2; // Chứa ảnh animation
    public String direction;

    public Rectangle solidArea; // Hộp hitbox xét va chạm
    public boolean collisionOn = false;

    // Ép buộc các class con phải có 2 hàm này
    public abstract void update();
    public abstract void draw(Graphics2D g2);
}