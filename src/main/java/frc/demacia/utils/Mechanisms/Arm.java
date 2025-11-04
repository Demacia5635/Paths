package frc.demacia.utils.Mechanisms;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;
import frc.demacia.utils.Motors.MotorInterface;

public class Arm extends StateBasedMechanism {

    public interface ArmState{
        double[] getAngels();
    }

    public double[] maxPos;

    public Arm(String name, MotorInterface[] motors, Class<StateEnum> enumClass) {
        super(name, motors, null, enumClass);
    }

    public void withMaxPose(double[] maxPos){
        this.maxPos = maxPos;
    }

    public void setToState(){
        if (state == null) return;
        for (int i = 0; i < motors.length; i++){
            if (motors[i] == null) return;
            double angle = ((ArmState) state).getAngels()[i];
            if (maxPos != null && angle > maxPos[i]) angle = maxPos[i];
            motors[i].setAngle(angle);
        }
    }

    public Command armCommand(){
        return new RunCommand(() -> setToState(), this);
    }
}
