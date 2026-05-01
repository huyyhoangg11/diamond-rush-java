package main.entity;

/**
 * Lớp trừu tượng đại diện cho các thực thể "sống", có khả năng chuyển động chủ động và có chỉ số sinh mệnh (máu).
 * Áp dụng cho Main Player và Các loại Quái vật (Enemies).
 */
public abstract class Character extends Entity implements IDamageable {

    public int maxLife;
    public int life;
    
    // Thuộc tính hỗ trợ cho Grid-based movement
    public boolean isMoving = false; // Đang trong quá trình di chuyển từ ô này sang ô khác
    
    public Character() {
        super();
        this.solidAreaDefaultX = 0;
        this.solidAreaDefaultY = 0;
    }
}
