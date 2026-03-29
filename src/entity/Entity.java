package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

// abstract: Lớp trừu tượng, không thể khởi tạo trực tiếp, dùng làm bản vẽ chung
public abstract class Entity {
    public int x, y; // Tọa độ trên lưới
    public int speed; // Tốc độ di chuyển

    public BufferedImage image; // Hình ảnh hiển thị
    public String direction; // Hướng quay mặt (up, down, left, right)

    // Hitbox dùng để xét va chạm sau này
    public Rectangle solidArea = new Rectangle(0, 0, 48, 48);
    public boolean collisionOn = false;

    // Hai hàm trừu tượng bắt buộc các class con phải tự viết lại (Override)
    public abstract void update();
    public abstract void draw(Graphics2D g2);
}