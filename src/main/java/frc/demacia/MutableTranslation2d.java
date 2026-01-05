package frc.demacia;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

public class MutableTranslation2d {
    private double x;
    private double y;
    
    public MutableTranslation2d() {
        this(0.0, 0.0);
    }
    
    public MutableTranslation2d(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public MutableTranslation2d(double distance, MutableRotation2d angle) {
        this.x = distance * angle.getCos();
        this.y = distance * angle.getSin();
    }
    
    public MutableTranslation2d set(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }
    
    public MutableTranslation2d setPolar(double distance, MutableRotation2d angle) {
        this.x = distance * angle.getCos();
        this.y = distance * angle.getSin();
        return this;
    }
    
    public MutableTranslation2d plus(MutableTranslation2d other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }
    
    public MutableTranslation2d plus(Translation2d other) {
        this.x += other.getX();
        this.y += other.getY();
        return this;
    }
    
    public MutableTranslation2d minus(MutableTranslation2d other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }
    
    public MutableTranslation2d minus(Translation2d other) {
        this.x -= other.getX();
        this.y -= other.getY();
        return this;
    }
    
    public MutableTranslation2d times(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }
    
    public MutableTranslation2d div(double scalar) {
        this.x /= scalar;
        this.y /= scalar;
        return this;
    }
    
    public MutableTranslation2d unaryMinus() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }
    
    public MutableTranslation2d rotateBy(MutableRotation2d rotation) {
        double newX = this.x * rotation.getCos() - this.y * rotation.getSin();
        double newY = this.x * rotation.getSin() + this.y * rotation.getCos();
        this.x = newX;
        this.y = newY;
        return this;
    }
    
    public MutableTranslation2d rotateBy(Rotation2d rotation) {
        double newX = this.x * rotation.getCos() - this.y * rotation.getSin();
        double newY = this.x * rotation.getSin() + this.y * rotation.getCos();
        this.x = newX;
        this.y = newY;
        return this;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getNorm() {
        return Math.hypot(x, y);
    }
    
    public double getDistance(MutableTranslation2d other) {
        return Math.hypot(other.x - x, other.y - y);
    }
    
    public double getDistance(Translation2d other) {
        return Math.hypot(other.getX() - x, other.getY() - y);
    }
    
    public MutableRotation2d getAngle() {
        return new MutableRotation2d(x, y);
    }
    
    public Translation2d toTranslation2d() {
        return new Translation2d(x, y);
    }
    
    public MutableTranslation2d setFrom(Translation2d translation) {
        this.x = translation.getX();
        this.y = translation.getY();
        return this;
    }
    
    public MutableTranslation2d copyFrom(MutableTranslation2d other) {
        this.x = other.x;
        this.y = other.y;
        return this;
    }
}

