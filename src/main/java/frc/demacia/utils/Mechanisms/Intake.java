package frc.demacia.utils.Mechanisms;

import java.util.function.BiConsumer;

import edu.wpi.first.math.Pair;
import frc.demacia.utils.Motors.MotorInterface;
import frc.demacia.utils.Sensors.SensorInterface;

// איסוף - Intake (עובד עד שחיישן מזהה או לפי זמן)
public class Intake extends StateBasedMechanism<Intake>{
    public interface IntakeState extends MechanismState{
        double[] getValues();
        
        default BiConsumer<Pair<MotorInterface[], SensorInterface[]>, double[]> getConsumer() {
            return (electronics, values) -> {
                MotorInterface[] motors = electronics.getFirst();
                for (int i = 0; i < motors.length && i < values.length; i++) {
                    motors[i].setDuty(values[i]);
                }
            };
        }
    }
    
    public enum IntakeMode {SENSOR, TIMED, CONTINUOUS}

    public Intake(String name, MotorInterface[] motors, SensorInterface[] sensors, 
                  Class<? extends Enum<? extends IntakeState>> enumClass) {
        super(name, motors, sensors, enumClass);
    }
}