package frc.demacia.utils.Motors;


/** 
 * Class to hold all Talon FX/SRX configuration
 * Applicable to Phoenix 6
 *  */
public class TalonSRXConfig extends BaseMotorConfig {

    /** 
     * Constructor
     * @param id - canbus ID
     * @param name - name of motor for logging
     */
    public TalonSRXConfig(int id, String name, Canbus canbus) {
        super(id, name, canbus);
    }

    public TalonSRXConfig(int id, String name) {
        super(id, name);
    }

    public TalonSRXConfig(int id, Canbus canbus) {
        super(id, canbus);
    }

    public TalonSRXConfig(int id, String name, Canbus canbus, BaseMotorConfig config) {
        super(id, name, canbus);
        copyBaseFields(config);
    }

    public TalonSRXConfig(int id, String name, BaseMotorConfig config) {
        super(id, name);
        copyBaseFields(config);
    }

    public TalonSRXConfig(int id, Canbus canbus, BaseMotorConfig config) {
        super(id, canbus);
        copyBaseFields(config);
    }

    @Override
    protected TalonSRXConfig self() {
        return this;
    }

    @Override
    public TalonSRXMotor create() {
        return new TalonSRXMotor(this);
    }
}