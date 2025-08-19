package frc.utils;

import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj.DutyCycleEncoder;

public class Encoder extends DutyCycleEncoder{
    EncoderConfig config;
    String name;

    public Encoder(EncoderConfig config){
        super(config.channel, config.scalar, config.offset);
        this.config = config;
		name = config.name;
        configEncoder();
        addLog();
		LogManager.log(name + " encoder initialized");
    }
    
    private void configEncoder() {
        setDutyCycleRange(config.minRange, config.maxRange);
        setInverted(config.inverted);
        setAssumedFrequency(config.frequency);
    }

    private void addLog() {
        LogManager.addEntry(name + "/Radians", this::getRadians, 2);
    }

    public void checkElectronics() {
        if (!isConnected()) {
            LogManager.log(name + " encoder disconnected", AlertType.kWarning);
        }
    }
    
    public double getRadians(){
        return get() * 2 * Math.PI;
    }
}
