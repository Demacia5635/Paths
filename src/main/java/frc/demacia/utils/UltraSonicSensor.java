package frc.demacia.utils;

import edu.wpi.first.wpilibj.Ultrasonic;
import frc.demacia.utils.Log.LogManager;
import frc.demacia.utils.Sensors.AnalogSensorInterface;


public class UltraSonicSensor extends Ultrasonic implements AnalogSensorInterface {
    String name;
    UltraSonicSensorConfig config;
    public double get() {
        return getRangeMeters();
    }


    public String getName() {
        return config.name;
    }


    public UltraSonicSensor( UltraSonicSensorConfig config) {
        super(config.pingChannel, config.channel);
        this.config = config;
        name = config.name;
        addLog();
		LogManager.log(name + " cancoder initialized");
        

    }

    @Override
    public void ping() {
        super.ping();
    }

    public double getRangeMeters() {
        return getRangeMM() / 100.0;
    }
    private void addLog() {
        LogManager.addEntry(name + "range", this::get, 3);
            getRangeMeters();
    }
}
