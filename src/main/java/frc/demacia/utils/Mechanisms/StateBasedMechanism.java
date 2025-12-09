package frc.demacia.utils.Mechanisms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import frc.demacia.utils.Motors.MotorInterface;
import frc.demacia.utils.Sensors.SensorInterface;

/**
 * State machine-based mechanism controller.
 * 
 * <p>Extends BaseMechanism with enum-based state control and automatic state transitions.</p>
 * 
 * <p><b>Features:</b></p>
 * <ul>
 *   <li>Enum-based state definition</li>
 *   <li>Conditional state transitions</li>
 *   <li>Dashboard state selection</li>
 *   <li>Manual test mode</li>
 * </ul>
 * 
 * <p><b>Example Usage:</b></p>
 * <pre>
 * public enum IntakeStates implements Intake.IntakeState {
 *     IDLE(new double[]{0, 0}),
 *     INTAKING(new double[]{0.8, 0.8}),
 *     OUTTAKING(new double[]{-0.5, -0.5});
 *     
 *     private double[] values;
 *     IntakeStates(double[] values) { this.values = values; }
 *     public double[] getValues() { return values; }
 * }
 * 
 * Intake intake = new Intake("Intake", motors, sensors, IntakeStates.class)
 *     .withStartingOption(IntakeStates.IDLE)
 *     .addTrigger(() -> controller.getAButton(), IntakeStates.INTAKING)
 *     .addTrigger(() -> beamBreak.get(), IntakeStates.IDLE, IntakeStates.INTAKING);
 * </pre>
 * 
 * @param <T> The concrete mechanism type
 */
public class StateBasedMechanism<T extends StateBasedMechanism<T>> extends BaseMechanism<T> {

    public interface MechanismState {
        double[] getValues();
    }

    public static class StateTrigger {
        private final Supplier<Boolean> condition;
        private final Enum<?> state;
        private final Enum<?> when;

        public StateTrigger(Supplier<Boolean> condition, Enum<?> state) {
            this.condition = condition;
            this.state = state;
            when = null;
        }

        public StateTrigger(Supplier<Boolean> condition, Enum<?> state, Enum<?> when) {
            this.condition = condition;
            this.state = state;
            this.when = when;
        }

        public boolean check() {
            return condition.get();
        }

        public Enum<?> getState() {
            return state;
        }

        public Enum<?> getWhen() {
            return when;
        }
    }

    protected double[] testValues;
    protected double[] idleValues;
    protected double[] idleValues2;

    protected Enum<?> state;
    protected Enum<?> testingState;
    protected Enum<?> idleState;
    protected Enum<?> idleState2;

    private List<StateTrigger> triggers = new ArrayList<>();
    
    SendableChooser<Enum<?>> stateChooser = new SendableChooser<>();

    public StateBasedMechanism(String name, MotorInterface[] motors, SensorInterface[] sensors, Class<? extends Enum<? extends MechanismState>> enumClass, BiConsumer<MotorInterface[], double[]> consumer) {
        super(name, motors, sensors, consumer);
        this.addNT(enumClass);
        if (enumClass == null) {
            throw new IllegalArgumentException("Enum class cannot be null for mechanism: " + name);
        }
        Enum<?>[] enumConstants = enumClass.getEnumConstants();
        if (enumConstants == null || enumConstants.length == 0) {
            throw new IllegalArgumentException("Enum class must have at least one constant for mechanism: " + name);
        }

        super.consumer = consumer;
        testValues = new double[motors.length];
        idleValues = new double[motors.length];
        idleValues2 = new double[motors.length];
        for (int i = 0; i < motors.length; i++){
            idleValues[i] = 0;//TODO
            idleValues2[i] = 0;//TODO
        }
        SmartDashboard.putData(this);
    }

