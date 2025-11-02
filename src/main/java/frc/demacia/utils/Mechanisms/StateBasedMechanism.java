package frc.demacia.utils.Mechanisms;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public abstract class StateBasedMechanism extends BaseMechanism {

    protected enum States{
        
    }
    protected States state;

    public void addNT() {
        try {
            SendableChooser<States> stateChooser = new SendableChooser<>();
            Class<?> enumClass = Class.forName(getClass().getName() + "$States");
            States[] possibleStates = (States[]) enumClass.getMethod("values").invoke(null);

            for (States s : possibleStates) {
                stateChooser.addOption(s.name(), s);
            }

            stateChooser.onChange(state -> this.state = state);

            SmartDashboard.putData(getName() + "/State Chooser", stateChooser);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}