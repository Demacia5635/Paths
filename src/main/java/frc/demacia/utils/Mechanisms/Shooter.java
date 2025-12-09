package frc.demacia.utils.Mechanisms;

import java.util.function.Supplier;

import edu.wpi.first.wpilibj2.command.RunCommand;
import frc.demacia.utils.LookUpTable;
import frc.demacia.utils.Motors.MotorInterface;
import frc.demacia.utils.Sensors.SensorInterface;

/**
 * State-based shooter mechanism.
 * 
 * <p>Uses feed forward for velocity. lookup table define target velocity.</p>
 * 
 * </pre>
 */
public class Shooter extends BaseMechanism<Shooter>{

    private LookUpTable lookUpTable;
    private Supplier<Double> posSupplier;

    public Shooter(String name, MotorInterface[] motors, LookUpTable lookUpTable, Supplier<Double> posSupplier) {
        super(name, motors, new SensorInterface[0],
        (motor, values) -> {
            for (int i = 0; i < motor.length && i < values.length; i++) {
                motor[i].setVelocity(values[i]);
            }});
        this.lookUpTable = lookUpTable;
        this.posSupplier = posSupplier;
    }

    @Override
    public RunCommand mechanismCommand(){
        setValues(lookUpTable.get(posSupplier.get()));
        return super.mechanismCommand();
    }
}
