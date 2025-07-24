package frc.robot.utils;

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
		LogManager.log(name + " eancoder initialized");
    }
    
    private void configEncoder() {
        setDutyCycleRange(config.minRange, config.maxRange);
        setInverted(config.inverted);
        setAssumedFrequency(config.frequency);
    }

    @Override
    public boolean isConnected() {
        return super.isConnected();
    }

    private void addLog() {
        LogManager.addEntry(name + "/Rotitions", this::getRotitions, 2);
    }
    
    public double getRotitions(){
        return get();
    }
}
