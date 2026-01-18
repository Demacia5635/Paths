package frc.robot.testMechanism.intake.subsystems;

import frc.demacia.utils.mechanisms.BaseMechanism;
import frc.demacia.utils.motors.MotorInterface;
import frc.demacia.utils.motors.TalonFXMotor;
import frc.demacia.utils.sensors.SensorInterface;
import frc.robot.testMechanism.intake.IntakeConstants;
import frc.robot.testMechanism.intake.IntakeConstants.Config;

public class IntakeSubsystem extends BaseMechanism {

    public IntakeSubsystem() {
        super(IntakeConstants.NAME, 
            new MotorInterface[] {
                new TalonFXMotor(Config.MOTOR_CONFIG)
            }, 
            new SensorInterface[0]);
    }
}