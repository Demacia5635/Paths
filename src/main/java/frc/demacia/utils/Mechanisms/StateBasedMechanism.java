package frc.demacia.utils.Mechanisms;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public abstract class StateBasedMechanism extends BaseMechanism {

    protected Enum<?> state;

    public void addNT(Class<? extends Enum<?>> enumClass) {
            SendableChooser<Enum<?>> stateChooser = new SendableChooser<>();
            
            for (Enum<?> s : enumClass.getEnumConstants()) {
                stateChooser.addOption(s.name(), s);
            }

            stateChooser.onChange(state -> this.state = state);

            SmartDashboard.putData(getName() + "/State Chooser", stateChooser);
    }
}