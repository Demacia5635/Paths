package frc.demacia.utils.Mechanisms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.demacia.utils.Log.LogManager;
import frc.demacia.utils.Controller.CommandController;
import frc.demacia.utils.Log.LogEntryBuilder.LogLevel;
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
    
    protected boolean isControllerCommand = false;
    protected int controlledMotorIndex;
    protected double controllerMultiplier = 0.8;
    protected CommandController controller;

    protected List<Command> commands;

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
     * Configures controller-based manual control for a specific motor.
     * 
     * <p>Enables direct joystick control of a motor, bypassing the mechanism's
     * normal state machine or command logic. Useful for testing, tuning, and
     * manual calibration procedures.</p>
     * 
     * <p><b>Features:</b></p>
     * <ul>
     *   <li>Manual control via controller left Y axis</li>
     *   <li>Toggle between normal and controller mode via SmartDashboard</li>
     *   <li>Default power scaling of 0.8 (configurable via controllerMultiplier field)</li>
     *   <li>Active while mechanism command is running</li>
     * </ul>
     * 
     * <p><b>Dashboard Control:</b></p>
     * After calling this method, a chooser appears in SmartDashboard/Shuffleboard at:
     * <pre>"MechanismName/Is Controller Command"</pre>
     * Toggle between TRUE (controller mode) and FALSE (normal mode) during runtime.
     * Default selection is TRUE.
     * 
     * <p><b>Behavior:</b></p>
     * <ul>
     *   <li>When TRUE: Motor responds to controller.getLeftY() * controllerMultiplier (default 0.8)</li>
     *   <li>When FALSE: Mechanism operates normally (states/triggers/values)</li>
     *   <li>State persists across enable/disable cycles</li>
     * </ul>
     * 
     * @param controller The controller to use for manual control
     * @param controlledMotorIndex Index of the motor to control (0-based)
     * @return this mechanism for chaining
     * @throws IllegalArgumentException if controller is null or motor index is invalid
     * 
     * @example
     * <pre>
     * // Basic setup - control motor 0 with test controller
     * mechanism.withController(testController, 0);
     * 
     * // In SmartDashboard:
     * // "Intake/Is Controller Command" → Select TRUE (default)
     * // Now left stick Y controls motor 0
     * 
     * // Multiple motors example:
     * leftArm.withController(testController, 0);   // Left stick controls left arm
     * rightArm.withController(testController, 0);  // Same controller, different mechanism
     * 
     * // To customize power scaling:
     * mechanism.withController(testController, 0);
     * mechanism.controllerMultiplier = 0.5; // 50% max power
     * </pre>
     * 
     * @see #mechanismCommand() for command execution behavior
     */
    @SuppressWarnings("unchecked")
    public T withController(CommandController controller, int controlledMotorIndex) {
        if (!isValidMotorIndex(controlledMotorIndex)) {
            throw new IllegalArgumentException("Invalid controlled motor index: " + controlledMotorIndex);
        }
        if (controller == null) {
            throw new IllegalArgumentException("Controller cannot be null");
        }
        this.controller = controller;
        this.controlledMotorIndex = controlledMotorIndex;
        
        SendableChooser<Boolean> controllerCommandChooser = new SendableChooser<>();
        controllerCommandChooser.setDefaultOption("TRUE", true);
        controllerCommandChooser.addOption("FALSE", false);
        controllerCommandChooser.onChange(value -> {
            this.isControllerCommand = value;
        });
        SmartDashboard.putData(getName() + "/Is Controller Command", controllerCommandChooser);
        
        return (T) this;
    }

    public Command withCommand(BiConsumer<MotorInterface[], double[]> executeAction, Supplier<Boolean> finishCondition, BiConsumer<MotorInterface[], double[]> endAction){
        Command command = new Command() {
            @Override
            public void execute() {
                executeAction.accept(motors, values);
            }

            @Override
            public boolean isFinished() {
                return finishCondition.get();
            }

            @Override
            public void end(boolean interrupted) {
                endAction.accept(motors, values);
            }
        };
        commands.add(command);
        return command;
    }

    public Command getCommand(int index){
        return commands.get(index);
    }
    
    /**
     * Creates a command.
     * 
     * <p>The command checks triggers and applies the consumer each cycle.</p>
     * 
     * @return Command that requires this subsystem
     */
    public RunCommand mechanismCommand(){
        return new RunCommand(() -> {
            if (isControllerCommand){
                ControlerCommand();
            } else{
                checkTriggers();
                runMechanism();
            }
        },this);
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

    private void ControlerCommand(){
        double power = controller.getLeftY() * controllerMultiplier; //TODO
        getMotor(controlledMotorIndex).setDuty(power);
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
