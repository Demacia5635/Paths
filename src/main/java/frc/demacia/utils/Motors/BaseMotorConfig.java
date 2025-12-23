package frc.demacia.utils.Motors;

import com.ctre.phoenix6.CANBus;

/**
 * Abstract base class for motor configurations.
 */
public abstract class BaseMotorConfig {
    
    public static enum Canbus { 
        Rio("rio"), 
        CANIvore("canivore");
    
        public final CANBus canbus;
        private Canbus(String name) {
            this.canbus = new CANBus(name);
        }
    } 

    public final int id;
    public final Canbus canbus;
    public final String name;

    public double maxVolt = 12;
    public double minVolt = -12;
    public double maxCurrent = 40;
    public double rampUpTime = 0.3;

    public boolean brake = true;
    public double motorRatio = 1;
    public boolean inverted = false;

    public double maxVelocity = 0;
    public double maxAcceleration = 0;
    public double maxJerk = 0;
    public double maxPositionError = 0.5;

    public CloseLoopParam[] pid = {new CloseLoopParam(), new CloseLoopParam(), new CloseLoopParam(), new CloseLoopParam()};

    public boolean isMeterMotor = false;
    public boolean isRadiansMotor = false;

    public double kv2 = 0;
    public double kSin = 0;
    public double posToRad = 0;

    public BaseMotorConfig(int id, String name, Canbus canbus) {
        this.id = id;
        this.name = name;
        this.canbus = canbus;
    }

    public BaseMotorConfig(int id, String name) {
        this(id, name, Canbus.Rio);
    }

    public BaseMotorConfig(int id, Canbus canbus) {
        this(id, "Unnamed Motor", canbus);
    }

    protected abstract BaseMotorConfig self();

    public abstract MotorInterface create();
    
    public BaseMotorConfig withVolts(double maxVolt) {
        this.maxVolt = maxVolt;
        this.minVolt = -maxVolt;
        return self();
    }

    public BaseMotorConfig withBrake(boolean brake) {
        this.brake = brake;
        return self();
    }

    public BaseMotorConfig withInvert(boolean invert) {
        this.inverted = invert;
        return self();
    }

    public BaseMotorConfig withRampTime(double rampTime) {
        this.rampUpTime = rampTime;
        return self();
    }

    public BaseMotorConfig withMeterMotor(double gearRatio, double diameter) {
        motorRatio = gearRatio / (diameter * Math.PI);
        isMeterMotor = true;
        isRadiansMotor = false;
        return self();
    }

    public BaseMotorConfig withRadiansMotor(double gearRatio) {
        motorRatio = gearRatio / (Math.PI * 2);
        isMeterMotor = false;
        isRadiansMotor = true;
        return self();
    }
    
    public BaseMotorConfig withMaxPositionError(double maxPositionError) {
        this.maxPositionError = maxPositionError;
        return self();
    }

    public BaseMotorConfig withCurrent(double maxCurrent) {
        this.maxCurrent = maxCurrent;
        return self();
    }
    
    public BaseMotorConfig withMotionParam(double maxVelocity, double maxAcceleration, double maxJerk) {
        this.maxVelocity = maxVelocity;
        this.maxAcceleration = maxAcceleration;
        this.maxJerk = maxJerk;
        return self();
    }
    
    public BaseMotorConfig withFeedForward(double kv2, double ksin, double posToRad) {
        this.kv2 = kv2;
        this.kSin = ksin;
        this.posToRad = posToRad;
        return self();
    }

    public BaseMotorConfig withPID(double kp, double ki, double kd, double ks, double kv, double ka, double kg) {
        return withPID(0, kp, ki, kd, ks, kv, ka, kg);
    }
    
    public BaseMotorConfig withPID(int slot, double kp, double ki, double kd, double ks, double kv, double ka, double kg) {
        if (slot >= 0 && slot < pid.length) {
            pid[slot] = new CloseLoopParam(kp, ki, kd, ks, kv, ka, kg);
        }
        return self();
    }

    protected void copyBaseFields(BaseMotorConfig other) {
        this.maxVolt = other.maxVolt;
        this.minVolt = other.minVolt;
        this.maxCurrent = other.maxCurrent;
        this.rampUpTime = other.rampUpTime;
        this.brake = other.brake;
        this.motorRatio = other.motorRatio;
        this.inverted = other.inverted;
        this.kv2 = other.kv2;
        this.kSin = other.kSin;
        this.posToRad = other.posToRad;
        this.maxAcceleration = other.maxAcceleration;
        this.maxVelocity = other.maxVelocity;
        this.maxJerk = other.maxJerk;
        this.pid[0].set(other.pid[0]);
        this.pid[1].set(other.pid[1]);
        this.pid[2].set(other.pid[2]);
        this.pid[3].set(other.pid[3]);
        this.maxPositionError = other.maxPositionError;
        this.isMeterMotor = other.isMeterMotor;
        this.isRadiansMotor = other.isRadiansMotor;
   }
}