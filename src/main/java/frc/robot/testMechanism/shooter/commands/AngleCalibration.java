package frc.robot.testMechanism.shooter.commands;

import frc.demacia.utils.mechanisms.CalibratinCommand;
import frc.robot.testMechanism.shooter.ShooterConstants.Config;
import frc.robot.testMechanism.shooter.ShooterConstants.Hardware;
import frc.robot.testMechanism.shooter.subsystems.Shooter;

public class AngleCalibration extends CalibratinCommand {
    public AngleCalibration(Shooter shooter) {
        super(
            shooter, 
            Hardware.ANGLE_MOTOR_NAME, 
            Config.CalibrationConstants.CALIBRATION_POWER,
            shooter::isLimitPressed, 
            Config.CalibrationConstants.RESET_POSITION,
            Config.CalibrationConstants.START_POWER,
            Config.CalibrationConstants.START_TIME_SEC
        );
    }
}