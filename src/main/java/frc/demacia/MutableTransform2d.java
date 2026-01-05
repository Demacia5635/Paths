    package frc.demacia;
    
    import edu.wpi.first.math.geometry.Transform2d;
    
    public class MutableTransform2d {
        private MutableTranslation2d translation;
        private MutableRotation2d rotation;
        
        public MutableTransform2d() {
            this.translation = new MutableTranslation2d();
            this.rotation = new MutableRotation2d();
        }
        
        public MutableTransform2d(double x, double y, double rotationRadians) {
            this.translation = new MutableTranslation2d(x, y);
            this.rotation = new MutableRotation2d(rotationRadians);
        }
        
        public MutableTransform2d(MutableTranslation2d translation, MutableRotation2d rotation) {
            this.translation = new MutableTranslation2d().copyFrom(translation);
            this.rotation = new MutableRotation2d().copyFrom(rotation);
        }
        
        public MutableTransform2d set(double x, double y, double rotationRadians) {
            this.translation.set(x, y);
            this.rotation.setRadians(rotationRadians);
            return this;
        }
        
        public MutableTransform2d set(MutableTranslation2d translation, MutableRotation2d rotation) {
            this.translation.copyFrom(translation);
            this.rotation.copyFrom(rotation);
            return this;
        }
        
        public MutableTranslation2d getTranslation() {
            return translation;
        }
        
        public MutableRotation2d getRotation() {
            return rotation;
        }
        
        public Transform2d toTransform2d() {
            return new Transform2d(translation.toTranslation2d(), rotation.toRotation2d());
        }
        
        public MutableTransform2d setFrom(Transform2d transform) {
            this.translation.setFrom(transform.getTranslation());
            this.rotation.setFrom(transform.getRotation());
            return this;
        }
        
        public MutableTransform2d copyFrom(MutableTransform2d other) {
            this.translation.copyFrom(other.translation);
            this.rotation.copyFrom(other.rotation);
            return this;
        }
    }

