package frc.robot.utils;

import com.ctre.phoenix6.hardware.Pigeon2;
import com.ctre.phoenix6.configs.Pigeon2Configuration;;

public class Pigeon extends Pigeon2{
    PigeonConfig config;
    String name;

    public Pigeon(PigeonConfig config){
        super(config.id, config.canbus);
        this.config = config;
        name = config.name;
        configPigeon();
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
}