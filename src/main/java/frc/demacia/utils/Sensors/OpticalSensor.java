package frc.demacia.utils.Sensors;
import edu.wpi.first.wpilibj.AnalogInput;

public class OpticalSensor extends AnalogInput implements SensorInterface {
    private final OpticalSensorConfig config;
    private final String name;

    public OpticalSensor(OpticalSensorConfig config) {
        super(config.port);
        this.config = config;
        this.name = config.name;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return super.getValue();
    }

    public double get(){
        return getValue();
    }

    public double getVoltage() {
        return super.getVoltage();
    }

 

}
