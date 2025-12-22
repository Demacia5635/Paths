package frc.robot.testMechanism;

import frc.demacia.utils.Mechanisms.BaseMechanism;
import frc.demacia.utils.Motors.MotorInterface;
import frc.demacia.utils.Motors.TalonFXMotor;
import frc.demacia.utils.Sensors.SensorInterface;

public class MotorMechanism extends BaseMechanism{

    public MotorMechanism(){
        super(ArmConstants.NAME, new MotorInterface[] {new TalonFXMotor(ArmConstants.ArmAngleMotorConstants.CONFIG)}, new SensorInterface[] {});
    }
}
