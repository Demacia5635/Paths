package frc.robot.utils;

public class EncoderConfig {
    public int channel;
    public String name;

    public double scalar;
    public double minRange;
    public double maxRange;
    public double offset;
    public double frequency;
    public double connectedFrequencyThreshold;
    public boolean inverted = false;
    
    public EncoderConfig(int channel, String name) {
        this.channel = channel;
        this.name = name;
    }

    public EncoderConfig withScalar(double scalar){
        this.scalar = scalar;
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
