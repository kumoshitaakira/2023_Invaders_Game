import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Invader {
    private double x;
    private double y;
    private int health;
    private boolean alternateImage = false;
    private int currentFrame = 0; // フレーム数

    // x、y座標と体力を持ったインスタンスを生成
    public Invader(double x, double y, int health) {
        this.x = x;
        this.y = y;
        this.health = health;
    }

    // x座標を返す
    public double getX() {
        return x;
    }

    // x座標を更新
    public void setX(double x) {
        this.x = x;
    }

    // y座標を返す
    public double getY() {
        return y;
    }

    // y座標を更新
    public void setY(double y) {
        this.y = y;
    }

    // x座標を右に動かすメソッド
    public void moveX(double speed) {
        x += speed;
    }

    // y座標を下に動かすメソッド
    public void moveY(double speed) {
        y += speed;
    }

    // 衝突時の体力の更新
    public void hit(int newHealth) {
        this.health = newHealth;
    }

    // 体力を返す
    public int getHealth() {
        return health;
    }

    // 画像を切り替える
    public void updateFrame(GraphicsContext gc) {
        currentFrame++;

        if (currentFrame % 6 == (0 | 1 | 2)) {
            alternateImage = !alternateImage;
        }
        Image alienImage = new Image("alien.png");
        double imageWidth = alienImage.getWidth() / 2;
        double imageHeight = alienImage.getHeight();
        double destWidth = 50;
        double destHeight = 25;
        if (alternateImage) {
            gc.drawImage(alienImage, 0, 0, imageWidth, imageHeight,
                    getX() - 25, getY() - 25, destWidth, destHeight);
        } else {
            gc.drawImage(alienImage, imageWidth, 0, imageWidth, imageHeight,
                    getX() - 25, getY() - 25, destWidth, destHeight);
        }

    }
}
