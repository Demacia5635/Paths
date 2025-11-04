package frc.demacia.utils.Mechanisms;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.demacia.utils.Motors.MotorInterface;
import frc.demacia.utils.Sensors.SensorInterface;

public abstract class StateBasedMechanism extends BaseMechanism {

    public enum StateEnum {
        Testing,
        IDLE1,
        IDLE2;

        public double[] values;

        double[] getValues(){
            return values;
        }
    }

    double[] testValues;

    protected Enum<?> state;

    public StateBasedMechanism(String name, MotorInterface[] motors, SensorInterface[] sensors, Class<StateEnum> enumClass) {
        super(name, motors, sensors);
        this.addNT(enumClass);
    }

    public void addNT(Class<? extends Enum<?>> enumClass) {
            SendableChooser<Enum<?>> stateChooser = new SendableChooser<>();
            
            for (Enum<?> s : enumClass.getEnumConstants()) {
                stateChooser.addOption(s.name(), s);
            }

            stateChooser.onChange(state -> this.state = state);

            SmartDashboard.putData(getName() + "/State Chooser", stateChooser);
    }

    public void periodic(){
        if (state == StateEnum.Testing){

        }
    }
}