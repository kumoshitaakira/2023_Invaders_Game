public class Invader {
    private double x;
    private double y;
    private int health;

    public Invader(double x, double y, int health) {
        this.x = x;
        this.y = y;
        this.health = health;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void move(double speed) {
        x += speed;
    }

    public void hit(int newHealth) {
        this.health = newHealth;
    }

    public int getHealth() {
        return health;
    }
}
