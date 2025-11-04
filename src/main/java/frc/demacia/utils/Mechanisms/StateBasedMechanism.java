package frc.demacia.utils.Mechanisms;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.demacia.utils.Motors.BaseMotorConfig;
import frc.demacia.utils.Sensors.BaseSensorConfig;

public abstract class StateBasedMechanism extends BaseMechanism {

    protected Enum<?> state;

    @SuppressWarnings("rawtypes")
    protected StateBasedMechanism(String name, BaseMotorConfig[] motorConfigs, BaseSensorConfig[] sensorConfigs, Class<? extends Enum<?>> enumClass) {
        super(name, motorConfigs, sensorConfigs);
        this.addNT(enumClass);
    }
    
    @SuppressWarnings("rawtypes")
    protected StateBasedMechanism(String name, BaseMotorConfig[] motorConfigs, Class<? extends Enum<?>> enumClass) {
        this(name, motorConfigs, null, enumClass);
    }

    public void addNT(Class<? extends Enum<?>> enumClass) {
            SendableChooser<Enum<?>> stateChooser = new SendableChooser<>();
            
            for (Enum<?> s : enumClass.getEnumConstants()) {
                stateChooser.addOption(s.name(), s);
            }

            stateChooser.onChange(state -> this.state = state);

            SmartDashboard.putData(getName() + "/State Chooser", stateChooser);
    }
}