package frc.demacia.utils.Sensors;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.Ultrasonic;
import frc.demacia.utils.Log.LogManager;
public class UltraSonicSensor extends Ultrasonic implements AnalogSensorInterface {
    public UltraSonicSensor(UltraSonicSensorConfig config) {
        super(config.pingChannel, config.channel);
        this.config = config;
        name = config.name;
     setAutomaticMode(true);
        addLog();
		LogManager.log(name + "UltraSonicSensor initialized");
    }

    String name;
    UltraSonicSensorConfig config;

    /**
     * @return the range in meters;
     */
    public double get() {
        return getRangeMeters();
    }


    public String getName() {
        return config.name;
    }
 
    @Override
    public void ping() {
        super.ping();
    }

    public double getRangeMeters() {
        return getRangeMM() / 1000d;
    }
    
    private void addLog() {
        LogManager.addEntry(name + "range", () -> getRangeMeters(), 3);
            getRangeMeters();
    }

    public void checkElectronics(){
        if (!isRangeValid()) {
            LogManager.log(name + " is at invalid range");
        }
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.addDoubleProperty("value", this::get, null);
    }
}
