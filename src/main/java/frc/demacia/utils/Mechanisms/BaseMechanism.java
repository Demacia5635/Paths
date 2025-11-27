package frc.demacia.utils.Mechanisms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.demacia.utils.Log.LogManager;
import frc.demacia.utils.Log.LogEntryBuilder.LogLevel;
import frc.demacia.utils.Motors.MotorInterface;
import frc.demacia.utils.Sensors.AnalogSensorInterface;
import frc.demacia.utils.Sensors.DigitalSensorInterface;
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
public class BaseMechanism<T extends BaseMechanism<T>> extends SubsystemBase{

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

    public static class MotorLimits {
        private final double min;
        private final double max;

        public MotorLimits(double min, double max) {
            if (min > max) {
                throw new IllegalArgumentException("Min limit cannot be greater than max limit");
            }
            this.min = min;
            this.max = max;
        }

        public double clamp(double value) {
            if (value < min) return min;
            if (value > max) return max;
            return value;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }
    }

    protected String name;
    protected MotorInterface[] motors;
    protected SensorInterface[] sensors;
    protected double[] values;

    protected Supplier<Boolean> isCalibratedSupplier = () -> true;
    
    protected List<Trigger> triggers = new ArrayList<>();
    protected MotorLimits[] motorsLimits;
    protected BiConsumer<MotorInterface[], double[]> consumer;

    @SuppressWarnings("unchecked")
    public BaseMechanism(String name, MotorInterface[] motors, SensorInterface[] sensors, BiConsumer<MotorInterface[], double[]> consumer) {
        this.name = name;
        this.motors = motors != null ? motors : new MotorInterface[0];;
        this.sensors = sensors != null ? sensors : new SensorInterface[0];
        this.consumer = consumer;
        motorsLimits = new MotorLimits[motors.length];
        values = new double[motors.length];
        LogManager.addEntry(name + " values", () -> values)
        .withLogLevel(LogLevel.LOG_AND_NT_NOT_IN_COMP).build();
    }

    public String getName(){
        return name;
    }

    /**
     * Sets the values to be used by the mechanism consumer.
     * 
     * <p>These values are typically motor powers, positions, angles, or velocities.</p>
     * 
     * @param values Array of values (interpretation depends on consumer)
     */
    public void setValues(double[] values){
        if (values.length != motors.length) {
            throw new IllegalArgumentException("Values size must match motor count");
        }
        this.values = values != null ? values : new double[motors.length];
    }

