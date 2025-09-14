// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.testSensors.piegon.subsystems;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import frc.robot.testSensors.piegon.Constants;



public class Pigeon extends SubsystemBase {
  frc.demacia.utils.Sensors.Pigeon pigeon;
  /** Creates a new Pigeon. */
  public Pigeon() {
    pigeon= new frc.demacia.utils.Sensors.Pigeon(Constants.PIGEON_CONFIG);
    
  }


  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
  public double getCurrentYaw(){
    return pigeon.getCurrentYaw();
  }
  public double getYawInZeroTo2Pi(){
    return pigeon.getCurrentYaw();
  }
  public double getCurrentPitch(){
    return pigeon.getCurrentPitch();
  }
  public double getPitchInZeroTo2Pi(){
    return pigeon.getPitchInZeroTo2Pi();
  }
  public double getCurrentRoll(){
    return pigeon.getCurrentRoll();
  }
  public double getRollInZeroTo2Pi(){
    return pigeon.getRollInZeroTo2Pi();
  }
  public double getXVelocity(){
    return pigeon.getXVelocity();
  }
  public double getYVelocity(){
    return pigeon.getYVelocity();
  }
  public double getZVelocity(){
    return pigeon.getZVelocity();
    
  }
  @Override
  public void initSendable(SendableBuilder builder) {
      super.initSendable(builder);
      builder.addDoubleProperty("getZVelocity:",this::getCurrentPitch,null);
  }
}   




