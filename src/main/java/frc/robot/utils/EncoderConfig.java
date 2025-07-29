package frc.robot.utils;

public class EncoderConfig {
    public int channel;
    public String name;

    public double scalar = 1;
    public double minRange = 0;
    public double maxRange = 1;
    public double offset = 0;
    public double frequency = 1000;
    public double connectedFrequencyThreshold = 100;
    public boolean inverted = false;
    
    public EncoderConfig(int channel, String name) {
        this.channel = channel;
        this.name = name;
    }

    public EncoderConfig withScalar(double scalar){
        this.scalar = scalar;
        return this;
    }

    public EncoderConfig withRange(double minRange, double maxRange){
        this.minRange = minRange;
        this.maxRange = maxRange;
        return this;
    }

    public EncoderConfig withMinRange(double minRange){
        this.minRange = minRange;
        return this;
    }

    public EncoderConfig withMaxRange(double maxRange){
        this.maxRange = maxRange;
        return this;
    }

    public EncoderConfig withOffset(double offset){
        this.offset = offset;
        return this;
    }

    public EncoderConfig withFrequency(double frequency){
        this.frequency = frequency;
        return this;
    }

    public EncoderConfig withConnectedFrequencyThreshold(double connectedFrequencyThreshold){
        this.connectedFrequencyThreshold = connectedFrequencyThreshold;
        return this;
    }

    public EncoderConfig withInvert(boolean inverted){
        this.inverted = inverted;
        return this;
    }
}
