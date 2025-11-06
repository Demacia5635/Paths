package frc.demacia.utils.Mechanisms;

import java.util.function.BiConsumer;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;
import frc.demacia.utils.Motors.MotorInterface;
import frc.demacia.utils.Sensors.SensorInterface;

public class StateBasedMechanism extends BaseMechanism {

    public interface StateEnum {
        double[] getValues();
    }

    protected double[] Values;
    protected double[] testValues;

    BiConsumer<MotorInterface[], double[]> consumer;

    protected Enum<?> state;

    public StateBasedMechanism(String name, MotorInterface[] motors, SensorInterface[] sensors, Class<? extends Enum<? extends StateEnum>> enumClass, BiConsumer<MotorInterface[], double[]> consumer) {
        super(name, motors, sensors);
        this.consumer = consumer;
        this.addNT(enumClass);
        testValues = new double[motors.length];
        SmartDashboard.putData(this);
    }

    public void addNT(Class<? extends Enum<? extends StateEnum>> enumClass) {
            SendableChooser<Enum<?>> stateChooser = new SendableChooser<>();
            
            for (Enum<?> s : enumClass.getEnumConstants()) {
                stateChooser.addOption(s.name(), s);
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

    public Command armCommand(){
        return new RunCommand(() -> setToState(), this);
    }

    private void setToState(){
        setState();
        consumer.accept(motors, Values);
    }

    private void setState(){
        if (state == null) {
            Values = testValues;
            return;
        };
        for (int i = 0; i < Values.length; i++){
            Values[i] = ((StateEnum) state).getValues()[i];
        }
    }

    private double[] getTestValues(){
        return testValues;
    }

    private void setTestValues(double[] testValues){
        this.testValues = testValues;
    }
}