package frc.utils.Sensors;

import com.ctre.phoenix6.CANBus;

public abstract class BaseSensorConfig <T extends BaseSensorConfig<T>>{
    public int id;
    public String name;
    public CANBus canbus;
    public int channel;

    public boolean isInverted = false;

    public boolean isDegrees = false;
    public boolean isRadians = false;

    /**
     * Constructor
     * @param id - CAN bus ID
     * @param name - name of motor for logging
     */
    public BaseSensorConfig(int channel, String name) {
        this.channel = channel;
        this.name = name;
    }

    public BaseSensorConfig(int id, CANBus canbus, String name) {
        this.id = id;
        this.name = name;
        this.canbus = canbus;
    }

    /**
     * Set motor inversion
     * @param invert true to invert motor direction
     * @return this config for chaining
     */
    @SuppressWarnings("unchecked")
    public T withInvert(boolean isInverted) {
        this.isInverted = isInverted;
        return (T) this;
    }

    /**
     * Configure motor for rotational motion (radians)
     * @param gearRatio gear ratio from motor to mechanism
     * @return this config for chaining
     */
    @SuppressWarnings("unchecked")
    public T withRadiansMotor() {
        isRadians = true;
        isDegrees = false;
        return (T) this;
    }

    /**
     * Configure motor for rotational motion (degrees)
     * @param gearRatio gear ratio from motor to mechanism
     * @return this config for chaining
     */
    @SuppressWarnings("unchecked")
    public T withDegreesMotor() {
        isRadians = false;
        isDegrees = true;
        return (T) this;
    }
}
