package frc.robot.testSensors.servo;
import edu.wpi.first.wpilibj.xrp.XRPServo
import frc.demacia.utils.Sensors.BaseSensorConfig;

public class ServoConfig extends BaseSensorConfig<ServoConfig>{
 public ServoConfig(int id, String name) {
     super(id, name);
     sensorType = XRPServo.class;
 }

}
