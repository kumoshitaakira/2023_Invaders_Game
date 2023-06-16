public class Invader {
    private double x;
    private double y;
    private int health;

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
    public void move(double speed) {
        x += speed;
    }

    // 衝突時の体力の更新
    public void hit(int newHealth) {
        this.health = newHealth;
    }

    // 体力を返す
    public int getHealth() {
        return health;
    }
}
