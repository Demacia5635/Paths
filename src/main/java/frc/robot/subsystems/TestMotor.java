// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.motorConstants;
import frc.utils.LogManager;
import frc.utils.TalonMotor;

public class TestMotor extends SubsystemBase {

  private final TalonMotor motor;

  /** Creates a new TestMotor. */
  public TestMotor() {
    motor = new TalonMotor(motorConstants.CONFIG);
    addNT();
  }

  public void addNT() {
    /* add to log the important stuff */
    LogManager.addEntry("vel", this::getVelocity, 3);
  }

  public void setPower(double power) {
    motor.setDuty(power);
  }

  public void setVelocityWithFeedForward(double velocity){
    motor.setVelocityWithFeedForward(velocity);
  }

  public double getVelocity(){
    return motor.getVelocity().getValueAsDouble();
  }

  public void stop() {
    motor.stopMotor();
  }

  @Override
  public void periodic() {
    
  }
}
