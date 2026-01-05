package frc.demacia.utils.geometry;

public class Pose2d {
    private Translation2d translation;
    private Rotation2d rotation;
    
    public Pose2d() {
        this.translation = new Translation2d();
        this.rotation = new Rotation2d();
    }
    
    public Pose2d(double x, double y, double rotationRadians) {
        this.translation = new Translation2d(x, y);
        this.rotation = new Rotation2d(rotationRadians);
    }
    
    public Pose2d(double x, double y, Rotation2d rotation) {
        this.translation = new Translation2d(x, y);
        this.rotation = new Rotation2d().copyFrom(rotation);
    }
    
    public Pose2d(Translation2d translation, Rotation2d rotation) {
        this.translation = new Translation2d().copyFrom(translation);
        this.rotation = new Rotation2d().copyFrom(rotation);
    }
    
    public Pose2d set(double x, double y, double rotationRadians) {
        this.translation.set(x, y);
        this.rotation.setRadians(rotationRadians);
        return this;
    }
    
    public Pose2d set(Translation2d translation, Rotation2d rotation) {
        this.translation.copyFrom(translation);
        this.rotation.copyFrom(rotation);
        return this;
    }
    
    public Pose2d plus(Transform2d transform) {
        Translation2d temp = new Translation2d()
            .setFrom(transform.getTranslation())
            .rotateBy(this.rotation);
        this.translation.plus(temp);
        this.rotation.rotateBy(transform.getRotation());
        return this;
    }
    
    public Pose2d relativeTo(Pose2d other) {
        Translation2d diff = new Translation2d()
            .copyFrom(this.translation)
            .minus(other.getTranslation());
        
        Rotation2d negRotation = new Rotation2d()
            .setFrom(other.getRotation())
            .unaryMinus();
        
        diff.rotateBy(negRotation);
        this.translation.copyFrom(diff);
        
        this.rotation.rotateBy(negRotation);
        return this;
    }
    
    public Translation2d getTranslation() {
        return translation;
    }
    
    public Rotation2d getRotation() {
        return rotation;
    }
    
    public double getX() {
        return translation.getX();
    }
    
    public double getY() {
        return translation.getY();
    }
    
    public Pose2d toPose2d() {
        return new Pose2d(translation.toTranslation2d(), rotation.toRotation2d());
    }
    
    public Pose2d setFrom(Pose2d pose) {
        this.translation.setFrom(pose.getTranslation());
        this.rotation.setFrom(pose.getRotation());
        return this;
    }
    
    public Pose2d copyFrom(Pose2d other) {
        this.translation.copyFrom(other.translation);
        this.rotation.copyFrom(other.rotation);
        return this;
    }
}

