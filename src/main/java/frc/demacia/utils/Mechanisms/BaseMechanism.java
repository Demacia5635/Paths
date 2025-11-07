package frc.demacia.utils.Mechanisms;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.demacia.utils.Motors.MotorInterface;
import frc.demacia.utils.Sensors.SensorInterface;

public abstract class BaseMechanism extends SubsystemBase{
    protected String name;
    protected MotorInterface[] motors;
    protected SensorInterface[] sensors;

    public BaseMechanism(String name, MotorInterface[] motors, SensorInterface[] sensors) {
        this.name = name;
        this.motors = motors;
        this.sensors = sensors;
    }

    public String getName(){
        return name;
    }

    public void stopAll(){
        if (motors == null) return;
        for (MotorInterface motor : motors){
            motor.setDuty(0);
        }
    }

    public void stop(int motorIndex){
        if (isValidMotorIndex(motorIndex)){
            motors[motorIndex].setDuty(0);
        }
    }

    public void setPower(int motorIndex, double power){
        if (isValidMotorIndex(motorIndex)){
            motors[motorIndex].setDuty(power);
        }
    }

    public void setNeutralModeAll(boolean isBrake) {
        if (motors == null) return;
        for (MotorInterface motor : motors) {
            if (motor != null) motor.setNeutralMode(isBrake);
        }
    }

    public void setNeutralMode(int motorIndex, boolean isBrake){
        if (isValidMotorIndex(motorIndex)){
            motors[motorIndex].setNeutralMode(isBrake);;
        }
    }

    public void checkElectronicsAll() {
        if (motors == null) return;
        for (MotorInterface motor : motors) {
            if (motor != null) motor.checkElectronics();
        }
        if (sensors == null) return;
        for (SensorInterface sensor : sensors) {
            if (sensor != null) sensor.checkElectronics();
        }
    }

    public void checkElectronicsMotor(int motorIndex){
        if (isValidMotorIndex(motorIndex)){
            motors[motorIndex].checkElectronics();;
        }
    }

    public void checkElectronicsSensor(int sensorIndex){
        if (isValidSensorIndex(sensorIndex)){
            sensors[sensorIndex].checkElectronics();;
        }
    }

    public MotorInterface getMotor(int index) {
        if (isValidMotorIndex(index)) return motors[index];
        throw new IllegalArgumentException("Invalid motor index " + index);
    }

    public SensorInterface getSensor(int index) {
        if (isValidSensorIndex(index)) return sensors[index];
        throw new IllegalArgumentException("Invalid motor index " + index);
    }

    private boolean isValidMotorIndex(int index) {
        return index >= 0 && index < motors.length;
    }

    private boolean isValidSensorIndex(int index) {
        return index >= 0 && index < sensors.length;
    }
}
