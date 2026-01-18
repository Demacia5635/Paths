package frc.demacia.utils.geometry;

public class Translation2d {
    private double x;
    private double y;
    
    public Translation2d() {
        this(0.0, 0.0);
    }
    
    public Translation2d(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public Translation2d(double distance, Rotation2d angle) {
        this.x = distance * angle.getCos();
        this.y = distance * angle.getSin();
    }
    
    public Translation2d set(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }
    
    public Translation2d setPolar(double distance, Rotation2d angle) {
        this.x = distance * angle.getCos();
        this.y = distance * angle.getSin();
        return this;
    }
    
    public Translation2d plus(Translation2d other) {
        this.x += other.getX();
        this.y += other.getY();
        return this;
    }
    
    public Translation2d minus(Translation2d other) {
        this.x -= other.getX();
        this.y -= other.getY();
        return this;
    }
    
    public Translation2d times(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }
    
    public Translation2d div(double scalar) {
        this.x /= scalar;
        this.y /= scalar;
        return this;
    }
    
    public Translation2d unaryMinus() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }
    
    public Translation2d rotateBy(Rotation2d rotation) {
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
    
    public double getDistance(Translation2d other) {
        return Math.hypot(other.getX() - x, other.getY() - y);
    }
    
    public Rotation2d getAngle() {
        return new Rotation2d(x, y);
    }
    
    public Translation2d toTranslation2d() {
        return new Translation2d(x, y);
    }
    
    public Translation2d setFrom(Translation2d translation) {
        this.x = translation.getX();
        this.y = translation.getY();
        return this;
    }
    
    public Translation2d copyFrom(Translation2d other) {
        this.x = other.x;
        this.y = other.y;
        return this;
    }
}

