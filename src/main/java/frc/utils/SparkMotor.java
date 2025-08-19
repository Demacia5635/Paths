package frc.utils;

import com.revrobotics.spark.ClosedLoopSlot;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;


public class SparkMotor extends SparkMax implements Sendable{
  SparkConfig config;
  String name;
  SparkMaxConfig sparkConfig;
  SparkClosedLoopController closedLoopController;

  String lastControlMode = "Unknown";
    double lastClosedLoopSP = 0;
    double lastClosedLoopError = 0;
    double lastPosition = 0;
    double lastVelocity = 0;
    double lastAcceleration = 0;
    double lastVoltage = 0;
    double lastCurrent = 0;

  public SparkMotor(SparkConfig config){
    super(config.id, MotorType.kBrushless);
    this.config = config;
    name = config.name;
    closedLoopController = getClosedLoopController();
    configMotor();
    addLog();
    LogManager.log(name + " motor initialized");
  }
  private void configMotor() {
    sparkConfig = new SparkMaxConfig();
    sparkConfig.inverted(config.inverted);
    sparkConfig.idleMode(config.brake ? IdleMode.kBrake : IdleMode.kCoast);
    sparkConfig.closedLoopRampRate(config.rampUpTime);
    sparkConfig.openLoopRampRate(config.rampUpTime);
    sparkConfig.smartCurrentLimit(config.stallCurrentLimit , config.freeCurrentLimit , config.currentLimitThresholdRpm);
    if (config.isVoltageCompensation) {
        sparkConfig.voltageCompensation(config.nominalVoltage);
    } else {
        sparkConfig.disableVoltageCompensation();
    }
    sparkConfig.secondaryCurrentLimit(config.currentLimit, config.currentChopCycles);
    // Encoder configuration
    sparkConfig.encoder.positionConversionFactor(config.motorRatio);
    sparkConfig.encoder.velocityConversionFactor(config.motorRatio);

    // PID configuration
    if (config.pid != null) {
      sparkConfig.closedLoop.pid(config.pid.kp, config.pid.ki, config.pid.kd, ClosedLoopSlot.kSlot0);
      sparkConfig.closedLoop.iZone(config.pid.kiz, ClosedLoopSlot.kSlot0);
      sparkConfig.closedLoop.outputRange(config.pid.kmin, config.pid.kmax, ClosedLoopSlot.kSlot0);
    }
    
    if (config.pid1 != null) {
      sparkConfig.closedLoop.pid(config.pid1.kp, config.pid1.ki, config.pid1.kd, ClosedLoopSlot.kSlot1);
      sparkConfig.closedLoop.iZone(config.pid1.kiz, ClosedLoopSlot.kSlot1);
      sparkConfig.closedLoop.outputRange(config.pid1.kmin, config.pid1.kmax, ClosedLoopSlot.kSlot1);
    }
    
    if (config.pid2 != null) {
      sparkConfig.closedLoop.pid(config.pid2.kp, config.pid2.ki, config.pid2.kd, ClosedLoopSlot.kSlot2);
      sparkConfig.closedLoop.iZone(config.pid2.kiz, ClosedLoopSlot.kSlot2);
      sparkConfig.closedLoop.outputRange(config.pid2.kmin, config.pid2.kmax, ClosedLoopSlot.kSlot2);
    }
    
    if (config.pid3 != null) {
      sparkConfig.closedLoop.pid(config.pid3.kp, config.pid3.ki, config.pid3.kd, ClosedLoopSlot.kSlot3);
      sparkConfig.closedLoop.iZone(config.pid3.kiz, ClosedLoopSlot.kSlot3);
      sparkConfig.closedLoop.outputRange(config.pid3.kmin, config.pid3.kmax, ClosedLoopSlot.kSlot3);
    }

    // MAXMotion configuration
    sparkConfig.closedLoop.maxMotion.maxVelocity(config.maxMotionVelocity);
    sparkConfig.closedLoop.maxMotion.maxAcceleration(config.maxMotionAccel);
    sparkConfig.closedLoop.maxMotion.allowedClosedLoopError(config.maxMotionPositionErrorTolerance);

    configure(sparkConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
}

  private void addLog() {
    LogManager.addEntry(name + "/Position and Velocity and Voltage and Current", 
            () -> new double[] {
                getCurrentPosition(),
                getCurrentVelocity(), 
                getCurrentVoltage(),
                getCurrentCurrent()
            }, 2, "motor");
  }

  public void checkElectronics() {
    if (hasActiveFault() && getFaults() != null) {
        LogManager.log(name + " has fault: " + getFaults(), AlertType.kError);
    }
  }








  /**
     * change the slot of the pid and feed forward.
     * will not work if the slot is null
     * @param slot the wanted slot between 0 and 3
     */
    public void changeSlot(int slot) {
        if (slot < 0 || slot > 3) {
            LogManager.log("slot is not between 0 and 3", AlertType.kError);
            return;
        }
        if (slot == 0 && config.pid == null) {
            LogManager.log("slot is null, add config for slot 0", AlertType.kError);
            return;
        }
        if (slot == 1 && config.pid1 == null) {
            LogManager.log("slot is null, add config for slot 1", AlertType.kError);
            return;
        }
        if (slot == 2 && config.pid2 == null) {
            LogManager.log("slot is null, add config for slot 2", AlertType.kError);
            return;
        }
        if (slot == 3 && config.pid3 == null) {
            LogManager.log("slot is null, add config for slot 3", AlertType.kError);
            return;
        }
        // Note: Slot changing in Spark is done when calling the control method
    }

    /**
     * set motor to brake or coast
     */
    public void setNeutralMode(boolean isBrake) {
      sparkConfig.idleMode(isBrake ? IdleMode.kBrake : IdleMode.kCoast);
      configure(sparkConfig, ResetMode.kNoResetSafeParameters, PersistMode.kPersistParameters);
    }

    private ClosedLoopSlot getSlotEnum(int slot) {
      return switch (slot) {
          case 0 -> ClosedLoopSlot.kSlot0;
          case 1 -> ClosedLoopSlot.kSlot1;
          case 2 -> ClosedLoopSlot.kSlot2;
          case 3 -> ClosedLoopSlot.kSlot3;
          default -> ClosedLoopSlot.kSlot0;
      };
  }

    /**
     * set power from 1 to -1 no PID/FF
     * @param power the wanted power between -1 to 1
     */
    public void setDuty(double power) {
      set(power);
  }

  public void setVoltage(double voltage) {
      setVoltage(voltage);
  }

  /**
   * set velocity to motor with PID and FF
   * @param velocity the wanted velocity in meter per second or radians per seconds depending on the config
   * @param feedForward wanted feed forward to add, defaults to 0
   * @param slot PID slot to use (0-3)
   */
  public void setVelocity(double velocity, double feedForward, int slot) {
      closedLoopController.setReference(velocity, ControlType.kVelocity, getSlotEnum(slot), feedForward);
  }

  public void setVelocity(double velocity, double feedForward) {
      setVelocity(velocity, feedForward, 0);
  }

  public void setVelocity(double velocity) {
      setVelocity(velocity, 0, 0);
  }

  /**
   * set MAXMotion position with PID and FF
   * must add to config MAXMotion configs (vel, acc, jerk[optional])
   * @param position the wanted position in meter or radians depending on the config
   * @param feedForward wanted feed forward to add, defaults to 0
   * @param slot PID slot to use (0-3)
   */
  public void setMAXMotionPosition(double position, double feedForward, int slot) {
      closedLoopController.setReference(position, ControlType.kMAXMotionPositionControl, getSlotEnum(slot), feedForward);
  }

  public void setMAXMotionPosition(double position, double feedForward) {
      setMAXMotionPosition(position, feedForward, 0);
  }

  public void setMAXMotionPosition(double position) {
      setMAXMotionPosition(position, 0, 0);
  }

  /**
   * set MAXMotion velocity with PID and FF
   * @param velocity the wanted velocity
   * @param feedForward wanted feed forward to add, defaults to 0
   * @param slot PID slot to use (0-3)
   */
  public void setMAXMotionVelocity(double velocity, double feedForward, int slot) {
      closedLoopController.setReference(velocity, ControlType.kMAXMotionVelocityControl, getSlotEnum(slot), feedForward);
  }

  public void setMAXMotionVelocity(double velocity, double feedForward) {
      setMAXMotionVelocity(velocity, feedForward, 0);
  }

  public void setMAXMotionVelocity(double velocity) {
      setMAXMotionVelocity(velocity, 0, 0);
  }

  public void setPosition(double position, double feedForward, int slot) {
      closedLoopController.setReference(position, ControlType.kPosition, getSlotEnum(slot), feedForward);
  }

  public void setPosition(double position, double feedForward) {
      setPosition(position, feedForward, 0);
  }

  public void setPosition(double position) {
      setPosition(position, 0, 0);
  }

    public void setVelocityWithFeedForward(double velocity) {
        setVelocity(velocity, velocityFeedForward(velocity));
    }

    public void setMAXMotionWithFeedForward(double position) {
        setMAXMotionPosition(position, positionFeedForward(position));
    }

    private double velocityFeedForward(double velocity) {
        return velocity * velocity * Math.signum(velocity) * config.kv2;
    }

    private double positionFeedForward(double position) {
        return Math.sin(position * config.posToRad) * config.kSin;
    }

    public String getCurrentControlMode() {
        // Note: Spark doesn't have a direct way to get current control mode
        // You might need to track this manually or use a different approach
        return lastControlMode;
    }

    public double getCurrentClosedLoopSP() {
        // This would need to be tracked manually or use Spark's logging
        return lastClosedLoopSP;
    }

    public double getCurrentClosedLoopError() {
        // This would need to be tracked manually or use Spark's logging
        return lastClosedLoopError;
    }

    public double getCurrentPosition() {
        lastPosition = getEncoder().getPosition();
        return lastPosition;
    }

    public double getCurrentVelocity() {
        lastVelocity = getEncoder().getVelocity();
        return lastVelocity;
    }

    public double getCurrentAcceleration() {
        // Spark doesn't provide direct acceleration measurement
        // You would need to calculate this or use external sensors
        return lastAcceleration;
    }

    public double getCurrentVoltage() {
        lastVoltage = getBusVoltage() * getAppliedOutput();
        return lastVoltage;
    }

    public double getCurrentCurrent() {
        lastCurrent = getOutputCurrent();
        return lastCurrent;
    }

    /**
     * creates a widget in elastic of the pid and ff for hot reload
     * @param slot the slot of the close loop params (from 0 to 3)
     */
    public void configPidFf(int slot) {
        Command configPidFf = new InstantCommand(() -> {
            SparkMaxConfig tempCfg = new SparkMaxConfig();
            
            ClosedLoopSlot sparkSlot = switch (slot) {
                case 0 -> ClosedLoopSlot.kSlot0;
                case 1 -> ClosedLoopSlot.kSlot1;
                case 2 -> ClosedLoopSlot.kSlot2;
                case 3 -> ClosedLoopSlot.kSlot3;
                default -> ClosedLoopSlot.kSlot0;
            };

            SparkConfig.closeLoopParam pidConfig = switch (slot) {
                case 0 -> config.pid;
                case 1 -> config.pid1;
                case 2 -> config.pid2;
                case 3 -> config.pid3;
                default -> config.pid;
            };

            if (pidConfig != null) {
                tempCfg.closedLoop.pid(pidConfig.kp, pidConfig.ki, pidConfig.kd, sparkSlot);
                tempCfg.closedLoop.iZone(pidConfig.kiz, sparkSlot);
                tempCfg.closedLoop.outputRange(pidConfig.kmin, pidConfig.kmax, sparkSlot);
                configure(tempCfg, ResetMode.kNoResetSafeParameters, PersistMode.kPersistParameters);
            }
        }).ignoringDisable(true);

        SmartDashboard.putData(name + "/PID+FF config", new Sendable() {
            @Override
            public void initSendable(SendableBuilder builder) {
                builder.setSmartDashboardType("PID+FF Config");

                SparkConfig.closeLoopParam pidConfig = switch (slot) {
                    case 0 -> config.pid;
                    case 1 -> config.pid1;
                    case 2 -> config.pid2;
                    case 3 -> config.pid3;
                    default -> config.pid;
                };

                if (pidConfig != null) {
                    builder.addDoubleProperty("KP", () -> pidConfig.kp, (double newValue) -> pidConfig.kp = newValue);
                    builder.addDoubleProperty("KI", () -> pidConfig.ki, (double newValue) -> pidConfig.ki = newValue);
                    builder.addDoubleProperty("KD", () -> pidConfig.kd, (double newValue) -> pidConfig.kd = newValue);
                    builder.addDoubleProperty("KIZ", () -> pidConfig.kiz, (double newValue) -> pidConfig.kiz = newValue);
                    builder.addDoubleProperty("KFF", () -> pidConfig.kff, (double newValue) -> pidConfig.kff = newValue);
                    builder.addDoubleProperty("KMAX", () -> pidConfig.kmax, (double newValue) -> pidConfig.kmax = newValue);
                    builder.addDoubleProperty("KMIN", () -> pidConfig.kmin, (double newValue) -> pidConfig.kmin = newValue);
                }

                builder.addBooleanProperty("Update", () -> configPidFf.isScheduled(),
                    value -> {
                        if (value) {
                            if (!configPidFf.isScheduled()) {
                                configPidFf.schedule();
                            }
                        } else {
                            if (configPidFf.isScheduled()) {
                                configPidFf.cancel();
                            }
                        }
                    }
                );
            }
        });
    }

    /**
     * creates a widget in elastic to configure MAXMotion in hot reload
     */
    public void configMAXMotion() {
      Command configMAXMotion = new InstantCommand(() -> {
        SparkMaxConfig tempSparkConfig = new SparkMaxConfig();
        
        tempSparkConfig.closedLoop.maxMotion.maxVelocity(config.maxMotionVelocity);
        tempSparkConfig.closedLoop.maxMotion.maxAcceleration(config.maxMotionAccel);
        tempSparkConfig.closedLoop.maxMotion.allowedClosedLoopError(config.maxMotionPositionErrorTolerance);
        
        configure(tempSparkConfig, ResetMode.kNoResetSafeParameters, PersistMode.kPersistParameters);
      }).ignoringDisable(true);

        SmartDashboard.putData(name + "/MAXMotion Config", new Sendable() {
            @Override
            public void initSendable(SendableBuilder builder) {
                builder.setSmartDashboardType("MAXMotion Config");

                builder.addDoubleProperty("Vel", () -> config.maxMotionVelocity, value -> config.maxMotionVelocity = value);
                builder.addDoubleProperty("Acc", () -> config.maxMotionAccel, value -> config.maxMotionAccel = value);
                builder.addDoubleProperty("Jerk", () -> config.maxMotionJerk, value -> config.maxMotionJerk = value);
                builder.addDoubleProperty("PosTolerance", () -> config.maxMotionPositionErrorTolerance, value -> config.maxMotionPositionErrorTolerance = value);
                builder.addDoubleProperty("VelTolerance", () -> config.maxMotionVelocityErrorTolerance, value -> config.maxMotionVelocityErrorTolerance = value);

                builder.addBooleanProperty("Update", () -> configMAXMotion.isScheduled(),
                    value -> {
                        if (value) {
                            if (!configMAXMotion.isScheduled()) {
                                configMAXMotion.schedule();
                            }
                        } else {
                            if (configMAXMotion.isScheduled()) {
                                configMAXMotion.cancel();
                            }
                        }
                    }
                );
            }
        });
    }

    /**
     * override the sendable of the SparkMax to our custom widget in elastic
     * to activate put in the code: SmartDashboard.putData("sparkMotor name", sparkMotor);
     */
    @Override
    public void initSendable(SendableBuilder builder) {
        builder.setSmartDashboardType("SparkMotor");
        builder.addBooleanProperty("IsInverted", () -> config.inverted, null);
        builder.addDoubleProperty("CloseLoopSP", this::getCurrentClosedLoopSP, null);
        builder.addDoubleProperty("CloseLoopError", this::getCurrentClosedLoopError, null);
        builder.addDoubleProperty("Position", this::getCurrentPosition, null);
        builder.addDoubleProperty("Velocity", this::getCurrentVelocity, null);
        builder.addDoubleProperty("Acceleration", this::getCurrentAcceleration, null);
        builder.addDoubleProperty("Voltage", this::getCurrentVoltage, null);
        builder.addDoubleProperty("Current", this::getCurrentCurrent, null);
    }
}
