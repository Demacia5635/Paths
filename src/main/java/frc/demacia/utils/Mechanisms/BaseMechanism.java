package frc.demacia.utils.Mechanisms;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.demacia.utils.Motors.BaseMotorConfig;
import frc.demacia.utils.Motors.MotorInterface;
import frc.demacia.utils.Sensors.BaseSensorConfig;
import frc.demacia.utils.Sensors.SensorInterface;

public abstract class BaseMechanism extends SubsystemBase{
    protected String name;
    protected MotorInterface[] motors;
    protected SensorInterface[] sensors;

    @SuppressWarnings({ "rawtypes"})
    public BaseMechanism(String name, BaseMotorConfig[] motorConfigs, BaseSensorConfig[] sensorConfigs) {
        this.name = name;
        int motorLength = motorConfigs == null ? 0 : motorConfigs.length;
        motors = new MotorInterface[motorLength];
        for (int i = 0; i < motorLength; i++){
            motors[i] = createMotor(motorConfigs[i]); 
        }
        int sensorLength = sensorConfigs == null ? 0 : sensorConfigs.length;;
        sensors = new SensorInterface[sensorLength];
        for (int i = 0; i < sensorLength; i++){
            sensors[i] = createSensor(sensorConfigs[i]);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private MotorInterface createMotor(BaseMotorConfig cfg) {
        try {
            return (MotorInterface) cfg.getMotorClass()
                    .getConstructor(cfg.getClass())
                    .newInstance(cfg);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create motor for " + cfg.name, e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private SensorInterface createSensor(BaseSensorConfig cfg) {
        try {
            return (SensorInterface) cfg.getSensorClass()
                    .getConstructor(cfg.getClass())
                    .newInstance(cfg);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create sensor for " + cfg.name, e);
        }
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

    protected MotorInterface getMotor(int index) {
        if (isValidMotorIndex(index)) return motors[index];
        throw new IllegalArgumentException("Invalid motor index " + index);
    }

    private boolean isValidMotorIndex(int index) {
        return index >= 0 && index < motors.length;
    }
}
