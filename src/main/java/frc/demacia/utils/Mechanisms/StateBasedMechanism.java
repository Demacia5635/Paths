package frc.demacia.utils.Mechanisms;

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

public class StateBasedMechanism extends BaseMechanism {

    public interface MechanismState {
        double[] getValues();
        BiConsumer<Pair<MotorInterface[], SensorInterface[]>, double[]> getConsumer();
    }

    protected double[] Values;
    protected double[] testValues;

    BiConsumer<Pair<MotorInterface[], SensorInterface[]>, double[]> consumer;
    BiConsumer<Pair<MotorInterface[], SensorInterface[]>, double[]> testConsumer;
    Supplier<Boolean> isCalibratedSupplier = () -> true;

    protected Enum<?> state;
    
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

    public StateBasedMechanism withStartingOption(Enum<?> state){
        if (state == null) {
            throw new IllegalArgumentException("Starting state cannot be null");
        }
        
        if (!(state instanceof MechanismState)) {
            throw new IllegalArgumentException("Starting state must implement MechanismState");
        }

        testConsumer = ((MechanismState) state).getConsumer();
        this.state = state;
        return this;
    }

    public StateBasedMechanism withCalibrationValue(Supplier<Boolean> isCalibratedSupplier){
        this.isCalibratedSupplier = isCalibratedSupplier;
        LogManager.addEntry(name + "/is calibrated", isCalibratedSupplier);
        return this;
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