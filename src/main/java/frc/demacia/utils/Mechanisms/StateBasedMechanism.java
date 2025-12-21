package frc.demacia.utils.Mechanisms;

import java.util.HashMap;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.demacia.utils.Log.LogManager;
import frc.demacia.utils.Motors.MotorInterface;
import frc.demacia.utils.Sensors.SensorInterface;

public class StateBasedMechanism extends BaseMechanism {

    public static class State {
        private String name;
        private double[] values;

        public State(String name, double[] values){
            if (values == null) {
                LogManager.log("Values cannot be null");
                return;
            }
            this.name = name;
            this.values = values;
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
    }

    public String name;

    SendableChooser<State> stateChooser = new SendableChooser<>();
    public State state;
    protected State testingState;
    protected State idleState;

    protected double[] testValues;

    protected HashMap<String, State> states = new HashMap<>();

    public StateBasedMechanism(String name, MotorInterface[] motors, SensorInterface[] sensors){
        super(name, motors, sensors);
        testValues = new double[motors.length];
        double[] idleValues = new double[motors.length];
        testingState = new State("TESTING", testValues);
        idleState  = new State("IDLE", idleValues);
        stateChooser.addOption(testingState.getName(), testingState);
        stateChooser.addOption(idleState.getName(), idleState);
        stateChooser.addOption(idleState.getName() + "2", idleState);
        stateChooser.onChange(state -> this.state = state);
        SmartDashboard.putData(getName() + "/State Chooser", stateChooser);
    }

    public void addState(String name, double[] values){
        State state = new State(name, values);
        stateChooser.addOption(state.getName(), state);
        states.put(state.getName(), state);
    }

    /**
     * Sets the starting state for this mechanism.
     * 
     * @param state Initial state (must implement MechanismState)
     * @return this mechanism for chaining
     */
    public void setStartingOption(String stateName){
        if (state == null) {
            LogManager.log("Starting state cannot be null");
            return;
        }

        stateChooser.setDefaultOption(state.getName(), states.get(stateName));
        this.state = states.get(stateName);
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.addDoubleArrayProperty(getName() + "/Test Values", () -> getTestValues(), testValues -> setTestValues(testValues));
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
        return testValues != null? testValues : new double[0];
    }

    public void setTestValues(double[] testValues){
        if (testingState == null) return;
        testingState.setValues(testValues);
        this.testValues = testValues;
    }
}