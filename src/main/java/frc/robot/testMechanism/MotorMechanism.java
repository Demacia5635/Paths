package frc.robot.testMechanism;

import frc.demacia.utils.mechanisms.BaseMechanism;
import frc.demacia.utils.motors.MotorInterface;
import frc.demacia.utils.motors.TalonFXMotor;
import frc.demacia.utils.sensors.SensorInterface;

public class MotorMechanism extends BaseMechanism{

    public MotorMechanism(){
        super(ArmConstants.NAME, new MotorInterface[] {new TalonFXMotor(ArmConstants.ArmAngleMotorConstants.CONFIG)}, new SensorInterface[] {});
    }
}
