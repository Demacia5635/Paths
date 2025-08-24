package frc.demacia.utils.Sensors;

import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import frc.demacia.utils.Log.LogManager;

public class DigitalEncoder extends DutyCycleEncoder implements SensorInterface{
    DigitalEncoderConfig config;
    String name;

    public DigitalEncoder(DigitalEncoderConfig config){
        super(config.channel, config.scalar, config.offset);
        this.config = config;
        name = config.name;
        configEncoder();
        addLog();
        LogManager.log(name + " digital encoder initialized");
    }
    
    private void configEncoder() {
        setDutyCycleRange(config.minRange, config.maxRange);
        setInverted(config.isInverted);
        setAssumedFrequency(config.frequency);
    }

    private void addLog() {
        LogManager.addEntry(name + "/Position", this::get, 2);
    }

    public String getName(){
        return config.name;
    }

    public void checkElectronics() {
        if (!isConnected()) {
            LogManager.log(name + " encoder disconnected", AlertType.kWarning);
        }
    }
    
    @Override
    public double get(){
        return super.get() * 2 * Math.PI;
    }

    public boolean isConnected() {
        return super.isConnected();
    }
}