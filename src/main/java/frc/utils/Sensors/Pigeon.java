package frc.utils.Sensors;

import com.ctre.phoenix6.hardware.Pigeon2;
import com.ctre.phoenix6.configs.Pigeon2Configuration;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.LinearAcceleration;
import frc.utils.Log.LogManager;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;

public class Pigeon extends Pigeon2{
    PigeonConfig config;
    String name;

    StatusSignal<Angle> yawSignal;
    StatusSignal<Angle> pitchSignal;
    StatusSignal<Angle> rollSignal;
    StatusSignal<AngularVelocity> xVelocitySignal;
    StatusSignal<AngularVelocity> yVelocitySignal;
    StatusSignal<AngularVelocity> zVelocitySignal;
    StatusSignal<LinearAcceleration> xAccelerationSignal;
    StatusSignal<LinearAcceleration> yAccelerationSignal;
    StatusSignal<LinearAcceleration> zAccelerationSignal;

    double lastYaw;
    double lastPitch;
    double lastRoll;
    double lastXVelocity;
    double lastYVelocity;
    double lastZVelocity;
    double lastXAcceleration;
    double lastYAcceleration;
    double lastZAcceleration;

    public Pigeon(PigeonConfig config){
        super(config.id, config.canbus);
        this.config = config;
        name = config.name;
        configPigeon();
        setStatusSignals();
        addLog();
		LogManager.log(name + " pigeon initialized");
    }
    private void configPigeon() {
        Pigeon2Configuration pigeonConfig = new Pigeon2Configuration();
        pigeonConfig.MountPose.MountPosePitch = config.pitchOffset;
        pigeonConfig.MountPose.MountPoseRoll = config.rollOffset;
        pigeonConfig.MountPose.MountPoseYaw = config.yawOffset;
        pigeonConfig.GyroTrim.GyroScalarX = config.xScalar;
        pigeonConfig.GyroTrim.GyroScalarY = config.yScalar;
        pigeonConfig.GyroTrim.GyroScalarZ = config.zScalar;
        pigeonConfig.Pigeon2Features.EnableCompass = config.compass;
        pigeonConfig.Pigeon2Features.DisableTemperatureCompensation = !config.temperatureCompensation;
        pigeonConfig.Pigeon2Features.DisableNoMotionCalibration = !config.noMotionCalibration;
        getConfigurator().apply(pigeonConfig);
    }

    private void setStatusSignals(){
        yawSignal = getYaw();
        pitchSignal = getPitch();
        rollSignal = getRoll();
        xVelocitySignal = getAngularVelocityXWorld();
        yVelocitySignal = getAngularVelocityYWorld();
        zVelocitySignal = getAngularVelocityZWorld();
        xAccelerationSignal = getAccelerationX();
        yAccelerationSignal = getAccelerationY();
        zAccelerationSignal = getAccelerationZ();

        lastYaw = yawSignal.getValueAsDouble();
        lastPitch = pitchSignal.getValueAsDouble();
        lastRoll = rollSignal.getValueAsDouble();
        lastXVelocity = xVelocitySignal.getValueAsDouble();
        lastYVelocity = yVelocitySignal.getValueAsDouble();
        lastZVelocity = zVelocitySignal.getValueAsDouble();
        lastXAcceleration = xAccelerationSignal.getValueAsDouble();
        lastYAcceleration = yAccelerationSignal.getValueAsDouble();
        lastZAcceleration = zAccelerationSignal.getValueAsDouble();
    }

    public void checkElectronics() {
        if (getFaultField().getValue() != 0) {
            LogManager.log(name + " have a fault: " + getFaultField().getValue());
        }
    }

    private void addLog() {
        LogManager.addEntry(name + "/yaw and pitch and x velocity and y velocity and z velocity", () -> new StatusSignal[] {
            yawSignal,
            pitchSignal,
            rollSignal,
            xVelocitySignal,
            yVelocitySignal,
            zVelocitySignal,
            xAccelerationSignal,
            yAccelerationSignal,
            zAccelerationSignal
        }, 2);
    }

