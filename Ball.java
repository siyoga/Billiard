import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.Ellipse2D;

public class Ball {
    private double x;
    private double y;
    private double radius = 10;
    private double mass = 1;
    private Speed speed;


    public static final Color[] colors = {new Color(204, 0, 0), new Color(52, 101, 164),
            new Color(237, 212, 0), new Color(245, 121, 0),
            new Color(115, 210, 22), new Color(117, 80, 123),
            new Color(85, 87, 83), new Color(193, 125, 17)};

    private static int color_count = 0;
    private int color = 0;

    public Ball(double x, double y, double radius, double mass, Speed speed) {
        this.x = x;
        this.y = y;
        setRadius(radius);
        setMass(mass);
        this.speed = speed;
        this.color = color_count++ % 8;
    }


    // Move
    public void move(double time) {
        move(speed.getX() * time, speed.getY() * time);
    }

    public void move(double x, double y) {
        this.x += x;
        this.y += y;

        if (this.x < radius) {
            this.x = 2 * radius - this.x;
            speed.addX(-2 * speed.getX());
            Billiard.queueHitUpdate();
        }

        if (this.x > Billiard.WIDTH - radius) {
            this.x = 2 * (Billiard.WIDTH - radius) - this.x;
            speed.addX(-2 * speed.getX());
            Billiard.queueHitUpdate();
        }

        if (this.y < radius) {
            this.y = 2 * radius - this.y;
            speed.addY(-2 * speed.getY());
            Billiard.queueHitUpdate();
        }

        if (this.y > Billiard.HEIGHT - radius) {
            this.y = 2 * (Billiard.HEIGHT - radius) - this.y;
            speed.addY(-2 * speed.getY());
            Billiard.queueHitUpdate();
        }
    }

    public void paint(Graphics2D g) {
        g.setColor(colors[color]);
        g.fill(new Ellipse2D.Double(x - radius, y - radius, 2 * radius, 2 * radius));
    }

    public double nextHit(Ball next) {
        double d_x = getX() - next.getX();
        double d_y = getY() - next.getY();
        double d_vx = speed.getX() - next.getSpeed().getX();
        double d_vy = speed.getY() - next.getSpeed().getY();

        if (d_vx == 0 && d_vy == 0)
            return Double.POSITIVE_INFINITY;

        double a = d_vx * d_vx + d_vy * d_vy;
        double b_mezzi = d_vx * d_x + d_vy * d_y;
        double delta_quarti = Math.pow(radius + next.getRadius(), 2) * a - Math.pow(d_vx * d_y - d_vy * d_x, 2);

        if (delta_quarti < 0)
            return Double.POSITIVE_INFINITY;

        double inizio = (-b_mezzi - Math.sqrt(delta_quarti)) / a;
        double fine = (-b_mezzi + Math.sqrt(delta_quarti)) / a;

        if (fine < 0)
            return Double.POSITIVE_INFINITY;

        if (inizio < (inizio - fine) / 2) // Large approximation, but it should work
            return Double.POSITIVE_INFINITY;

        if (inizio < 0)
            return 0.0;

        return inizio;
    }

    public void hit(Ball next, double time) {
        move(speed.getX() * time, speed.getY() * time);
        next.move(next.getSpeed().getX() * time, next.getSpeed().getY() * time);

        double theta = Math.atan2(next.getY() - getY(), next.getX() - getX());

        double v_1 = speed.getComponent(theta);
        double v_2 = next.getSpeed().getComponent(theta);

        double m_1 = getMass();
        double m_2 = next.getMass();

        double w_1 = ((m_1 - m_2) * v_1 + 2 * m_2 * v_2) / (m_1 + m_2);
        double w_2 = ((m_2 - m_1) * v_2 + 2 * m_1 * v_1) / (m_1 + m_2);

        speed.addComponent(theta, -v_1 + w_1);
        next.getSpeed().addComponent(theta, -v_2 + w_2);
    }

    public Speed getSpeed() {
        return speed;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        if (radius > 0)
            this.radius = radius;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        if (mass > 0)
            this.mass = mass;
    }

    public int getColorId() {
        return color;
    }

    public void setColorId(int color_id) {
        this.color = (color_id + 8) % 8;
    }
}
