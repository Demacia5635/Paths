package frc.demacia.utils.Motors;

/** 
 * Class to hold all Spark motor configuration
 * Applicable to REV Spark Max/Flex
 *  */
public class SparkFlexConfig extends BaseMotorConfig {

    /** 
     * Constructor
     * @param id - canbus ID
     * @param name - name of motor for logging
     */
    public SparkFlexConfig(int id, String name, Canbus canbus) {
        super(id, name, canbus);
    }

    public SparkFlexConfig(int id, String name) {
        super(id, name);
    }

    public SparkFlexConfig(int id, Canbus canbus) {
        super(id, canbus);
    }

    public SparkFlexConfig(int id, String name, Canbus canbus, BaseMotorConfig config) {
        super(id, name, canbus);
        copyBaseFields(config);
    }

    public SparkFlexConfig(int id, String name, BaseMotorConfig config) {
        super(id, name);
        copyBaseFields(config);
    }

    public SparkFlexConfig(int id, Canbus canbus, BaseMotorConfig config) {
        super(id, canbus);
        copyBaseFields(config);
    }

    @Override
    protected SparkFlexConfig self() {
        return this;
    }

    @Override
    public SparkFlexMotor create() {
        return new SparkFlexMotor(this);
    }
}