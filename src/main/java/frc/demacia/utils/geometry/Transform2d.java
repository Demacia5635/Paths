package frc.demacia.utils.geometry;

public class Transform2d {
    private Translation2d translation;
    private Rotation2d rotation;
    
    public Transform2d() {
        this.translation = new Translation2d();
        this.rotation = new Rotation2d();
    }
    
    public Transform2d(double x, double y, double rotationRadians) {
        this.translation = new Translation2d(x, y);
        this.rotation = new Rotation2d(rotationRadians);
    }
    
    public Transform2d(Translation2d translation, Rotation2d rotation) {
        this.translation = new Translation2d().copyFrom(translation);
        this.rotation = new Rotation2d().copyFrom(rotation);
    }
    
    public Transform2d set(double x, double y, double rotationRadians) {
        this.translation.set(x, y);
        this.rotation.setRadians(rotationRadians);
        return this;
    }
    
    public Transform2d set(Translation2d translation, Rotation2d rotation) {
        this.translation.copyFrom(translation);
        this.rotation.copyFrom(rotation);
        return this;
    }
    
    public Translation2d getTranslation() {
        return translation;
    }
    
    public Rotation2d getRotation() {
        return rotation;
    }
    
    public Transform2d toTransform2d() {
        return new Transform2d(translation.toTranslation2d(), rotation.toRotation2d());
    }
    
    public Transform2d setFrom(Transform2d transform) {
        this.translation.setFrom(transform.getTranslation());
        this.rotation.setFrom(transform.getRotation());
        return this;
    }
    
    public Transform2d copyFrom(Transform2d other) {
        this.translation.copyFrom(other.translation);
        this.rotation.copyFrom(other.rotation);
        return this;
    }
}

