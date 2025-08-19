package frc.utils.Sensors;

import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import frc.utils.LogManager;

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
        setInverted(config.isInverted);
        setAssumedFrequency(config.frequency);
    }

    private void addLog() {
        LogManager.addEntry(name + "/position", this::getPosition, 2);
    }

    public void checkElectronics() {
        if (!isConnected()) {
            LogManager.log(name + " encoder disconnected", AlertType.kWarning);
        }
    }
    
    public double getPosition(){
        return config.isRadians? get() * 2 * Math.PI 
        :get() * 360;
    }
}
