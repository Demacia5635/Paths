package frc.demacia.utils.Mechanisms;

import java.util.function.BiConsumer;

import edu.wpi.first.math.Pair;
import frc.demacia.utils.Motors.MotorInterface;
import frc.demacia.utils.Sensors.SensorInterface;

public class Elevator extends StateBasedMechanism{
    public interface ArmState extends MechanismState{
        double[] getValues();

        default BiConsumer<Pair<MotorInterface[], SensorInterface[]>, double[]> getConsumer() {
            return (electronics, values) -> {
                MotorInterface[] motors = electronics.getFirst();
                for (int i = 0; i < motors.length && i < values.length; i++) {
                    motors[i].setMotion(values[i]);
                }
            };
        }
    }

    public Elevator(String name, MotorInterface[] motors, Class<? extends Enum<? extends ArmState>> enumClass) {
        super(name, motors, new SensorInterface[0], enumClass);
    }
}
