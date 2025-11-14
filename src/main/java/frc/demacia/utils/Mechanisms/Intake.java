package frc.demacia.utils.Mechanisms;

import frc.demacia.utils.Motors.MotorInterface;
import frc.demacia.utils.Sensors.SensorInterface;

public class Intake extends StateBasedMechanism<Intake>{
    public interface IntakeState extends MechanismState{
        double[] getValues();
    }
    
    public enum IntakeMode {SENSOR, TIMED, CONTINUOUS}

    public Intake(String name, MotorInterface[] motors, SensorInterface[] sensors, 
                  Class<? extends Enum<? extends IntakeState>> enumClass) {
        super(name, motors, sensors, enumClass, 
        (motor, values) -> {
            for (int i = 0; i < motor.length && i < values.length; i++) {
                motor[i].setDuty(values[i]);
            }});
    }
}