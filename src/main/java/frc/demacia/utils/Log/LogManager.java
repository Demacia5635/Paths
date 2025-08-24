// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.demacia.utils.Log;

import java.util.ArrayList;
import java.util.function.Supplier;

import com.ctre.phoenix6.StatusSignal;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.demacia.utils.constants.UtilsContants.ConsoleConstants;

public class LogManager extends SubsystemBase {

  public static LogManager logManager; // singleton reference

  DataLog log;
  NetworkTable table = NetworkTableInstance.getDefault().getTable("Log");

  private static ArrayList<ConsoleAlert> activeConsole;
  
  // Optimization: Configurable skip cycles for better performance
  private static int SkipedCycles1 = 0;
  private static int SKIP_CYCLES = 1; // Default: log every cycle, can be changed for optimization
  private static boolean issenabled = true;

  // array of log entries
  ArrayList<LogEntry<?>> logEntries = new ArrayList<>();

  // Log managerconstructor
  public LogManager() {
    logManager = this;

    DataLogManager.start();
    DataLogManager.logNetworkTables(false);
    log = DataLogManager.getLog();
    DriverStation.startDataLog(log);
    
    activeConsole = new ArrayList<>();
    log("log manager is ready");
  }

  /*
   * add a log entry with all data
   */
  private <T> LogEntry<T> add(String name, StatusSignal<T> phoenix6Status, StatusSignal<T>[] phoenix6StatusArray, Supplier<T> getter, int logLevel, String metaData, Class<T> classType) {
    LogEntry<T> entry = new LogEntry<T>(name, phoenix6Status, phoenix6StatusArray, getter,  logLevel, metaData, classType);
    logEntries.add(entry);
    return entry;
  }

  /*
   * get a log entry - if not found, create one
   */
  private LogEntry<?> get(String name) {
    LogEntry<?> e = find(name);
    return e != null 
    ?e 
    :new LogEntry(name, null, null, null, 1, "", (Class<Double>) Double.class);
  }

  public static void removeInComp() {
    for (int i = 0; i < LogManager.logManager.logEntries.size(); i++) {
      LogManager.logManager.logEntries.get(i).removeInComp();
      if (LogManager.logManager.logEntries.get(i).logLevel == 1) {
        LogManager.logManager.logEntries.remove(LogManager.logManager.logEntries.get(i));
        i--;
      }
    }
  }

  /*
   * find a log entry by name
   */
  private LogEntry<?> find(String name) {
    for (LogEntry<?> entry : logEntries) {
      if (entry.name.equals(name)) {
        return entry;
      }
    }
    return null;
  }
  
  public static <T> LogEntry<T> addEntry(String name, StatusSignal<T> phoenixStatus, int logLevel, String metaData) {
    Class<T> classType;
    try{
      phoenixStatus.getValueAsDouble();
      classType = (Class<T>) Double.class;
    } catch(Exception e){
      classType = (Class<T>) phoenixStatus.getValue().getClass();
    }
    return logManager.add(name, phoenixStatus, null, null, logLevel, metaData, classType);
  }

  public static <T> LogEntry<T> addEntry(String name, StatusSignal<T> phoenix6Status, int logLevel) {
    return addEntry(name, phoenix6Status, logLevel, "");
  }

  public static <T> LogEntry<T> addEntry(String name, StatusSignal<T> phoenix6Status, String metaData) {
    return addEntry(name, phoenix6Status, 4, metaData);
  }

  public static <T> LogEntry<T> addEntry(String name, StatusSignal<T> phoenix6Status) {
    return addEntry(name, phoenix6Status, 4, "");
  }

  public static <T> LogEntry<T> addEntry(String name, StatusSignal<T>[] phoenixStatusArray, int logLevel, String metaData) {
    Class<T> classType;
    try {
      phoenixStatusArray[0].getValueAsDouble();
      classType = (Class<T>) double[].class;
    } catch (Exception e) {
      classType = (Class<T>) boolean[].class;
    }
    return logManager.add(name, null, phoenixStatusArray, null, logLevel, metaData, classType);
  }

  public static <T> LogEntry<T> addEntry(String name, StatusSignal<T>[] phoenixStatusArray, int logLevel) {
    return addEntry(name, phoenixStatusArray, logLevel, "");
  }

  public static <T> LogEntry<T> addEntry(String name, StatusSignal<T>[] phoenixStatusArray, String metaData) {
    return addEntry(name, phoenixStatusArray, 4, metaData);
  }

  public static <T> LogEntry<T> addEntry(String name, StatusSignal<T>[] phoenixStatusArray) {
    return addEntry(name, phoenixStatusArray, 4, "");
  }

  /*
   * Static function - add log entry for double supplier with option to add to
   * network table
   */
  public static <T> LogEntry<T> addEntry(String name, Supplier<T> getter, int logLevel, String metaData) {
    T value = getter.get();
    Class<T> classType = (Class<T>) value.getClass();
    return logManager.add(name, null, null, getter, logLevel, metaData, classType);
  }

  public static <T> LogEntry<T> addEntry(String name, Supplier<T> getter, int logLevel) {
    return addEntry(name, getter, logLevel, "");
  }

  public static <T> LogEntry<T> addEntry(String name, Supplier<T> getter, String metaData) {
    return addEntry(name, getter, 4, metaData);
  }

  public static <T> LogEntry<T> addEntry(String name, Supplier<T> getter) {
    return addEntry(name, getter, 4, "");
  }

  /*
   * Static function - get an entry, create if not foune - will see network table
   * is crating new
   */
  public static LogEntry<?> getEntry(String name) {
    return logManager.get(name);
  }

  /*
   * Log text message - also will be sent System.out
   */
  public static ConsoleAlert log(Object message, AlertType alertType) {
    DataLogManager.log(String.valueOf(message));
    
    ConsoleAlert alert = new ConsoleAlert(String.valueOf(message.toString()), alertType);
    alert.set(true);
    if (activeConsole.size() > ConsoleConstants.CONSOLE_LIMIT) {
      activeConsole.get(0).close();
      activeConsole.remove(0);
    }
    activeConsole.add(alert);
    return alert;
  }

  public static ConsoleAlert log(Object meesage) {
    return log(meesage, AlertType.kInfo);
  }

  @Override
  public void periodic() {
    // Configurable cycle skipping for performance optimization
    SkipedCycles1++;
    if (SkipedCycles1 < SKIP_CYCLES || !issenabled) {
      return;
    }
    SkipedCycles1 = 0;

    for (LogEntry<?> e : logEntries) {
      e.log();
    }
  }

  /*
   * Set skip interval for periodic logging (1 = every cycle, 2 = every other cycle, etc.)
   */
  public static void setStaticSkipCycles(int cycles) {
    SKIP_CYCLES = Math.max(1, cycles);
  }

  /*
   * Get current skip interval
   */
  public static int getSStaticSkipCycles() {
    return SKIP_CYCLES;
  }
  
  /*
   * Enable/disable all logging for performance in competition
   */
  public static void setLoggingEnabled(boolean isenabled) {
    issenabled = isenabled;
  }

  public static boolean getLoggingEnabled() {
    return issenabled;
  }
  
  /*
   * Get number of active log entries
   */
  public static int getEntryCount() {
    return logManager != null ? logManager.logEntries.size() : 0;
  }
  
  
  /*
   * Clear all log entries (useful for testing)
   */
  public static void clearEntries() {
    if (logManager != null) {
      logManager.logEntries.clear();
    }
  }
}