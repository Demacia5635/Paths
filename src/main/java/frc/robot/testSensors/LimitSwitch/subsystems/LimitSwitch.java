// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.testSensors.LimitSwitch.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.testSensors.LimitSwitch.Constants;

public class LimitSwitch extends SubsystemBase {

  frc.demacia.utils.Sensors.LimitSwitch limitSwitch;

  /** Creates a new LimitSwitch. */
  public LimitSwitch() {
    limitSwitch = new frc.demacia.utils.Sensors.LimitSwitch(Constants.limitSwitchConfig);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }

  public boolean isPressed() {
    return limitSwitch.get();
  }
}
