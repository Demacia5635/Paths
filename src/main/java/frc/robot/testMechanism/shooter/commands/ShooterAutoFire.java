package frc.robot.testMechanism.shooter.commands;

import frc.demacia.utils.mechanisms.ShooterCommand;
import frc.demacia.utils.motors.MotorInterface.ControlMode;
import frc.robot.testMechanism.shooter.ShooterConstants.Config;
import frc.robot.testMechanism.shooter.subsystems.Shooter;

public class ShooterAutoFire extends ShooterCommand {
    private final Shooter shooter;

    public ShooterAutoFire(Shooter shooter) {
        super(shooter, 
            new ControlMode[] { 
                ControlMode.MOTION,   
                ControlMode.VELOCITY, 
                ControlMode.VELOCITY
            }, 
            () -> false
        );
        this.shooter = shooter;
    }

    @Override
    public void execute() {
        super.execute();
        if (shooter.isReadyToShoot()) {
            shooter.setPower(3, Config.FEEDER_POWER);
        } else {
            shooter.stop(3);
        }
    }
}