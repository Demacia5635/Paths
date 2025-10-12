// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.testChassis;

import com.ctre.phoenix6.CANBus;

import edu.wpi.first.math.geometry.Translation2d;
import frc.demacia.utils.Motors.TalonConfig;
import frc.demacia.utils.Motors.BaseMotorConfig.Canbus;
import frc.demacia.utils.Sensors.CancoderConfig;
import frc.demacia.utils.Sensors.PigeonConfig;
import frc.demacia.utils.chassis.ChassisConfig;
import frc.demacia.utils.chassis.SwerveModuleConfig;

public final class Constants {
  public static final String NAME = "test chassis";
  public static final SwerveModuleConfig frontLeftModuleConfig = new SwerveModuleConfig(
    NAME + " frontLeft",
    new TalonConfig(0, Canbus.CANIvore, "frontLeft steer"),
    new TalonConfig(0, Canbus.CANIvore, "frontLeft drive"),
    new CancoderConfig(0, new CANBus("canivore"), "frontLeft cancoder"));
  public static final SwerveModuleConfig frontRightModuleConfig = new SwerveModuleConfig(
    NAME + " frontRight",
    new TalonConfig(0, Canbus.CANIvore, "frontRight steer"),
    new TalonConfig(0, Canbus.CANIvore, "frontRight drive"),
    new CancoderConfig(0, new CANBus("canivore"), "frontRight cancoder"));
  public static final SwerveModuleConfig backLeftModuleConfig = new SwerveModuleConfig(
    NAME + " backLeft",
    new TalonConfig(0, Canbus.CANIvore, "backLeft steer"),
    new TalonConfig(0, Canbus.CANIvore, "backLeft drive"),
    new CancoderConfig(0, new CANBus("canivore"), "backLeft cancoder"));
  public static final SwerveModuleConfig backRightModuleConfig = new SwerveModuleConfig(
    NAME + " backRight",
    new TalonConfig(0, Canbus.CANIvore, "backRight steer"),
    new TalonConfig(0, Canbus.CANIvore, "backRight drive"),
    new CancoderConfig(0, new CANBus("canivore"), "backRight cancoder"));
  public static final CANBus PIGEO_CANBUS = new CANBus("canivore");
  public static final PigeonConfig pigeonConfig = new PigeonConfig(0, PIGEO_CANBUS, NAME + " pigeon");
  public static final Translation2d frontLeftPosition = new Translation2d(null, null);
  public static final Translation2d frontRightPosition = new Translation2d(null, null);
  public static final Translation2d backLeftPosition = new Translation2d(null, null);
  public static final Translation2d backRightPosition = new Translation2d(null, null);

  public static final ChassisConfig CHASSIS_CONFIG = new ChassisConfig(
    NAME,
    frontLeftModuleConfig,
    frontRightModuleConfig,
    backLeftModuleConfig,
    backRightModuleConfig,
    pigeonConfig,
    frontLeftPosition,
    frontRightPosition,
    backLeftPosition,
    backRightPosition);
}
