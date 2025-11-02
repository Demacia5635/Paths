package frc.demacia.utils.Mechanisms;

import frc.demacia.utils.Motors.MotorInterface;

public abstract class BaseMechanism {
    protected String name;
    protected MotorInterface[] motors;

    public String getName(){
        return name;
    }

    public void stopAll(){
        for (MotorInterface motor : motors){
            motor.setDuty(0);
        }
    }

    public void stopAll(int motorIndex){
        if (motorIndex < motors.length && motorIndex >= 0){
            motors[motorIndex].setDuty(0);
        }
    }

    public void setPower(int motorIndex, double power){
        if (motorIndex < motors.length && motorIndex >= 0){
            motors[motorIndex].setDuty(0);
        }
    }

    public void setNeutralMode(int motorIndex, boolean isBrake){
        if (motorIndex < motors.length && motorIndex >= 0){
            motors[motorIndex].setNeutralMode(isBrake);;
        }
    }
}
