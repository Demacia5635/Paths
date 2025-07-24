package frc.robot.utils;

import com.ctre.phoenix6.hardware.Pigeon2;
import com.ctre.phoenix6.configs.Pigeon2Configuration;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.LinearAcceleration;

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
		LogManager.log(name + " cancoder initialized");
    }
    private void configPigeon() {
        Pigeon2Configuration pigeonConfig = new Pigeon2Configuration();
        pigeonConfig.MountPose.MountPosePitch = config.pitchOffset;
        pigeonConfig.MountPose.MountPoseRoll = config.rollOffset;
        pigeonConfig.MountPose.MountPoseYaw = config.yawOffset;
        pigeonConfig.GyroTrim.GyroScalarX = config.xScalar;
        pigeonConfig.GyroTrim.GyroScalarY = config.yScalar;
        pigeonConfig.GyroTrim.GyroScalarZ = config.zScalar;
        pigeonConfig.Pigeon2Features.EnableCompass = config.enableCompass;
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
            zVelocitySignal
        }, 2);
    }

    @SuppressWarnings("rawtypes")
    private double getStatusSignal(StatusSignal statusSignal, double lastValue) {
        statusSignal.refresh();
        if (statusSignal.getStatus() == StatusCode.OK) {
            lastValue = statusSignal.getValueAsDouble() * 2 * Math.PI;
        }
        return lastValue;
    }

    public double getYawSignal() {
        return getStatusSignal(yawSignal, lastYaw);
    }

    public double getPitchSignal() {
        return getStatusSignal(pitchSignal, lastPitch);
    }

    public double getRollSignal() {
        return getStatusSignal(rollSignal, lastRoll);
    }

    public double getXVelocitySignal() {
        return getStatusSignal(xVelocitySignal, lastXVelocity);
    }

    public double getYVelocitySignal() {
        return getStatusSignal(yVelocitySignal, lastYVelocity);
    }

    public double getZVelocitySignal() {
        return getStatusSignal(zVelocitySignal, lastZVelocity);
    }

    public double getXAccelerationSignal() {
        return getStatusSignal(xAccelerationSignal, lastXAcceleration);
    }

    public double getYAccelerationSignal() {
        return getStatusSignal(yAccelerationSignal, lastYAcceleration);
    }

    public double getZAccelerationSignal() {
        return getStatusSignal(zAccelerationSignal, lastZAcceleration);
    }

    public void reset() {
        super.reset();
    }
}