    @SuppressWarnings("rawtypes")
    private double getStatusSignal(StatusSignal statusSignal, double lastValue) {
        return getStatusSignal(statusSignal, lastValue, false);
    }

    @SuppressWarnings("rawtypes")
    private double getStatusSignal(StatusSignal statusSignal, double lastValue, boolean convertAngle) {
        statusSignal.refresh();
        if (statusSignal.getStatus() == StatusCode.OK) {
            double value = statusSignal.getValueAsDouble();
            value = config.isInverted ? -value : value;
            lastValue = convertAngle ? (config.isRadians ? value * Math.PI / 180 : value) : value;
        }
        return lastValue;
    }

    @SuppressWarnings("rawtypes")
    private double getAngularAccelerationStatusSignal(StatusSignal velocitySignal, double lastVelocity) {
        velocitySignal.refresh();
        if (velocitySignal.getStatus() == StatusCode.OK) {
            return (velocitySignal.getValueAsDouble()) - lastVelocity;
        }
        return 0;
    }

    public double getYawSignal() {
        lastYaw = getStatusSignal(yawSignal, lastYaw, true);
        return lastYaw;
    }

    public double getYawSignalInZeroToPi() {
        double yaw = getYawSignal();
        return config.isRadians ? (yaw % (2 * Math.PI) + (2 * Math.PI)) % (2 * Math.PI) : (yaw % 360 + 360) % 360;
    }

    public double getPitchSignal() {
        lastPitch = getStatusSignal(pitchSignal, lastPitch, true);
        return lastPitch;
    }

    public double getPitchSignalInZeroToPi() {
        double pitch = getPitchSignal();
        return config.isRadians ? (pitch % (2 * Math.PI) + (2 * Math.PI)) % (2 * Math.PI) : (pitch % 360 + 360) % 360;
    }

    public double getRollSignal() {
        lastRoll = getStatusSignal(rollSignal, lastRoll, true);
        return lastRoll;
    }

    public double getRollSignalInZeroToPi() {
        double roll = getRollSignal();
        return config.isRadians ? (roll % (2 * Math.PI) + (2 * Math.PI)) % (2 * Math.PI) : (roll % 360 + 360) % 360;
    }

    public double getXVelocitySignal() {
        lastXVelocity = getStatusSignal(xVelocitySignal, lastXVelocity);
        return lastXVelocity;
    }

    public double getYVelocitySignal() {
        lastYVelocity = getStatusSignal(yVelocitySignal, lastYVelocity);
        return lastYVelocity;
    }

    public double getZVelocitySignal() {
        lastZVelocity = getStatusSignal(zVelocitySignal, lastZVelocity);
        return lastZVelocity;
    }

    public double getXAccelerationSignal() {
        lastXAcceleration = getStatusSignal(xAccelerationSignal, lastXAcceleration);
        return lastXAcceleration;
    }

    public double getYAccelerationSignal() {
        lastYAcceleration = getStatusSignal(yAccelerationSignal, lastYAcceleration);
        return lastYAcceleration;
    }

    public double getZAccelerationSignal() {
        lastZAcceleration = getStatusSignal(zAccelerationSignal, lastZAcceleration);
        return lastZAcceleration;
    }

    public double getXAngularAccelerationSignal() {
        double acceleration = getAngularAccelerationStatusSignal(xVelocitySignal, lastXVelocity);
        lastXVelocity = xVelocitySignal.getValueAsDouble();
        return acceleration;
    }

    public double getYAngularAccelerationSignal() {
        double acceleration = getAngularAccelerationStatusSignal(yVelocitySignal, lastYVelocity);
        lastYVelocity = yVelocitySignal.getValueAsDouble();
        return acceleration;
    }

    public double getZAngularAccelerationSignal() {
        double acceleration = getAngularAccelerationStatusSignal(zVelocitySignal, lastZVelocity);
        lastZVelocity = zVelocitySignal.getValueAsDouble();
        return acceleration;
    }

    public void reset() {
        super.reset();
    }


}