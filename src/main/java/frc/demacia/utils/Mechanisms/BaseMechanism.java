package frc.demacia.utils.mechanisms;

import java.util.HashMap;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.demacia.utils.log.LogManager;
import frc.demacia.utils.motors.MotorInterface;
import frc.demacia.utils.sensors.SensorInterface;

/**
 * A base class for robot mechanisms (subsystems) that manage a collection of motors and sensors.
 * <p>
 * This class provides common functionality for:
 * <ul>
 * <li>Storing motors and sensors by name for easy retrieval.</li>
 * <li>Controlling all motors at once (stop, set power, set neutral mode).</li>
 * <li>Automatically creating SmartDashboard buttons for switching Neutral Modes (Brake/Coast).</li>
 * <li>Performing electronics checks on hardware.</li>
 * </ul>
 * </p>
 */
public class BaseMechanism extends SubsystemBase{
    /** The name of the mechanism (used for logging and dashboard) */
    protected String name;
    /** Map of motors belonging to this mechanism, keyed by their name */
    protected HashMap<String, MotorInterface> motors;
    /** Map of sensors belonging to this mechanism, keyed by their name */
    protected HashMap<String, SensorInterface> sensors;

    protected MotorInterface[] motorsArray;

    /**
     * Constructs a new BaseMechanism.
     * Initializes the motor and sensor maps and creates debug buttons on the Dashboard.
     * @param name The name of the subsystem
     * @param motors Array of motors to register
     * @param sensors Array of sensors to register
     */
    public BaseMechanism(String name, MotorInterface[] motors, SensorInterface[] sensors) {
        this.name = name;
        setName(name);
        motorsArray = motors;
        // Initialize motors map
        this.motors = new HashMap<>();
        for (MotorInterface motor : motors) {
            this.motors.put(motor.getName(), motor);
        }
        
        // Initialize sensors map
        this.sensors = new HashMap<>();
        for (SensorInterface sensor : sensors) {
            this.sensors.put(sensor.getName(), sensor);
        }

        // Create individual Brake/Coast buttons for each motor
        for (String motorName : this.motors.keySet()) {
            SmartDashboard.putData(getName() + "/" + motorName + "/set brake", 
                new InstantCommand(() -> setNeutralMode(motorName, true)).ignoringDisable(true));
            SmartDashboard.putData(getName() + "/" + motorName + "/set coast", 
                new InstantCommand(() -> setNeutralMode(motorName, false)).ignoringDisable(true));
        }

        // Create global Brake/Coast buttons for the whole mechanism
        SmartDashboard.putData(getName() + "/set coast all", 
                new InstantCommand(() -> setNeutralModeAll(false)).ignoringDisable(true));
        SmartDashboard.putData(getName() + "/set brake all", 
                new InstantCommand(() -> setNeutralModeAll(true)).ignoringDisable(true));
        
        SmartDashboard.putData(name, this);
    }

    /**
     * @return The name of the mechanism
     */
    public String getName(){
        return name;
    }

    /**
     * Stops all motors in this mechanism.
     */
    public void stopAll(){
        if (motors == null) return;
        for (MotorInterface motor : motors.values()){
            motor.stop();
        }
    }

    /**
     * Stops a specific motor by name.
     * @param motorName The name of the motor to stop
     */
    public void stop(String motorName){
        if (isValidMotorIndex(motorName)){
            motors.get(motorName).setDuty(0);
        }
    }

    /**
     * Sets the duty cycle (power) for all motors.
     * @param power The power to set [-1.0, 1.0]
     */
    public void setPowerAll(double power) {
        if (motors == null) return;
        for (MotorInterface motor : motors.values()){
            motor.setDuty(power);
        }
    }

    /**
     * Sets the duty cycle (power) for a specific motor.
     * @param motorName The name of the motor
     * @param power The power to set [-1.0, 1.0]
     */
    public void setPower(String motorName, double power){
        if (isValidMotorIndex(motorName)){
            motors.get(motorName).setDuty(power);
        }
    }

    /**
     * Sets the neutral mode (Brake or Coast) for all motors.
     * @param isBrake true for Brake mode, false for Coast mode
     */
    public void setNeutralModeAll(boolean isBrake) {
        if (motors == null) return;
        for (MotorInterface motor : motors.values()) {
            if (motor != null) motor.setNeutralMode(isBrake);
        }
    }

    /**
     * Sets the neutral mode (Brake or Coast) for a specific motor.
     * @param motorName The name of the motor
     * @param isBrake true for Brake mode, false for Coast mode
     */
    public void setNeutralMode(String motorName, boolean isBrake){
        if (isValidMotorIndex(motorName)){
            motors.get(motorName).setNeutralMode(isBrake);
        }
    }

    /**
     * Triggers the electronics check for all motors and sensors.
     */
    public void checkElectronicsAll() {
        if (motors == null) return;
        for (MotorInterface motor : motors.values()) {
            if (motor != null) motor.checkElectronics();
        }
        if (sensors == null) return;
        for (SensorInterface sensor : sensors.values()) {
            if (sensor != null) sensor.checkElectronics();
        }
    }

    /**
     * Checks electronics for a specific motor.
     * @param motorName The name of the motor
     */
    public void checkElectronicsMotor(String motorName){
        if (isValidMotorIndex(motorName)){
            motors.get(motorName).checkElectronics();
        }
    }

    /**
     * Checks electronics for a specific sensor.
     * @param sensorName The name of the sensor
     */
    public void checkElectronicsSensor(String sensorName){
        if (isValidSensorIndex(sensorName)){
            sensors.get(sensorName).checkElectronics();
        }
    }

    /**
     * Retrieves a motor object by its name.
     * Logs an error if the motor name is invalid.
     * @param motorName The name of the motor
     * @return The MotorInterface object, or null if not found
     */
    public MotorInterface getMotor(String motorName) {
        if (!isValidMotorIndex(motorName)){
            LogManager.log("Invalid motor: " + motorName);
            return null;
        }
        return motors.get(motorName);
    }

    public MotorInterface[] getMotors() {
        return motorsArray;
    }

    /**
     * Retrieves a sensor object by its name.
     * Logs an error if the sensor name is invalid.
     * @param sensorName The name of the sensor
     * @return The SensorInterface object, or null if not found
     */
    public SensorInterface getSensor(String sensorName) {
        if (!isValidSensorIndex(sensorName)){
            LogManager.log("Invalid sensor: " + sensorName);
            return null;
        }
        return sensors.get(sensorName);
    }

    /**
     * Checks if a motor name exists in the map.
     * @param motorName The name to check
     * @return true if valid, false otherwise
     */
    protected boolean isValidMotorIndex(String motorName) {
        return motors.containsKey(motorName);
    }

    /**
     * Checks if a sensor name exists in the map.
     * @param sensorName The name to check
     * @return true if valid, false otherwise
     */
    protected boolean isValidSensorIndex(String sensorName) {
        return sensors.containsKey(sensorName);
    }
}