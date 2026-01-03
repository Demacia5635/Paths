package frc.demacia.utils.mechanisms;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.demacia.utils.log.LogManager;
import frc.demacia.utils.motors.MotorInterface;
import frc.demacia.utils.sensors.SensorInterface;

/**
 * An extension of BaseMechanism that introduces the concept of States.
 * <p>
 * This class allows controlling the mechanism using a State Machine approach.
 * Each state defines a set of target values (e.g., positions or velocities) for the motors.
 * It includes a {@link SendableChooser} on the dashboard to manually switch states for testing.
 * </p>
 */
public class StateBaseMechanism extends BaseMechanism {

    /**
     * Interface representing a state of the mechanism.
     * Usually implemented by an Enum.
     */
    public interface MechanismState {
        /** @return The target values for the motors in this state */
        double[] getValues();
        /** @return The name of the state */
        String name();
    }

    /** The name of the mechanism */
    public String name;

    /** Chooser for selecting states via the Dashboard */
    SendableChooser<MechanismState> stateChooser = new SendableChooser<>();
    
    /** The current active state */
    public MechanismState state;
    
    /** * Default IDLE state.
     * Sets all motor targets to 0.
     */
    private final MechanismState IDLE_STATE = new MechanismState() {
        @Override 
        public double[] getValues() { 
            return new double[motors != null ? motors.size() : 0]; 
        }
        @Override
        public String name() {
            return "IDLE";
        }
    };

    /**
     * Special TESTING state.
     * Uses values from a specific 'Test Values' array that can be edited on the Dashboard.
     */
    private final MechanismState TESTING_STATE = new MechanismState() {
        @Override 
        public double[] getValues() { 
            return getTestValues(); 
        }
        @Override
        public String name() {
            return "TESTING";
        }
    };

    /** Stores the values used when in TESTING state */
    protected double[] testValues;

    /**
     * Constructs a new StateBaseMechanism.
     * @param name The name of the mechanism
     * @param motors Array of motors
     * @param sensors Array of sensors
     * @param enumClass The Enum class defining the mechanism's states
     */
    public StateBaseMechanism(String name, MotorInterface[] motors, SensorInterface[] sensors, Class<? extends MechanismState> enumClass){
        super(name, motors, sensors);
        testValues = new double[motors.length];
        addNT(enumClass);
    }

    /**
     * Populates the NetworkTable (Dashboard) with the state chooser.
     * Adds TESTING, IDLE, and all values from the provided Enum.
     * @param enumClass The state Enum class
     */
    private void addNT(Class<? extends MechanismState> enumClass) {
        stateChooser.addOption("TESTING", TESTING_STATE);
        stateChooser.addOption("IDLE", IDLE_STATE);
        stateChooser.addOption("IDLE2", IDLE_STATE); 
        
        for (MechanismState state : enumClass.getEnumConstants()) {
            stateChooser.addOption(state.name(), state);
        }
        
        // Listener to update the local state variable when dashboard selection changes
        stateChooser.onChange(state -> this.state = state);
        
        SmartDashboard.putData(getName() + "/State Chooser", stateChooser);
    }

    /**
     * Sets the default option selected in the dashboard chooser on startup.
     * @param state The state to be default
     */
    public void setStartingOption(MechanismState state){
        if (state == null) {
            LogManager.log("Starting state cannot be null");
            return;
        }

        stateChooser.setDefaultOption(state.name(), state);
    }

    /**
     * Initializes the Sendable data.
     * Adds the 'Test Values' array property to the dashboard so it can be edited live.
     */
    @Override
    public void initSendable(SendableBuilder builder) {
        builder.addDoubleArrayProperty(getName() + "/Test Values", () -> getTestValues(), testValues -> setTestValues(testValues));
    }

    /**
     * Manually sets the current state of the mechanism.
     * @param state The new state
     */
    public void setState(MechanismState state) {
        this.state = state;
    }

    /**
     * @return The current state of the mechanism
     */
    public MechanismState getState() {
        return state;
    }

    /**
     * @return The array of values used for the TESTING state
     */
    public double[] getTestValues(){
        return testValues != null? testValues : new double[0];
    }

    /**
     * Updates the values used for the TESTING state.
     * @param testValues The new values array
     */
    public void setTestValues(double[] testValues){
        this.testValues = testValues;
    }
}