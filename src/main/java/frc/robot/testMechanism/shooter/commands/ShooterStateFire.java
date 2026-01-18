package frc.robot.testMechanism.shooter.commands;

import frc.demacia.utils.mechanisms.DefaultCommand;
import frc.demacia.utils.motors.MotorInterface.ControlMode;
import frc.robot.testMechanism.shooter.ShooterConstants.Config;
import frc.robot.testMechanism.shooter.ShooterConstants.Hardware;
import frc.robot.testMechanism.shooter.subsystems.Shooter;

public class ShooterStateFire extends DefaultCommand {
    private final Shooter shooter;

    public ShooterStateFire(Shooter shooter) {
        super(shooter, new ControlMode[] {
            ControlMode.MOTION,   // Motor 0: Angle
            ControlMode.VELOCITY, // Motor 1: Up
            ControlMode.VELOCITY, // Motor 2: Down
        });
        this.shooter = shooter;
    }

    @Override
    public void execute() {
        // Sets target Angle, Up, and Down based on current state values
        super.execute();

        // Specific logic for Feeder
        if (shooter.isReadyToShoot()) {
            shooter.setPower(Hardware.FEEDER_MOTOR_NAME, Config.FEEDER_POWER);
        } else {
            shooter.stop(Hardware.FEEDER_MOTOR_NAME);
        }
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        shooter.stopAll();
    }
}