    private void addNT(Class<? extends Enum<? extends MechanismState>> enumClass) {
        for (Enum<?> state : enumClass.getEnumConstants()) {
            stateChooser.addOption(state.name(), state);
        }
        stateChooser.addOption("TESTING", testingState);
        stateChooser.addOption("IDLE", idleState);
        stateChooser.addOption("IDLE2", idleState2);

        stateChooser.onChange(state -> this.state = state);

        SmartDashboard.putData(getName() + "/State Chooser", stateChooser);
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        super.initSendable(builder);
        builder.addDoubleArrayProperty(getName() + "/Test Values", () -> getTestValues(), testValues -> setTestValues(testValues));
    }

    /**
     * Sets the starting state for this mechanism.
     * 
     * @param state Initial state (must implement MechanismState)
     * @return this mechanism for chaining
     * @throws IllegalArgumentException if state doesn't implement MechanismState
     */
    @SuppressWarnings("unchecked")
    public T withStartingOption(Enum<?> state){
        if (state == null) {
            throw new IllegalArgumentException("Starting state cannot be null");
        }
        
        if (!(state instanceof MechanismState)) {
            throw new IllegalArgumentException("Starting state must implement MechanismState");
        }

        stateChooser.setDefaultOption(state.name(), state);
        this.state = state;
        return (T) this;
    }

    /**
     * Adds a state transition trigger.
     * 
     * <p>When condition becomes true, mechanism transitions to target state.</p>
     * 
     * @param condition When to transition
     * @param state Target state
     * @return this mechanism for chaining
     */
    @SuppressWarnings("unchecked")
    public T addTrigger(Supplier<Boolean> condition, Enum<?> state) {
        if (!(state instanceof MechanismState)) {
            throw new IllegalArgumentException("Target state must implement MechanismState");
        }
        
        triggers.add(new StateTrigger(condition, state));
        return (T) this;
    }

    /**
     * Adds a conditional state transition trigger.
     * 
     * <p>Transition only occurs if currently in the specified state.</p>
     * 
     * @param condition When to transition
     * @param state Target state
     * @param atState Only transition when in this state
     * @return this mechanism for chaining
     */
    @SuppressWarnings("unchecked")
    public T addTrigger(Supplier<Boolean> condition, Enum<?> state, Enum<?> atState) {
        if (!(state instanceof MechanismState)) {
            throw new IllegalArgumentException("Target state must implement MechanismState");
        }
        
        triggers.add(new StateTrigger(condition, state, atState));
        return (T) this;
    }

    /**
     * Gets the current state.
     * 
     * @return Current state enum
     */
    public void setState(Enum<?> state) {
        this.state = state;
    }

    /**
     * Gets the current state.
     * 
     * @return Current state enum
     */
    public Enum<?> getState() {
        return state;
    }

    /**
     * Creates a command that runs the state machine.
     * 
     * <p>Checks state triggers and updates mechanism each cycle.</p>
     * 
     * @return Command that runs this state machine
     */
    public Command StateMechanismCommand(Enum<?> state){
        setState(state);
        checkStateTriggers();
        setState();
        return mechanismCommand();
    }

    /**
     * Creates a command that runs the state machine.
     * 
     * <p>Checks state triggers and updates mechanism each cycle.</p>
     * 
     * @return Command that runs this state machine
     */
    public Command runStateMechanismCommand(){
        checkStateTriggers();
        setState();
        return mechanismCommand();
    }

    private void checkStateTriggers() {
        for (StateTrigger trigger : triggers) {
            if (trigger.check() && (state == trigger.getWhen() || trigger.getWhen() == null)) {
                state = trigger.getState();
            }
        }
    }

    private void setState(){
        if (state == testingState) {
            values = testValues;
            return;
        }
        if (state == idleState) {
            values = idleValues;
            return;
        }
        if (state == idleState2) {
            values = idleValues2;
            return;
        }
        values = ((MechanismState) state).getValues();
    }

    private double[] getTestValues(){
        return testValues;
    }

    private void setTestValues(double[] testValues){
        this.testValues = testValues;
    }
}