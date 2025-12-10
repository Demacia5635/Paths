package frc.demacia.utils.Mechanisms;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.demacia.utils.Motors.MotorInterface;

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

    public class State {
        private String name;
        private double[] values;
        private List<BiConsumer<MotorInterface[], double[]>> motorAndValuesInitializes;
        private List<Consumer<MotorInterface[]>> motorInitializes;
        private List<Supplier<Boolean>> finishes;
        private List<BiConsumer<MotorInterface[], double[]>> motorAndValuesEnds;
        private List<Consumer<MotorInterface[]>> motorEnds;

        public State(String name, double[] values){
            this.name = name;
            this.values = values;
        }

        public State withInitialize(BiConsumer<MotorInterface[], double[]> consumer){
            motorAndValuesInitializes.add(consumer);
            return this;
        }

        public State withInitialize(Consumer<MotorInterface[]> consumer){
            motorInitializes.add(consumer);
            return this;
        }

        public State withFinish(Supplier<Boolean> finish){
            finishes.add(finish);
            return this;
        }

        public State withEnd(BiConsumer<MotorInterface[], double[]> consumer){
            motorAndValuesEnds.add(consumer);
            return this;
        }

        public State withEnd(Consumer<MotorInterface[]> consumer){
            motorEnds.add(consumer);
            return this;
        }

        public String getName(){
            return name;
        }

        public void setValues(double[] values){
            this.values = values;
        }

        public double[] getValues(){
            return values;
        }

        public List<BiConsumer<MotorInterface[], double[]>> getMotorAndValuesInitializes(){
            return motorAndValuesInitializes;
        }

        public List<Consumer<MotorInterface[]>> getMotorInitializes(){
            return motorInitializes;
        }

        public List<Supplier<Boolean>> getFinishes(){
            return finishes;
        }

        public List<BiConsumer<MotorInterface[], double[]>> getMotorAndValuesEnd(){
            return motorAndValuesEnds;
        }

        public List<Consumer<MotorInterface[]>> getMotorEnds(){
            return motorEnds;
        }
    }

    public String name;

    SendableChooser<State> stateChooser = new SendableChooser<>();
    public State state;
    protected State testingState;
    protected State idleState;
    protected State idleState2;

    public StateBasedMechanism(String name){
        super(name);
    }

    @SuppressWarnings("unchecked")
    public T withState(State state){
        stateChooser.addOption(state.getName(), state);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T bindButton(Trigger button, State state){
        button.onTrue(new Command() {
            @Override
            public void initialize() {
                for (BiConsumer<MotorInterface[], double[]> motorAndValuesInitialize : state.getMotorAndValuesInitializes()){
                    motorAndValuesInitialize.accept(motors, values);
                }
                for (Consumer<MotorInterface[]> motorInitialize : state.getMotorInitializes()){
                    motorInitialize.accept(motors);
                }
            }

            @Override
            public void execute() {
                consumer.accept(motors, values);
            }

            @Override
            public boolean isFinished() {
                for (Supplier<Boolean> finish : state.getFinishes()){
                    if (finish.get()){
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void end(boolean interrupted) {
                stopAll();
                for (BiConsumer<MotorInterface[], double[]> motorAndValuesEnd : state.getMotorAndValuesEnd()){
                    motorAndValuesEnd.accept(motors, values);
                }
                for (Consumer<MotorInterface[]> motorEnd : state.getMotorEnds()){
                    motorEnd.accept(motors);
                }
            }
        });
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T build(){
        super.build();
        double[] idleValues = new double[motors.length];
        testingState = new State(name, idleValues);
        idleState  = new State(name, idleValues);
        idleState2 = new State(name, idleValues);
        stateChooser.addOption("TESTING", testingState);
        stateChooser.addOption("IDLE", idleState);
        stateChooser.addOption("IDLE2", idleState2);
        stateChooser.onChange(state -> this.state = state);
        SmartDashboard.putData(getName() + "/State Chooser", stateChooser);
        return (T) this;
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
    public T withStartingOption(State state){
        if (state == null) {
            throw new IllegalArgumentException("Starting state cannot be null");
        }
        
        if (!(state instanceof MechanismState)) {
            throw new IllegalArgumentException("Starting state must implement MechanismState");
        }

        stateChooser.setDefaultOption(state.getName(), state);
        this.state = state;
        return (T) this;
    }

    /**
     * Gets the current state.
     * 
     * @return Current state enum
     */
    public void setState(State state) {
        this.state = state;
    }

    /**
     * Gets the current state.
     * 
     * @return Current state enum
     */
    public State getState() {
        return state;
    }

    public double[] getTestValues(){
        return testingState.getValues();
    }

    public void setTestValues(double[] testValues){
        testingState.setValues(testValues);
    }
}