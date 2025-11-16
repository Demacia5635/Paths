package frc.demacia.utils.Mechanisms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.demacia.utils.Motors.MotorInterface;
import frc.demacia.utils.Sensors.SensorInterface;

/**
 * Base class for all robot mechanisms.
 * 
 * <p>Provides common functionality for managing motors and sensors with
 * trigger-based conditional control.</p>
 * 
 * <p><b>Features:</b></p>
 * <ul>
 *   <li>Trigger-based conditional actions</li>
 *   <li>Automatic safety stops</li>
 *   <li>Motor and sensor management</li>
 *   <li>Electronics health checking</li>
 * </ul>
 * 
 * <p><b>Example Usage:</b></p>
 * <pre>
 * BaseMechanism mechanism = new BaseMechanism(
 *     "Intake", 
 *     new MotorInterface[] {motor1, motor2},
 *     new SensorInterface[] {beamBreak},
 *     (motors, values) -> {
 *         for (int i = 0; i < motors.length; i++) {
 *             motors[i].setDuty(values[i]);
 *         }
 *     }
 * );
 * 
 * // Add trigger to stop when sensor detects game piece
 * mechanism.addStop(() -> beamBreak.get());
 * </pre>
 * 
 * @param <T> The concrete mechanism type (for method chaining)
 */
public abstract class BaseMechanism<T extends BaseMechanism<T>> extends SubsystemBase{

    public static class Trigger {
        private final Supplier<Boolean> condition;
        private final BiConsumer<MotorInterface[], double[]> consumer;

        public Trigger(Supplier<Boolean> condition, BiConsumer<MotorInterface[], double[]> consumer) {
            this.condition = condition;
            this.consumer = consumer;
        }

        public boolean check() {
            return condition.get();
        }

        public BiConsumer<MotorInterface[], double[]> getConsumer() {
            return consumer;
        }
    }

    protected String name;
    protected MotorInterface[] motors;
    protected SensorInterface[] sensors;
    protected double[] values;

    
    private List<Trigger> triggers = new ArrayList<>();
    protected BiConsumer<MotorInterface[], double[]> consumer;

    public BaseMechanism(String name, MotorInterface[] motors, SensorInterface[] sensors, BiConsumer<MotorInterface[], double[]> consumer) {
        this.name = name;
        this.motors = motors != null ? motors : new MotorInterface[0];;
        this.sensors = sensors != null ? sensors : new SensorInterface[0];
        this.consumer = consumer;
        values = new double[0];
    }

    public String getName(){
        return name;
    }

    /**
     * Sets the values to be used by the mechanism consumer.
     * 
     * <p>These values are typically motor powers, positions, or velocities.</p>
     * 
     * @param values Array of values (interpretation depends on consumer)
     */
    public void setValues(double[] values){
        this.values = values != null ? values : new double[0];
    }

    /**
     * Adds a conditional trigger that runs when condition is true.
     * 
     * <p>Triggers are checked every cycle in order of addition.
     * Use for conditional actions like "stop when limit switch pressed".</p>
     * 
     * @param condition Supplier that returns true when trigger should fire
     * @param consumer Action to perform (receives motors and current values)
     * @return this mechanism for chaining
     */
    @SuppressWarnings("unchecked")
    public T addTrigger(Supplier<Boolean> condition, BiConsumer<MotorInterface[], double[]> consumer) {
        if (condition == null || consumer == null) {
            throw new IllegalArgumentException("Trigger condition and consumer cannot be null");
        }
        triggers.add(new Trigger(condition, consumer));
        return (T) this;
    }

    /**
     * Adds a stop trigger that sets all motors to zero when condition is true.
     * 
     * <p>Convenient shortcut for safety stops.</p>
     * 
     * @param condition Supplier that returns true when mechanism should stop
     * @return this mechanism for chaining
     */
    @SuppressWarnings("unchecked")
    public T addStop(Supplier<Boolean> condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Stop condition cannot be null");
        }
        BiConsumer<MotorInterface[], double[]> stopConsumer = 
        (motors, values) -> {
            for (MotorInterface motor : motors) {
                motor.setDuty(0);
            }};
        triggers.add(new Trigger(condition, stopConsumer));
        return (T) this;
    }
    
    /**
     * Creates a command that continuously runs this mechanism.
     * 
     * <p>The command checks triggers and applies the consumer each cycle.</p>
     * 
     * @return RunCommand that requires this subsystem
     */
    public Command runMechanismCommand(){
        return new RunCommand(() -> {
            checkTriggers();
            runMechanism();}
            , this);
    }

    private void checkTriggers() {
        for (Trigger trigger : triggers) {
            if (trigger.check()) {
                trigger.getConsumer().accept(motors, values);
            }
        }
    }

    private void runMechanism(){
        if (consumer == null) {
            System.err.println("Consumer is null, cannot run mechanism");
            return;
        }
        if (values == null) {
            System.err.println("Values is null, cannot run mechanism");
            return;
        }

        consumer.accept(motors, values);
    }

    /**
     * Stops all motors immediately.
     */
    public void stopAll(){
        if (motors == null) return;
        for (MotorInterface motor : motors){
            motor.setDuty(0);
        }
    }

    /**
     * Stops a specific motor by index.
     * 
     * @param motorIndex Index of motor to stop
     */
    public void stop(int motorIndex){
        if (isValidMotorIndex(motorIndex)){
            motors[motorIndex].setDuty(0);
        }
    }

    /**
     * Sets power for a specific motor.
     * 
     * @param motorIndex Index of motor
     * @param power Duty cycle (-1.0 to 1.0)
     */
    public void setPower(int motorIndex, double power){
        if (isValidMotorIndex(motorIndex)){
            motors[motorIndex].setDuty(power);
        }
    }

    /**
     * Sets neutral mode for all motors.
     * 
     * @param isBrake true for brake, false for coast
     */
    public void setNeutralModeAll(boolean isBrake) {
        if (motors == null) return;
        for (MotorInterface motor : motors) {
            if (motor != null) motor.setNeutralMode(isBrake);
        }
    }

    /**
     * Checks electronics for all motors and sensors.
     * 
     * <p>Logs any faults to console and telemetry.</p>
     */
    public void setNeutralMode(int motorIndex, boolean isBrake){
        if (isValidMotorIndex(motorIndex)){
            motors[motorIndex].setNeutralMode(isBrake);
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
            motors[motorIndex].checkElectronics();
        }
    }

    public void checkElectronicsSensor(int sensorIndex){
        if (isValidSensorIndex(sensorIndex)){
            sensors[sensorIndex].checkElectronics();
        }
    }

    /**
     * Gets a motor by index.
     * 
     * @param index Motor index
     * @return The motor interface
     * @throws IllegalArgumentException if index is invalid
     */
    public MotorInterface getMotor(int index) {
        if (isValidMotorIndex(index)) return motors[index];
        throw new IllegalArgumentException("Invalid motor index " + index);
    }

    /**
     * Gets a sensor by index.
     * 
     * @param index Sensor index
     * @return The sensor interface
     * @throws IllegalArgumentException if index is invalid
     */
    public SensorInterface getSensor(int index) {
        if (isValidSensorIndex(index)) return sensors[index];
        throw new IllegalArgumentException("Invalid sensor index " + index);
    }

    private boolean isValidMotorIndex(int index) {
        return index >= 0 && index < motors.length;
    }

    private boolean isValidSensorIndex(int index) {
        return index >= 0 && index < sensors.length;
    }
}
