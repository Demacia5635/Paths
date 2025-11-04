package frc.demacia.utils.Mechanisms;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;
import frc.demacia.utils.Motors.BaseMotorConfig;

public class Arm extends StateBasedMechanism {

    public interface ArmState {
        double[] getAngels();
    }

    @SuppressWarnings("rawtypes")
    public Arm(String name, BaseMotorConfig[] motorConfigs, Class<? extends Enum<? extends ArmState>> enumClass) {
        super(name, motorConfigs, enumClass);
    }

    public void setToState(){
        if (state == null) return;
        for (int i = 0; i < motors.length; i++){
            if (motors[i] == null) return;
            motors[i].setAngle(((ArmState) state).getAngels()[i]);
        }
    }

    public Command armCommand(){
        return new RunCommand(() -> setToState(), this);
    }
}
