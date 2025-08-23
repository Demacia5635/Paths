package frc.demacia.utils.Sensors;

public class EncoderConfig extends BaseSensorConfig<EncoderConfig>{
    public double scalar = 1;
    public double minRange = 0;
    public double maxRange = 1;
    public double offset = 0;
    public double frequency = 1000;
    public double connectedFrequencyThreshold = 100;

    public EncoderConfig(int channel, String name) {
        super(channel, name);
    }

    public EncoderConfig withScalar(double scalar) {
        this.scalar = scalar;
        return this;
    }

    public EncoderConfig withRange(double minRange, double maxRange) {
        this.minRange = minRange;
        this.maxRange = maxRange;
        return this;
    }

    public EncoderConfig withOffset(double offset) {
        this.offset = offset;
        return this;
    }

    public EncoderConfig withFrequency(double frequency) {
        this.frequency = frequency;
        return this;
    }
}
