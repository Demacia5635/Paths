package frc.demacia.utils.Sensors;

import edu.wpi.first.wpilibj.AnalogInput;

public class LidarSensor extends AnalogInput implements SensorInterface {
    private final String name;
    private final LidarSensorConfig config;

    public LidarSensor(LidarSensorConfig config) {
        super(config.port); 
        this.config = config;
        this.name = config.name;
    }

    public double getDistanceCM() {
        double voltage = getVoltage();
        return voltage * config.conversionFactor; 
    }

    public boolean isValid() {
        return getDistanceCM() > config.minRange && getDistanceCM() < config.maxRange;
    }

    public String getName() {
        return name;
    }
}
