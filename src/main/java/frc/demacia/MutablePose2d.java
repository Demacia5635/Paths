package frc.demacia;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Transform2d;

    public class MutablePose2d {
        private MutableTranslation2d translation;
        private MutableRotation2d rotation;
        
        public MutablePose2d() {
            this.translation = new MutableTranslation2d();
            this.rotation = new MutableRotation2d();
        }
        
        public MutablePose2d(double x, double y, double rotationRadians) {
            this.translation = new MutableTranslation2d(x, y);
            this.rotation = new MutableRotation2d(rotationRadians);
        }
        
        public MutablePose2d(double x, double y, MutableRotation2d rotation) {
            this.translation = new MutableTranslation2d(x, y);
            this.rotation = new MutableRotation2d().copyFrom(rotation);
        }
        
        public MutablePose2d(MutableTranslation2d translation, MutableRotation2d rotation) {
            this.translation = new MutableTranslation2d().copyFrom(translation);
            this.rotation = new MutableRotation2d().copyFrom(rotation);
        }
        
        public MutablePose2d set(double x, double y, double rotationRadians) {
            this.translation.set(x, y);
            this.rotation.setRadians(rotationRadians);
            return this;
        }
        
        public MutablePose2d set(MutableTranslation2d translation, MutableRotation2d rotation) {
            this.translation.copyFrom(translation);
            this.rotation.copyFrom(rotation);
            return this;
        }
        
        public MutablePose2d plus(MutableTransform2d transform) {
            MutableTranslation2d temp = new MutableTranslation2d()
                .copyFrom(transform.getTranslation())
                .rotateBy(this.rotation);
            this.translation.plus(temp);
            this.rotation.rotateBy(transform.getRotation());
            return this;
        }
        
        public MutablePose2d plus(Transform2d transform) {
            MutableTranslation2d temp = new MutableTranslation2d()
                .setFrom(transform.getTranslation())
                .rotateBy(this.rotation);
            this.translation.plus(temp);
            this.rotation.rotateBy(transform.getRotation());
            return this;
        }
        
        public MutablePose2d relativeTo(MutablePose2d other) {
            MutableTranslation2d diff = new MutableTranslation2d()
                .copyFrom(this.translation)
                .minus(other.translation);
            
            MutableRotation2d negRotation = new MutableRotation2d()
                .copyFrom(other.rotation)
                .unaryMinus();
            
            diff.rotateBy(negRotation);
            this.translation.copyFrom(diff);
            
            this.rotation.rotateBy(negRotation);
            return this;
        }
        
        public MutablePose2d relativeTo(Pose2d other) {
            MutableTranslation2d diff = new MutableTranslation2d()
                .copyFrom(this.translation)
                .minus(other.getTranslation());
            
            MutableRotation2d negRotation = new MutableRotation2d()
                .setFrom(other.getRotation())
                .unaryMinus();
            
            diff.rotateBy(negRotation);
            this.translation.copyFrom(diff);
            
            this.rotation.rotateBy(negRotation);
            return this;
        }
        
        public MutableTranslation2d getTranslation() {
            return translation;
        }
        
        public MutableRotation2d getRotation() {
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
        
        public MutablePose2d setFrom(Pose2d pose) {
            this.translation.setFrom(pose.getTranslation());
            this.rotation.setFrom(pose.getRotation());
            return this;
        }
        
        public MutablePose2d copyFrom(MutablePose2d other) {
            this.translation.copyFrom(other.translation);
            this.rotation.copyFrom(other.rotation);
            return this;
        }
    }

