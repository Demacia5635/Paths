package frc.robot.testMechanism.shooter.commands;

import frc.demacia.utils.mechanisms.ShooterCommand;
import frc.demacia.utils.motors.MotorInterface.ControlMode;
import frc.robot.testMechanism.shooter.subsystems.Shooter;

public class ShooterAutoFire extends ShooterCommand {
    public ShooterAutoFire(Shooter shooter) {
        super(shooter, 
            new ControlMode[] { 
                ControlMode.MOTION,   
                ControlMode.VELOCITY, 
                ControlMode.VELOCITY  
            }, 
            () -> !shooter.isNoteIn()
        );
    }
}