    /**
     * Get the values that will be used by the mechanism consumer.
     * 
     * <p>These values are typically motor powers, positions, angles, or velocities.</p>
     */
    public double[] getValues(){
        return values;
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
     * Adds a stop trigger that activates when a digital sensor reaches the target state.
     *
     * <p>This is a convenience method for common cases such as:
     * <ul>
     *   <li>Stop when a limit switch is pressed</li>
     *   <li>Stop when a beam break is triggered</li>
     * </ul>
     * </p>
     *
     * @param index Index of the sensor in the mechanism
     * @param targetState The boolean state required to trigger the stop
     * @return this mechanism for chaining
     * @throws IllegalArgumentException if index is invalid or sensor is not DigitalSensorInterface
     */
    public T addStopWhenSensor(int index, boolean targetState) {
        if (!isValidSensorIndex(index)) {
            throw new IllegalArgumentException(
                "addStopWhenSensor (digital): invalid sensor index " + index
            );
        }

        if (!(sensors[index] instanceof DigitalSensorInterface)) {
            throw new IllegalArgumentException(
                "addStopWhenSensor (digital): sensor at index " + index +
                " is not a DigitalSensorInterface"
            );
        }

        DigitalSensorInterface digital = (DigitalSensorInterface) sensors[index];
        return (T) addStop(() -> digital.get() == targetState);
    }

    /**
     * Adds a stop trigger that activates when an analog sensor reads a given value.
     *
     * <p>Useful for mechanisms that stop at specific analog positions such as:
     * <ul>
     *   <li>Potentiometers</li>
     *   <li>Analog range/limit sensors</li>
     * </ul>
     * </p>
     *
     * @param index Index of the sensor in the mechanism
     * @param targetState The analog value at which the stop should activate
     * @return this mechanism for chaining
     * @throws IllegalArgumentException if index is invalid or sensor is not AnalogSensorInterface
     */
    public T addStopWhenSensor(int index, double targetState) {
        if (!isValidSensorIndex(index)) {
            throw new IllegalArgumentException(
                "addStopWhenSensor (analog): invalid sensor index " + index
            );
        }

        if (!(sensors[index] instanceof AnalogSensorInterface)) {
            throw new IllegalArgumentException(
                "addStopWhenSensor (analog): sensor at index " + index +
                " is not an AnalogSensorInterface"
            );
        }

        AnalogSensorInterface analog = (AnalogSensorInterface) sensors[index];
        return (T) addStop(() -> analog.get() == targetState);
    }

    /**
     * Sets limits for a specific motor.
     * 
     * <p>Values will be clamped to [min, max] range before being sent to the motor.</p>
     * 
     * @param motorIndex Index of motor
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @return this mechanism for chaining
     */
    @SuppressWarnings("unchecked")
    public T setMotorLimits(int motorIndex, double min, double max) {
        if (!isValidMotorIndex(motorIndex)) {
            throw new IllegalArgumentException("Invalid motor index: " + motorIndex);
        }
        motorsLimits[motorIndex] = new MotorLimits(min, max);
        return (T) this;
    }

    /**
     * Sets only a minimum limit for a specific motor.
     * 
     * @param motorIndex Index of motor
     * @param min Minimum allowed value
     * @return this mechanism for chaining
     */
    public T setMotorMinLimit(int motorIndex, double min) {
        return setMotorLimits(motorIndex, min, Double.POSITIVE_INFINITY);
    }

    /**
     * Sets only a maximum limit for a specific motor.
     * 
     * @param motorIndex Index of motor
     * @param max Maximum allowed value
     * @return this mechanism for chaining
     */
    public T setMotorMaxLimit(int motorIndex, double max) {
        return setMotorLimits(motorIndex, Double.NEGATIVE_INFINITY, max);
    }

    /**
     * Removes limits for a specific motor.
     * 
     * @param motorIndex Index of motor
     * @return this mechanism for chaining
     */
    @SuppressWarnings("unchecked")
    public T removeMotorLimits(int motorIndex) {
        if (isValidMotorIndex(motorIndex)) {
            motorsLimits[motorIndex] = null;
        }
        return (T) this;
    }

    /**
     * Gets the limits for a specific motor.
     * 
     * @param motorIndex Index of motor
     * @return MotorLimits object or null if no limits set
     */
    public MotorLimits getMotorLimits(int motorIndex) {
        if (isValidMotorIndex(motorIndex)) {
            return motorsLimits[motorIndex];
        }
        return null;
    }

    /**
     * Applies motor limits to current values array.
     */
    private void applyMotorLimits() {
        if (values == null) return;
        
        for (int i = 0; i < values.length && i < motorsLimits.length; i++) {
            if (motorsLimits[i] != null) {
                values[i] = motorsLimits[i].clamp(values[i]);
            }
        }
    }

    /**
     * Sets a calibration check function.
     * 
     * <p>Mechanism will only run if calibration supplier returns true.
     * Useful for mechanisms that need zeroing before use.</p>
     * 
     * @param isCalibratedSupplier Supplier that returns calibration status
     * @return this mechanism for chaining
     */
    @SuppressWarnings("unchecked")
    public T withCalibrationValue(Supplier<Boolean> isCalibratedSupplier){
        this.isCalibratedSupplier = isCalibratedSupplier;
        LogManager.addEntry(name + "/is calibrated", isCalibratedSupplier);
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
        if (!isCalibratedSupplier.get()) {
            System.err.println("Mechanism " + name + " is not calibrated, cannot run");
            return;
        }
        if (consumer == null) {
            System.err.println("Consumer is null, cannot run mechanism");
            return;
        }
        if (values == null) {
            System.err.println("Values is null, cannot run mechanism");
            return;
        }

        applyMotorLimits();
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
     * Sets power for all motors.
     * 
     * @param power Duty cycle (-1.0 to 1.0)
     */
    public void setAll(double power) {
        for (int i = 0; i < motors.length; i++) {
            values[i] = power;
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

    public void setNeutralMode(int motorIndex, boolean isBrake){
        if (isValidMotorIndex(motorIndex)){
            motors[motorIndex].setNeutralMode(isBrake);
        }
    }

    /**
     * Checks electronics for all motors and sensors.
     * 
     * <p>Logs any faults to console and telemetry.</p>
     */
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
