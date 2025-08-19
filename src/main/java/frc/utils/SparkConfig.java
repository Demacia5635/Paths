package frc.utils;

/** 
 * Class to hold all Spark Max configuration
 * Applicable to REV Robotics Spark Max
 */
public class SparkConfig {
    public int id;                  // CAN ID
    public String name;             // Name of the motor - used for logging

    public double nominalVoltage = 12;     // Nominal voltage for voltage compensation
    public boolean isVoltageCompensation = true; // Enable/disable voltage compensation
    public int stallCurrentLimit = 40;     // Stall current limit (A)
    public int freeCurrentLimit = 40;      // Free current limit (A) 
    public int currentLimitThresholdRpm = 5800; // RPM threshold for current limiting
    public double currentLimit = 60;       // Secondary current limit (A)
    public int currentChopCycles = 0;      // Current chop cycles for secondary limit
    public double rampUpTime = 0.3;        // Ramp rate for both open and closed loop

    public boolean brake = true;           // brake/coast
    public double motorRatio = 1;          // motor to mechanism ratio
    public boolean inverted = false;       // if to invert motor

    public closeLoopParam pid = new closeLoopParam(0, 0, 0, 0, 0, 0, 0); // close loop argument - PID + FF
    public closeLoopParam pid1 = null;     // pid for slot 1
    public closeLoopParam pid2 = null;     // pid for slot 2
    public closeLoopParam pid3 = null;     // pid for slot 3

    public double maxMotionAccel = 1;      // maximum MAXMotion acceleration
    public double maxMotionVelocity = 1;   // maximum MAXMotion velocity
    public double maxMotionJerk = 0;       // maximum MAXMotion jerk
    public double maxMotionPositionErrorTolerance = 0.05; // Position error tolerance
    public double maxMotionVelocityErrorTolerance = 0.1;  // Velocity error tolerance

    public double kv2 = 0;
    public double kSin = 0;
    public double posToRad = 0;

    /** 
    * Class to hold closed loop param
    */
    class closeLoopParam {
        double kp;  
        double ki;
        double kd;
        double kiz; // I Zone
        double kff; // Feed Forward
        double kmax; // Max output
        double kmin; // Min output

        closeLoopParam(double kp, double ki, double kd, double kiz, double kff, double kmax, double kmin) {
            this.kp = kp;
            this.ki = ki;
            this.kd = kd;
            this.kiz = kiz;
            this.kff = kff;
            this.kmax = kmax;
            this.kmin = kmin;
        }
    }

    /** 
     * Constructor
     * @param id - CAN ID
     * @param name - name of motor for logging
     */
    public SparkConfig(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /** 
     * @param nominalVoltage
     * @param isCompensationEnabled
     * @return SparkConfig
     */
    public SparkConfig withVoltageCompensation(double nominalVoltage, boolean isVoltageCompensation) {
        this.nominalVoltage = nominalVoltage;
        this.isVoltageCompensation = isVoltageCompensation;
        return this;
    }

    public SparkConfig withSmartCurrentLimit(int stallCurrentLimit, int freeCurrentLimit, int currentLimitThresholdRpm) {
        withStallCurrentLimit(stallCurrentLimit);
        withFreeCurrentLimit(freeCurrentLimit);
        withCurrentLimitThresholdRpm(currentLimitThresholdRpm);
        return this;
    }

    public SparkConfig withStallCurrentLimit(int stallCurrentLimit) {
        this.stallCurrentLimit = stallCurrentLimit;
        return this;
    }

    public SparkConfig withFreeCurrentLimit(int freeCurrentLimit) {
        this.freeCurrentLimit = freeCurrentLimit;
        return this;
    }

    public SparkConfig withCurrentLimitThresholdRpm(int currentLimitThresholdRpm) {
        this.currentLimitThresholdRpm = currentLimitThresholdRpm;
        return this;
    }

    public SparkConfig withSecondaryCurrentLimit(double currentLimit, int chopCycles) {
        withCurrentLimit(currentLimit);
        withCurrentChopCycles(currentChopCycles);
        return this;
    }

    public SparkConfig withCurrentLimit(double currentLimit) {
        this.currentLimit = currentLimit;
        return this;
    }

    public SparkConfig withCurrentChopCycles(int currentChopCycles) {
        this.currentChopCycles = currentChopCycles;
        return this;
    }

    public SparkConfig withRampUpTime(double rampUpTime) {
        this.rampUpTime = rampUpTime;
        return this;
    }

    public SparkConfig withBrake(boolean brake) {
        this.brake = brake;
        return this;
    }

    public SparkConfig withMotorRatio(double motorRatio) {
        this.motorRatio = motorRatio;
        return this;
    }

    public SparkConfig withInvert(boolean inverted) {
        this.inverted = inverted;
        return this;
    }

    public SparkConfig withMeterMotor(double circonference) {
        this.motorRatio *= 1 / circonference;
        return this;
    }

    public SparkConfig withRadiansMotor() {
        this.motorRatio *= 1 / (Math.PI * 2);
        return this;
    }

    /** 
     * @param kv2
     * @param ksin
     * @param posToRad
     * @return SparkConfig
     */
    public SparkConfig withFeedForward(double kv2, double ksin, double posToRad) {
        this.kv2 = kv2;
        this.kSin = ksin;
        this.posToRad = posToRad;
        return this;
    }

    /** 
     * @param kp
     * @param ki
     * @param kd
     * @param kiz
     * @param kff
     * @param kmax
     * @param kmin
     * @return SparkConfig
     */
    public SparkConfig withPID(double kp, double ki, double kd, double kiz, double kff, double kmax, double kmin) {
        pid = new closeLoopParam(kp, ki, kd, kiz, kff, kmax, kmin);
        return this;
    }

    /** 
     * @param kp
     * @param ki
     * @param kd
     * @param kiz
     * @param kff
     * @param kmax
     * @param kmin
     * @return SparkConfig
     */
    public SparkConfig withPID1(double kp, double ki, double kd, double kiz, double kff, double kmax, double kmin) {
        pid1 = new closeLoopParam(kp, ki, kd, kiz, kff, kmax, kmin);
        return this;
    }

    /** 
     * @param kp
     * @param ki
     * @param kd
     * @param kiz
     * @param kff
     * @param kmax
     * @param kmin
     * @return SparkConfig
     */
    public SparkConfig withPID2(double kp, double ki, double kd, double kiz, double kff, double kmax, double kmin) {
        pid2 = new closeLoopParam(kp, ki, kd, kiz, kff, kmax, kmin);
        return this;
    }

    /** 
     * @param kp
     * @param ki
     * @param kd
     * @param kiz
     * @param kff
     * @param kmax
     * @param kmin
     * @return SparkConfig
     */
    public SparkConfig withPID3(double kp, double ki, double kd, double kiz, double kff, double kmax, double kmin) {
        pid3 = new closeLoopParam(kp, ki, kd, kiz, kff, kmax, kmin);
        return this;
    }
    
    /** 
     * @param velocity
     * @param acceleration
     * @param jerk
     * @return SparkConfig
     */
    public SparkConfig withMAXMotion(double velocity, double acceleration, double jerk) {
        maxMotionVelocity = velocity;
        maxMotionAccel = acceleration;
        maxMotionJerk = jerk;
        return this;
    }

    /** 
     * @param positionTolerance
     * @param velocityTolerance
     * @return SparkConfig
     */
    public SparkConfig withMAXMotionTolerance(double positionTolerance, double velocityTolerance) {
        maxMotionPositionErrorTolerance = positionTolerance;
        maxMotionVelocityErrorTolerance = velocityTolerance;
        return this;
    }
}