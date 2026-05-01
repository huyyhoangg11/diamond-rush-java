package main.entity;

/**
 * Interface dành cho các vật thể có thể tương tác trên bản đồ (Ví dụ: Kim cương nhặt được, Cửa mở được).
 */
public interface IInteractable {
    /**
     * Hành vi tương tác của vật thể khi Player đụng trúng.
     * @param player Nhân vật chính va chạm với vật thể này.
     */
    void onInteract(Player player);
    
    /**
     * Cờ đánh dấu vật thể không còn sử dụng được nữa (đã nhặt/phá vỡ).
     * @return true nếu có thể xóa khỏi đồ thị map.
     */
    boolean isConsumed();
}
