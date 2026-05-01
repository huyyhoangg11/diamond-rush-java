package main.entity;

/**
 * Lớp trừu tượng đại diện cho các vật thể tĩnh / vô tri trong game.
 * Ví dụ: Cửa, Kim cương, Chìa khóa, Tảng đá.
 */
public abstract class GameObject extends Entity {
    
    public GameObject() {
        super();
        // Đặc thù của vật thể vô tri là thường có solidArea lấp đầy nguyên ô (48x48)
    }
    
    // GameObject thường sẽ không tự update phức tạp như Character nên có thể để mặc định rỗng 
    // hoặc cho phép override.
    @Override
    public void update() {
        // Tảng đá (Boulder) sẽ override hàm này để tự rơi xuống nếu phía dưới trống.
    }
}
