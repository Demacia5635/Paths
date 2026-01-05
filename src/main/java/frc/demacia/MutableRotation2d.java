package frc.demacia;
import edu.wpi.first.math.geometry.Rotation2d;

// Removed redundant nested class declaration
    
    public class MutableRotation2d {
        private double cos;
        private double sin;
        
        public MutableRotation2d() {
            this(1.0, 0.0);
        }
        
        public MutableRotation2d(double radians) {
            this.cos = Math.cos(radians);
            this.sin = Math.sin(radians);
        }
        
        public MutableRotation2d(double x, double y) {
            double magnitude = Math.hypot(x, y);
            if (magnitude > 1e-6) {
                this.cos = x / magnitude;
                this.sin = y / magnitude;
            } else {
                this.cos = 1.0;
                this.sin = 0.0;
            }
        }
        
        public MutableRotation2d setRadians(double radians) {
            this.cos = Math.cos(radians);
            this.sin = Math.sin(radians);
            return this;
        }
        
        public MutableRotation2d setDegrees(double degrees) {
            return setRadians(Math.toRadians(degrees));
        }
        
        public MutableRotation2d setComponents(double x, double y) {
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
        
        public MutableRotation2d rotateBy(MutableRotation2d other) {
            double newCos = this.cos * other.cos - this.sin * other.sin;
            double newSin = this.cos * other.sin + this.sin * other.cos;
            this.cos = newCos;
            this.sin = newSin;
            return this;
        }
        
        public MutableRotation2d rotateBy(Rotation2d other) {
            double newCos = this.cos * other.getCos() - this.sin * other.getSin();
            double newSin = this.cos * other.getSin() + this.sin * other.getCos();
            this.cos = newCos;
            this.sin = newSin;
            return this;
        }
        
        public MutableRotation2d minus(MutableRotation2d other) {
            return rotateBy(new MutableRotation2d(other.cos, -other.sin));
        }
        
        public MutableRotation2d unaryMinus() {
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
        
        public MutableRotation2d setFrom(Rotation2d rotation) {
            this.cos = rotation.getCos();
            this.sin = rotation.getSin();
            return this;
        }
        
        public MutableRotation2d copyFrom(MutableRotation2d other) {
            this.cos = other.cos;
            this.sin = other.sin;
            return this;
        }
    }
    

