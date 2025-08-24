package frc.demacia.utils.Sensors;

import frc.demacia.utils.Log.LogManager;

public class AnalogEncoder extends edu.wpi.first.wpilibj.AnalogEncoder implements AnalogSensorInterface{
    AnalogEncoderConfig config;
    String name;

    public AnalogEncoder(AnalogEncoderConfig config){
        super(config.channel, config.fullRange, config.offset);
        this.config = config;
        name = config.name;
        configEncoder();
        addLog();
        LogManager.log(name + " analog encoder initialized");
    }

    private void configEncoder() {
        setInverted(config.isInverted);
        setVoltagePercentageRange(config.minRange, config.maxRange);
    }

    private void addLog() {
        LogManager.addEntry(name + "/Position", this::get, 2);
    }

    public String getName(){
        return config.name;
    }

    public boolean isInverted(){
        return config.isInverted;
    }
    
    @Override
    public double get(){
        return super.get();
    }
}