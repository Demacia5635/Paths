package frc.robot.testMechanism.shooter.subsystems;

import edu.wpi.first.wpilibj.AnalogInput;
import frc.demacia.utils.mechanisms.StateBaseMechanism;
import frc.demacia.utils.motors.MotorInterface;
import frc.demacia.utils.motors.TalonFXMotor;
import frc.demacia.utils.sensors.LimitSwitch;
import frc.demacia.utils.sensors.SensorInterface;
import frc.robot.testMechanism.shooter.ShooterConstants.*;

public class Shooter extends StateBaseMechanism {
    
    private final AnalogInput noteSensor;
    private final double VOLTAGE_THRESHOLD = 4.7;

    public Shooter() {
        super("Shooter", 
            new MotorInterface[] {
                new TalonFXMotor(Config.ANGLE_CONFIG),
                new TalonFXMotor(Config.SHOOTER_UP_CONFIG),
                new TalonFXMotor(Config.SHOOTER_DOWN_CONFIG),
                new TalonFXMotor(Config.FEEDER_CONFIG)
            }, 
            new SensorInterface[] {
                new LimitSwitch(Config.LIMIT_CONFIG)
            }, 
            SHOOTER_STATE.class);

        noteSensor = new AnalogInput(Hardware.ANALOG_INPUT_ID);
        withLookUpTable(Config.SHOOTING_TABLE, () -> 0.0);
        
        setPositionMechanism(0);
        setStartingOption(SHOOTER_STATE.IDLE);
    }

    public boolean isNoteIn() {
        return noteSensor.getVoltage() < VOLTAGE_THRESHOLD;
    }

    public boolean isLimitPressed() {
        return ((LimitSwitch) getSensor(Hardware.LIMIT_SWITCH_NAME)).get();
    }

    public boolean isReadyToShoot() {
        double angleErr = Math.abs(getMotor(0).getCurrentPosition() - getValue(0));
        double upErr = Math.abs(getMotor(1).getCurrentVelocity() - getValue(1));
        double downErr = Math.abs(getMotor(2).getCurrentVelocity() - getValue(2));

        return angleErr < Config.ANGLE_THRESHOLD && 
               upErr < Config.VELOCITY_THRESHOLD && 
               downErr < Config.VELOCITY_THRESHOLD &&
               getValue(1) > 0;
    }

    @Override
    public void periodic() {
        super.periodic();
        if (isLimitPressed()) {
            getMotor(0).setEncoderPosition(Config.CalibrationConstants.RESET_POSITION); 
            setCalibration(true);
        }
    }
}