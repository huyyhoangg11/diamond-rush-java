package main.entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public abstract class Entity {
    public String name;
    
    public int x, y;     // Tọa độ pixel (dùng để vẽ ra màn hình)
    public int col, row; // Tọa độ theo lưới (dùng để xử lý logic, xét va chạm)
    
    public int speed; // Tốc độ di chuyển (số pixel di chuyển mỗi frame)

    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2; // Chứa ảnh animation
    public String direction = "down"; // Hướng đứng mặc định

    // Hộp hitbox xét va chạm (tính theo pixel, thường nhỏ hơn kích thước TILE_SIZE)
    public Rectangle solidArea; 
    public int solidAreaDefaultX, solidAreaDefaultY;
    public boolean collisionOn = false;
    
    // Biến hỗ trợ Animation (Hoạt ảnh)
    public int spriteCounter = 0;
    public int spriteNum = 1;

    // Ép buộc các class con phải có 2 hàm này
    public abstract void update();
    public abstract void draw(Graphics2D g2);
} 