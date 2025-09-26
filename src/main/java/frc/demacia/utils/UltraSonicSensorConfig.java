package frc.demacia.utils;
import frc.demacia.utils.Sensors.AnalogSensorConfig;

public class UltraSonicSensorConfig extends AnalogSensorConfig<UltraSonicSensorConfig> {
    int pingChannel;
    public UltraSonicSensorConfig(int channel, int pingChannel, String name) {
        super(channel, name);
        this.pingChannel = pingChannel;
        }
    }