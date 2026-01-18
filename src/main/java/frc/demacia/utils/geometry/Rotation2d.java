package frc.demacia.utils.geometry;

// Removed redundant nested class declaration
    
public class Rotation2d {
    private double cos;
    private double sin;
    
    public Rotation2d() {
        this(1.0, 0.0);
    }
    
    public Rotation2d(double radians) {
        this.cos = Math.cos(radians);
        this.sin = Math.sin(radians);
    }
    
    public Rotation2d(double x, double y) {
        double magnitude = Math.hypot(x, y);
        if (magnitude > 1e-6) {
            this.cos = x / magnitude;
            this.sin = y / magnitude;
        } else {
            this.cos = 1.0;
            this.sin = 0.0;
        }
    }
    
    public Rotation2d setRadians(double radians) {
        this.cos = Math.cos(radians);
        this.sin = Math.sin(radians);
        return this;
    }
    
    public Rotation2d setDegrees(double degrees) {
        return setRadians(Math.toRadians(degrees));
    }
    
    public Rotation2d setComponents(double x, double y) {
        double magnitude = Math.hypot(x, y);
        if (magnitude > 1e-6) {
            this.cos = x / magnitude;
            this.sin = y / magnitude;
        } else {
            this.cos = 1.0;
            this.sin = 0.0;
        }
        return this;
    }
    
    public Rotation2d rotateBy(Rotation2d other) {
        double newCos = this.cos * other.getCos() - this.sin * other.getSin();
        double newSin = this.cos * other.getSin() + this.sin * other.getCos();
        this.cos = newCos;
        this.sin = newSin;
        return this;
    }
    
    public Rotation2d minus(Rotation2d other) {
        return rotateBy(new Rotation2d(other.cos, -other.sin));
    }
    
    public Rotation2d unaryMinus() {
        this.sin = -this.sin;
        return this;
    }
    
    public double getRadians() {
        return Math.atan2(sin, cos);
    }
    
    public double getDegrees() {
        return Math.toDegrees(getRadians());
    }
    
    public double getCos() {
        return cos;
    }
    
    public double getSin() {
        return sin;
    }
    
    public double getTan() {
        return sin / cos;
    }
    
    public Rotation2d toRotation2d() {
        return new Rotation2d(cos, sin);
    }
    
    public Rotation2d setFrom(Rotation2d rotation) {
        this.cos = rotation.getCos();
        this.sin = rotation.getSin();
        return this;
    }
    
    public Rotation2d copyFrom(Rotation2d other) {
        this.cos = other.cos;
        this.sin = other.sin;
        return this;
    }
}


