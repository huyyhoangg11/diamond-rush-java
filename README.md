# Diamond Rush Java

**Diamond Rush Java** là một game phiêu lưu - giải đố 2D lấy cảm hứng từ dòng game **Diamond Rush** cổ điển. Người chơi điều khiển nhà thám hiểm di chuyển qua các khu mỏ, thu thập kim cương, đẩy đá, sử dụng vật phẩm hỗ trợ, tránh bẫy và quái vật, sau đó tìm cửa thoát để hoàn thành từng màn chơi.

Dự án được viết bằng Java thuần, sử dụng **Java Swing/AWT** để tạo cửa sổ game, xử lý input bàn phím, vẽ bản đồ dạng tile, quản lý trạng thái game và phát âm thanh.

---

## Mục lục

* [Công nghệ và thư viện sử dụng](#công-nghệ-và-thư-viện-sử-dụng)
* [Tính năng chính](#tính-năng-chính)
* [Cấu trúc dự án](#cấu-trúc-dự-án)
* [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
* [Cách tải và chạy game](#cách-tải-và-chạy-game)
* [Cách build file JAR](#cách-build-file-jar)
* [Hướng dẫn điều khiển](#hướng-dẫn-điều-khiển)
* [Luật chơi cơ bản](#luật-chơi-cơ-bản)
* [Báo cáo và tài liệu](#báo-cáo-và-tài-liệu)
* [Hạn chế hiện tại](#hạn-chế-hiện-tại)
* [Hướng phát triển](#hướng-phát-triển)

---

## Công nghệ và thư viện sử dụng

| Nhóm             | Công nghệ / thư viện                              | Vai trò                                                |
| ---------------- | ------------------------------------------------- | ------------------------------------------------------ |
| Ngôn ngữ         | Java                                              | Xây dựng toàn bộ logic game                            |
| Giao diện        | Java Swing (`JFrame`, `JPanel`)                   | Tạo cửa sổ game và panel hiển thị                      |
| Đồ họa           | Java AWT / Java2D (`Graphics2D`, `BufferedImage`) | Vẽ map, nhân vật, vật phẩm, hiệu ứng và UI             |
| Âm thanh         | Java Sound API (`Clip`, `AudioSystem`)            | Phát nhạc nền và hiệu ứng âm thanh `.wav`              |
| Dữ liệu map      | CSV                                               | Lưu dữ liệu tile nền và object của từng màn chơi       |
| Asset            | PNG, WAV                                          | Hình ảnh tile, nhân vật, object, giao diện và âm thanh |
| Build            | IntelliJ IDEA / JDK CLI                           | Biên dịch source code và đóng gói file `.jar`          |
| Quản lý mã nguồn | Git, GitHub                                       | Lưu trữ source code, báo cáo và bản phát hành          |

Dự án hiện không sử dụng Maven hoặc Gradle, vì vậy có thể chạy và build trực tiếp bằng IntelliJ IDEA hoặc các lệnh có sẵn trong JDK.

---

## Tính năng chính

* **Gameplay dạng ô lưới 2D**: nhân vật di chuyển từng ô bằng WASD hoặc phím mũi tên.
* **Hệ thống nhiều màn chơi**: game đọc dữ liệu map từ các file CSV trong thư mục `res/maps`.
* **World Map**: cho phép chọn màn chơi, mở khóa màn mới và theo dõi tiến trình.
* **Lưu và tiếp tục tiến trình**: hỗ trợ chức năng Continue bằng file save trong thư mục người dùng.
* **Thu thập kim cương**: người chơi nhặt kim cương để tăng điểm và hoàn thành mục tiêu màn.
* **Đá và cơ chế vật lý đơn giản**: đá có thể chặn đường, được đẩy, rơi hoặc trượt trong một số tình huống.
* **Búa**: dùng phím `F` sau khi nhặt búa để phá vật cản hoặc tác động lên enemy ở ô kề bên.
* **Chìa khóa và khóa đặc biệt**: người chơi có thể nhặt chìa khóa để mở một số loại khóa.
* **Công tắc và khóa chốt**: đá hoặc người chơi có thể kích hoạt công tắc để mở đường.
* **Enemy và bẫy**: có rắn, tượng phun lửa, boss/rắn đặc biệt và các vùng nguy hiểm.
* **Checkpoint**: cho phép quay về điểm spawn/checkpoint gần nhất bằng phím `R`.
* **Giao diện đầy đủ**: gồm menu chính, hướng dẫn chơi, cốt truyện, pause, game over, win screen và HUD trong game.
* **Âm thanh**: có nhạc nền menu/stage và hiệu ứng âm thanh khi di chuyển, nhặt kim cương, đẩy đá, mất mạng, chọn menu và qua màn.

---

## Cấu trúc dự án

```text
diamond-rush-java/
├── README.md
├── docs/
│   └── Project_OOP_Game_Diamond_Rush_2D.pdf
├── src/
│   └── main/
│       ├── Game.java
│       ├── config/
│       ├── core/
│       ├── entity/
│       ├── input/
│       ├── map/
│       ├── object/
│       ├── ui/
│       └── util/
└── res/
    ├── maps/
    ├── tiles/
    ├── objects/
    ├── characters/
    ├── ui/
    └── sounds/
```

Trong đó:

* `src/main/Game.java`: điểm bắt đầu chương trình, tạo cửa sổ game và khởi động game.
* `config/`: chứa cấu hình kích thước màn hình, tile, FPS và các hằng số.
* `core/`: chứa các lớp lõi như game panel, game loop và logic gameplay chính.
* `entity/`: chứa các thực thể động như người chơi và enemy.
* `input/`: xử lý input từ bàn phím.
* `map/`: đọc và quản lý dữ liệu bản đồ từ file CSV.
* `object/`: chứa các object trong game như đá, kim cương, cửa, búa, chìa khóa, khóa, công tắc.
* `ui/`: chứa menu, world map, pause screen, game over, win screen, HUD và âm thanh giao diện.
* `util/`: chứa các lớp tiện ích như quản lý tài nguyên.
* `res/`: chứa toàn bộ ảnh, map và âm thanh của game.
* `docs/`: chứa báo cáo và tài liệu liên quan đến bài tập lớn.

---

## Yêu cầu hệ thống

Để chạy game, máy cần có:

* **Java 16 trở lên**.
* Hệ điều hành có giao diện đồ họa: Windows, macOS hoặc Linux desktop.
* Loa hoặc tai nghe nếu muốn nghe âm thanh.

Kiểm tra phiên bản Java:

```bash
java -version
```

Nếu phiên bản Java thấp hơn 16, cần cài Java 16 hoặc mới hơn trước khi chạy game.

---

## Cách tải và chạy game

### Cách 1: Tải file JAR từ GitHub release

Người chơi không cần tải toàn bộ source code. Chỉ cần tải file `.jar` trong mục **release** của repository.

Sau khi tải file, mở terminal tại thư mục chứa file `.jar` và chạy:

```bash
java -jar DiamondRush.jar
```

### Cách 2: Chạy từ source code

Clone repository:

```bash
git clone <repository-url>
cd diamond-rush-java
```

Trên Windows PowerShell:

```powershell
New-Item -ItemType Directory -Force out
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp "out;res" main.Game
```

Trên macOS/Linux/Git Bash:

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out:res main.Game
```

Lưu ý: cần thêm `res` vào classpath vì game sử dụng ảnh, âm thanh và map từ thư mục tài nguyên.

---

## Cách build file JAR

### Cách 1: Build bằng IntelliJ IDEA

1. Mở project bằng IntelliJ IDEA.
2. Vào `File` → `Project Structure`.
3. Chọn `Project`.
4. Đặt:

  * `SDK`: JDK 16 hoặc mới hơn.
  * `Language level`: 16.
5. Vào `Artifacts`.
6. Tạo artifact dạng:

  * `JAR` → `From modules with dependencies`.
7. Chọn main class là:

```text
main.Game
```

8. Bấm `Apply` → `OK`.
9. Chọn:

```text
Build → Build Artifacts... → Rebuild
```

File JAR sau khi build thường nằm trong:

```text
out/artifacts/Project_OOP_jar/
```

Có thể đổi tên file thành:

```text
DiamondRush2D.jar
```

rồi chạy thử:

```bash
java -jar DiamondRush2D.jar
```

### Cách 2: Build bằng terminal

Trên Windows PowerShell:

```powershell
Remove-Item -Recurse -Force out -ErrorAction SilentlyContinue
Remove-Item -Force DiamondRush2D.jar -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force out
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
jar --create --file DiamondRush2D.jar --main-class main.Game -C out . -C res .
java -jar DiamondRush2D.jar
```

Trên macOS/Linux/Git Bash:

```bash
rm -rf out DiamondRush.jar
mkdir -p out
javac -d out $(find src -name "*.java")
jar --create --file DiamondRush2D.jar --main-class main.Game -C out . -C res .
java -jar DiamondRush2D.jar
```

Lưu ý: thư mục `out/` là thư mục build tạm và thường được đưa vào `.gitignore`. Không cần push thư mục này lên GitHub. File `.jar` nên được upload vào mục **GitHub Releases**.

---

## Hướng dẫn điều khiển

| Phím      | Chức năng                                                                    |
| --------- | ---------------------------------------------------------------------------- |
| `W` / `↑` | Di chuyển lên                                                                |
| `S` / `↓` | Di chuyển xuống                                                              |
| `A` / `←` | Di chuyển sang trái                                                          |
| `D` / `→` | Di chuyển sang phải                                                          |
| `Enter`   | Chọn menu, xác nhận, chuyển trang hướng dẫn hoặc quay lại sau khi thắng/thua |
| `P`       | Tạm dừng / tiếp tục game                                                     |
| `Esc`     | Tạm dừng khi đang chơi hoặc quay lại ở một số màn hình                       |
| `Q`       | Lưu và quay về menu                                                          |
| `R`       | Reset về checkpoint/spawn gần nhất                                           |
| `F`       | Dùng búa sau khi đã nhặt được búa                                            |

### Điều khiển theo màn hình

* **Menu chính**: dùng `W/S` hoặc `↑/↓` để chọn, `Enter` để xác nhận.
* **World Map**: dùng phím mũi tên hoặc WASD để chọn màn, `Enter` để vào màn, `Esc` để quay lại menu.
* **How To Play**: dùng `Enter` hoặc phím điều hướng để đổi trang, `Esc` để quay lại menu.
* **Trong màn chơi**: dùng WASD/phím mũi tên để di chuyển, `F` dùng búa, `P` hoặc `Esc` để pause, `R` reset checkpoint, `Q` lưu và quay về menu.
* **Pause Screen**: dùng `W/S` hoặc `↑/↓` để chọn Continue/Reset/Quit, `Enter` để xác nhận.

---

## Luật chơi cơ bản

1. Di chuyển nhân vật qua khu mỏ để thu thập kim cương.
2. Tránh rắn, lửa, đá rơi và các vật cản nguy hiểm.
3. Đẩy đá để mở đường, kích hoạt công tắc hoặc tiêu diệt enemy.
4. Nhặt búa để phá bụi/cụm nhựa và làm choáng enemy ở gần.
5. Nhặt chìa khóa để mở khóa đặc biệt.
6. Khi đủ điều kiện, đi vào cửa hoặc nhặt kim cương kết thúc màn để hoàn thành level.
7. Nếu gặp nguy hiểm, dùng `R` để quay về checkpoint/spawn gần nhất.
8. Nếu mất hết mạng, game chuyển sang màn hình Game Over.

---

## Báo cáo và tài liệu

Báo cáo bài tập lớn của nhóm được đặt trong thư mục `docs/`.

```text
docs/
└── Project_OOP_Game_Diamond_Rush_2D.pdf
```

Báo cáo trình bày các nội dung chính:

* Thông tin chung về đề tài.
* Danh sách thành viên nhóm.
* Phân công công việc và đánh giá đóng góp.
* Tóm tắt kết quả đạt được.
* Các chức năng chính đã hoàn thành.
* Cấu trúc mã nguồn.
* Liên hệ và vận dụng các kiến thức OOP.
* Sơ đồ use case.
* Sơ đồ lớp.
* Hạn chế và hướng phát triển.
* Phụ lục hướng dẫn chạy chương trình.

---

## Hạn chế hiện tại

* Số lượng màn chơi còn ít hơn game Diamond Rush gốc.
* Chưa có đầy đủ các thế giới, boss, vật phẩm và cơ chế đặc biệt như bản gốc.
* Animation và hiệu ứng hình ảnh còn đơn giản.
* Hệ thống âm thanh mới ở mức cơ bản.
* Chưa có trình chỉnh sửa map trực quan; map hiện được tạo/chỉnh bằng file CSV.
* Chưa có màn hình tùy chỉnh cấu hình như âm lượng, độ phân giải hoặc key binding.
* Save game còn đơn giản, lưu bằng file properties trong thư mục người dùng.
* Chưa có bản đóng gói native riêng cho Windows/macOS/Linux; hiện chủ yếu chạy qua file `.jar`.

---

## Hướng phát triển

Trong tương lai, dự án có thể được phát triển thêm theo các hướng sau:

* Thêm nhiều màn chơi mới với độ khó tăng dần.
* Bổ sung thêm thế giới mới như rừng, băng, dung nham, đền cổ hoặc hang boss.
* Thêm enemy/boss mới với hành vi phức tạp hơn.
* Hoàn thiện hệ thống vật phẩm như khiên, thuốc hồi mạng, bom, cổng dịch chuyển hoặc chìa khóa theo màu.
* Tách các phần xử lý trong `GamePanel` thành các lớp quản lý riêng như `LevelManager`, `ObjectManager`, `EnemyManager`, `CollisionManager` và `SaveManager`.
* Cải thiện OOP bằng cách giảm việc sử dụng nhiều `instanceof`, tăng tính đóng gói và tách trách nhiệm rõ ràng hơn.
* Làm editor tạo map trực quan thay vì chỉnh CSV thủ công.
* Thêm menu cài đặt âm lượng, toàn màn hình, độ phân giải và đổi phím điều khiển.
* Cải thiện animation, particle effect, transition giữa màn và hiệu ứng thắng/thua.
* Thêm hệ thống thành tích, điểm số cao và thời gian hoàn thành level.
* Đóng gói bản phát hành bằng `jpackage` để tạo app native cho từng hệ điều hành.

---

## Ghi chú phát triển map

Mỗi màn chơi nên có 2 file trong thư mục `res/maps/`:

```text
mapXX_background.csv
mapXX_objects.csv
```

Trong đó:

* `mapXX_background.csv`: lưu dữ liệu tile nền.
* `mapXX_objects.csv`: lưu dữ liệu object, vật phẩm, enemy và chướng ngại vật.

Tên `XX` nên đánh số 2 chữ số, ví dụ:

```text
map01
map02
map03
```

Khi thêm đủ cả hai file background và object, game có thể tự phát hiện màn mới trong giới hạn quét hiện tại.

---

## Trạng thái dự án

Dự án hiện đang ở mức **playable prototype**. Game có thể chạy, chơi qua các màn, lưu tiến trình và thử nghiệm các cơ chế chính như di chuyển, nhặt kim cương, đẩy đá, dùng búa, enemy, bẫy, checkpoint, điều kiện thắng/thua và giao diện menu.

Source code, báo cáo và bản phát hành JAR được lưu trữ trên GitHub để phục vụ việc nộp bài tập lớn môn Lập trình hướng đối tượng.
