package frc.demacia.utils.Mechanisms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import edu.wpi.first.math.Pair;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;
import frc.demacia.utils.Log.LogManager;
import frc.demacia.utils.Motors.MotorInterface;
import frc.demacia.utils.Sensors.SensorInterface;

public class StateBasedMechanism<T extends StateBasedMechanism<T>> extends BaseMechanism {

    public interface MechanismState {
        double[] getValues();
        BiConsumer<Pair<MotorInterface[], SensorInterface[]>, double[]> getConsumer();
    }

    public static class Trigger {
        private final Supplier<Boolean> condition;
        private final Enum<?> state;

        public Trigger(Supplier<Boolean> condition, Enum<?> state) {
            this.condition = condition;
            this.state = state;
        }

        public boolean check() {
            return condition.get();
        }

        public Enum<?> getState() {
            return state;
        }
    }

    protected double[] Values;
    protected double[] testValues;

    BiConsumer<Pair<MotorInterface[], SensorInterface[]>, double[]> consumer;
    BiConsumer<Pair<MotorInterface[], SensorInterface[]>, double[]> testConsumer;
    Supplier<Boolean> isCalibratedSupplier = () -> true;

    protected Enum<?> state;

    private List<Trigger> triggers = new ArrayList<>();
    
    SendableChooser<Enum<?>> stateChooser = new SendableChooser<>();

    public StateBasedMechanism(String name, MotorInterface[] motors, SensorInterface[] sensors, Class<? extends Enum<? extends MechanismState>> enumClass) {
        super(name, motors, sensors);
        this.addNT(enumClass);
        if (enumClass == null) {
            throw new IllegalArgumentException("Enum class cannot be null for mechanism: " + name);
        }
        Enum<?>[] enumConstants = enumClass.getEnumConstants();
        if (enumConstants == null || enumConstants.length == 0) {
            throw new IllegalArgumentException("Enum class must have at least one constant for mechanism: " + name);
        }

        testConsumer = ((MechanismState) enumClass.getEnumConstants()[0]).getConsumer();
        Values = new double[motors.length];
        testValues = new double[motors.length];
        SmartDashboard.putData(this);
    }

    public void addNT(Class<? extends Enum<? extends MechanismState>> enumClass) {
        for (Enum<?> state : enumClass.getEnumConstants()) {
            stateChooser.addOption(state.name(), state);
        }
        stateChooser.addOption("TESTING", null);

        stateChooser.onChange(state -> this.state = state);

        SmartDashboard.putData(getName() + "/State Chooser", stateChooser);
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        super.initSendable(builder);
        builder.addDoubleArrayProperty(getName() + "/Test Values", () -> getTestValues(), testValues -> setTestValues(testValues));
    }

    public Command toStateCommand(){
        return new RunCommand(() -> setToState(), this);
    }

    @SuppressWarnings("unchecked")
    public T withStartingOption(Enum<?> state){
        if (state == null) {
            throw new IllegalArgumentException("Starting state cannot be null");
        }
        
        if (!(state instanceof MechanismState)) {
            throw new IllegalArgumentException("Starting state must implement MechanismState");
        }

        testConsumer = ((MechanismState) state).getConsumer();
        this.state = state;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withCalibrationValue(Supplier<Boolean> isCalibratedSupplier){
        this.isCalibratedSupplier = isCalibratedSupplier;
        LogManager.addEntry(name + "/is calibrated", isCalibratedSupplier);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addTrigger(Supplier<Boolean> condition, Enum<?> state) {
        if (!(state instanceof MechanismState)) {
            throw new IllegalArgumentException("Target state must implement MechanismState");
        }
        
        triggers.add(new Trigger(condition, state));
        return (T) this;
    }

    public void periodic() {
        for (Trigger trigger : triggers) {
            if (trigger.check()) {
                state = trigger.getState();
            }
        }
    }

    public Enum<?> getState() {
        return state;
    }

    private void setToState(){
        if (!isCalibratedSupplier.get()) return;
        setState();
        if (consumer == null) {
            System.err.println("Consumer is null, cannot set state");
            return;
        }

        consumer.accept(new Pair<>(motors, sensors), Values);
    }

    private void setState(){
        if (state == null) {
            consumer = testConsumer;
            Values = testValues;
            return;
        };
        consumer = ((MechanismState) state).getConsumer();
        Values = ((MechanismState) state).getValues();
    }

    private double[] getTestValues(){
        return testValues;
    }

    private void setTestValues(double[] testValues){
        this.testValues = testValues;
    }
}