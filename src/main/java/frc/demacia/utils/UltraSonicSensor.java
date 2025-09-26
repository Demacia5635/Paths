package frc.demacia.utils;

import edu.wpi.first.wpilibj.Ultrasonic;
import frc.demacia.utils.Sensors.AnalogSensorInterface;
import frc.demacia.utils.Sensors.CancoderConfig;
import edu.wpi.first.wpilibj.Timer;

public class UltraSonicSensor extends Ultrasonic implements AnalogSensorInterface {
    String name;
    UltraSonicSensorConfig config;

    public double get() {
        return getRangeMeters();
    }


    public String getName() {
        return config.name;
    }


    public UltraSonicSensor(int pingChannelPort, int echoChannelPort, UltraSonicSensorConfig config) {
        super(pingChannelPort, echoChannelPort);
        this.config = config;
        name = config.name;

        

    }

    @Override
    public void ping() {
        super.ping();
    }

    public double getRangeMeters() {
        return getRangeMM() / 100.0;
    }
}
