// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.utils;

import static frc.robot.utils.constants.UtilsContants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;

import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.BooleanTopic;
import edu.wpi.first.networktables.BooleanArrayPublisher;
import edu.wpi.first.networktables.BooleanArrayTopic;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.DoubleTopic;
import edu.wpi.first.networktables.DoubleArrayPublisher;
import edu.wpi.first.networktables.DoubleArrayTopic;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.util.datalog.BooleanLogEntry;
import edu.wpi.first.util.datalog.BooleanArrayLogEntry;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.DoubleLogEntry;
import edu.wpi.first.util.datalog.DoubleArrayLogEntry;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotContainer;
import frc.robot.utils.constants.UtilsContants.ConsoleConstants;

public class LogManager extends SubsystemBase {

  public static LogManager logManager; // singleton reference

  private DataLog log;
  private NetworkTableInstance ntInst = NetworkTableInstance.getDefault();
  private NetworkTable table = ntInst.getTable("Log");

  private static ArrayList<ConsoleAlert> activeConsole;
  
  // Optimization: Configurable skip cycles for better performance
  private int skipCycles1 = 0;
  private static int STATIC_SKIP_INTERVAL = 1; // Default: log every cycle, can be changed for optimization
  private static boolean issenabled = true;

  /*
   * class for a single data entry
   */
  public class LogEntry {
    DoubleLogEntry doubleEntry; // wpilib log entry
    DoubleArrayLogEntry doubleArrayEntry;
    BooleanLogEntry booleanEntry;
    BooleanArrayLogEntry booleanArrayEntry;
    @SuppressWarnings("rawtypes")
    StatusSignal phoenix6Status; // supplier of phoenix 6 status signal
    StatusSignal[] phoenix6Statuses;
    DoubleSupplier getterDouble; // supplier for double data - if no status signal provider
    DoubleSupplier[] getterDoubleArray;
    BooleanSupplier getterBoolean;
    BooleanSupplier[] getterBooleanArray;
    BiConsumer<Double, Long> doubleConsumer = null; // optional consumer when data changed - data value and time
    BiConsumer<double[], Long> doubleArrayConsumer = null;
    BiConsumer<Boolean, Long> booleanConsumer = null;
    BiConsumer<boolean[], Long> booleanArrayConsumer = null;
    String name;
    DoublePublisher ntPublisherDouble; // network table punlisher
    DoubleArrayPublisher ntPublisherDoubleArray;
    BooleanPublisher ntPublisherBoolean;
    BooleanArrayPublisher ntPublisherBooleanArray;
    double lastValue = Double.MAX_VALUE; // last value - only logging when value changes
    double[] lastArrayValue = new double[0];
    private double precision = 0; // Configurable precision for change detection
    private int skipCycles2 = 0;
    private int SkipCycle = 1; // Default: log every cycle, can be changed for optimization
    
    // Optimization: Cache type checks
    private final boolean isBooleanType;
    private final boolean isDoubleType;
    private final boolean isDoubleArrayType;
    private final boolean isBooleanArrayType;

    public boolean isBooleanType(BooleanSupplier getterBoolean, StatusSignal phoenix6Status){
        return getterBoolean != null || (phoenix6Status != null && phoenix6Status.getTypeClass() == Boolean.class);
    }

    public boolean isDoubleType(DoubleSupplier getterDouble, StatusSignal phoenix6Status){
        return getterDouble != null || (phoenix6Status != null && phoenix6Status.getTypeClass() == Double.class);
    }

    public boolean isDoubleArrayType(DoubleSupplier[] getterDoubleArray, StatusSignal[] phoenix6Statuses){
        return getterDoubleArray != null || (phoenix6Statuses != null && phoenix6Statuses.length > 0 && phoenix6Statuses[0].getTypeClass().equals(Double.class));
    }

    /*
     * the log levels are this:
     * 1 -> log if it is not in a compition
     * 2 -> only log
     * 3 -> log and add to network tables if not in a compition
     * 4 -> log and add to network tables
     */
    public int logLevel;

    /*
     * Constructor with the suppliers and boolean if add to network table
     */
    @SuppressWarnings("rawtypes")
    LogEntry(String name, StatusSignal phoenix6Status, StatusSignal[] phoenix6Statuses, DoubleSupplier getterDouble, BooleanSupplier getterBoolean, DoubleSupplier[] getterDoubleArray, BooleanSupplier[] getterBooleanArray,
        int logLevel) {

      this.name = name;
      this.logLevel = logLevel;
      
      // Cache type checks for better performance
      this.isBooleanType = isBooleanType(getterBoolean, phoenix6Status);
      this.isDoubleType = isDoubleType(getterDouble, phoenix6Status);
      this.isDoubleArrayType = isDoubleArrayType(getterDoubleArray, phoenix6Statuses);
      this.isBooleanArrayType = !isBooleanType && !isDoubleType && !isDoubleArrayType;
      
      if (isBooleanType) {
        this.booleanEntry = new BooleanLogEntry(log, name);
      } else if (isDoubleType) {
        this.doubleEntry = new DoubleLogEntry(log, name);
      } else if (isDoubleArrayType) {
        this.doubleArrayEntry = new DoubleArrayLogEntry(log, name);
      } else{
        this.booleanArrayEntry = new BooleanArrayLogEntry(log, name);
      }
      this.phoenix6Status = phoenix6Status;
      this.phoenix6Statuses = phoenix6Statuses;
      this.getterDouble = getterDouble;
      this.getterDoubleArray = getterDoubleArray;
      this.getterBoolean = getterBoolean;
      this.getterBooleanArray = getterBooleanArray;
      if (logLevel == 4 || (logLevel == 3 && !RobotContainer.isComp())) {
        if (isBooleanType) {
          BooleanTopic bt = table.getBooleanTopic(name);
          ntPublisherBoolean = bt.publish();
        }else if (isDoubleType) {
          DoubleTopic bt = table.getDoubleTopic(name);
          ntPublisherDouble = bt.publish();
        } else if (isDoubleArrayType) {
          DoubleArrayTopic bt = table.getDoubleArrayTopic(name);
          ntPublisherDoubleArray = bt.publish();
        } else {
          BooleanArrayTopic bt = table.getBooleanArrayTopic(name);
          ntPublisherBooleanArray = bt.publish();
        }
      } else {
        ntPublisherDouble = null;
        ntPublisherDoubleArray = null;
        ntPublisherBoolean = null;
        ntPublisherBooleanArray = null;
      }
    }

    /*
     * perform a periodic log
     * get the data from the getters and call the actual log
     */
    void log() {
      skipCycles2++;
      if (skipCycles2 < SkipCycle) {
        return;
      }
      skipCycles2 = 0;
      double v = 0;
      long time = 0;

      if (phoenix6Status != null) {
        var st = phoenix6Status.refresh();
        if (st.getStatus() == StatusCode.OK) {
            v = st.getValueAsDouble();
            time = (long) (st.getTimestamp().getTime() * 1000);
            log(v, time);
            return;
        } else {
            v = 1000000 + st.getStatus().value;
            log(v, time);
            return;
        }
      } else if(phoenix6Statuses != null) {

        StatusCode  st = StatusSignal.refreshAll(phoenix6Statuses);
          if (st == StatusCode.OK) {
            double[] arrV = new double[phoenix6Statuses.length];
              for (int i = 0; i < phoenix6Statuses.length; i++) {
                arrV[i] = phoenix6Statuses[i].getValueAsDouble();
    
                if (i == 0) {
                    time = (long) (phoenix6Statuses[i].getTimestamp().getTime() * 1000);
                }
              }
              log(arrV, time);
              return;
          } else {
              v = 1000000 + st.value;
              log(v, time);
              return;
          }
      } else if (getterBoolean != null) {
          v = getterBoolean.getAsBoolean() ? 1 : 0;
          time = 0;
          log(v, time);
          return;
      } else if (getterDouble != null) {
          v = getterDouble.getAsDouble();
          time = 0;
          log(v, time);
          return;
      } else if (getterDoubleArray != null) {
        double[] arrV = new double[getterDoubleArray.length];
        for (int i = 0; i < getterDoubleArray.length; i++) {
          arrV[i] = getterDoubleArray[i].getAsDouble();
        }
        time = 0;
        log(arrV, time);
        return;
      } else if (getterBooleanArray != null) {
        double[] arrV = new double[getterBooleanArray.length];
        for (int i = 0; i < getterBooleanArray.length; i++) {
          arrV[i] = getterBooleanArray[i].getAsBoolean() ? 1 : 0;
        }
          time = 0;
          log(arrV, time);
          return;
      }
  }

    /*
     * log a value use zero (current) time
     */
    public void log(double v) {
      log(v, 0);
    }

    /*
     * Log data and time if data changed
     * also publish to network table (if required)
     * also call consumer if set
     */
    public void log(double v, long time) {
      if (Math.abs(v - lastValue) < precision) {
        return ;
      }
      if (isBooleanType) {
        booleanEntry.append(v == 1, time);
        if (ntPublisherBoolean != null) {
          ntPublisherBoolean.set(v == 1);
        }
        if (doubleConsumer != null) {
          doubleConsumer.accept(v, time);
        }
        if (booleanConsumer != null) {
          booleanConsumer.accept(v == 1, time);
        }
      } else  {
        doubleEntry.append(v, time);
        if (ntPublisherDouble != null) {
          ntPublisherDouble.set(v);
        }
        if (doubleConsumer != null) {
          doubleConsumer.accept(v, time);
        }
      }
      lastValue = v;
    }

    public void log(double[] arrV) {
      log(arrV, 0);
    }

    private boolean hasSignificantChange(double[] arrV) {
      if (arrV.length != lastArrayValue.length) {
        return true;
      }
      
      for (int i = 0; i < arrV.length; i++) {
        if (Math.abs(arrV[i] - lastArrayValue[i]) >= precision) {
          return true;
        }
      }
      return false;
    }

    public void log(double[] arrV, long time) {
      if (!hasSignificantChange(arrV)){
        return ;
      }
      if (isBooleanArrayType) {
        boolean[] boolArrV = new boolean[arrV.length];
        for (int i = 0; i < arrV.length; i++){
          boolArrV[i] = (arrV[i] == 1);
        }
        booleanArrayEntry.append(boolArrV, time);
        if (ntPublisherBooleanArray != null) {
          ntPublisherBooleanArray.set(boolArrV);
        }
        if (doubleArrayConsumer != null) {
          doubleArrayConsumer.accept(arrV, time);
        }
        if (booleanArrayConsumer != null) {
          booleanArrayConsumer.accept(boolArrV, time);
        }
      } else  {
        doubleArrayEntry.append(arrV, time);
        if (ntPublisherDoubleArray != null) {
            ntPublisherDoubleArray.set(arrV);
        }
        if (doubleArrayConsumer != null) {
            doubleArrayConsumer.accept(arrV, time);
        }
      }
      lastArrayValue = Arrays.copyOf(arrV, arrV.length);
    }

    public void setPrecision(double precision) {
      this.precision = precision;
    }

    public double getPrecision() {
      return precision;
    }

    /*
     * Set skip interval for periodic logging (1 = every cycle, 2 = every other cycle, etc.)
     */
    public void setSkipCycles(int interval) {
      SkipCycle = Math.max(1, interval);
    }
  
    /*
     * Get current skip interval
     */
    public int getSkipCycles() {
      return SkipCycle;
    }

    // set the consumer
    public void setDoubleConsumer(BiConsumer<Double, Long> consumer) {
      this.doubleConsumer = consumer;
    }

    public void setDoubleArrayConsumer(BiConsumer<double[], Long> consumer) {
      this.doubleArrayConsumer = consumer;
    }

    public void setBooleanConsumer(BiConsumer<Boolean, Long> consumer) {
      this.booleanConsumer = consumer;
    }

    public void setBooleanArrayConsumer(BiConsumer<boolean[], Long> consumer) {
      this.booleanArrayConsumer = consumer;
    }

    public void removeInComp() {
      if (logLevel == 3) {
        if (isBooleanType) {
          ntPublisherBoolean.close();
        } else if (isDoubleType) {
          ntPublisherDouble.close();
        } else if (isDoubleArrayType) {
          ntPublisherDoubleArray.close();
        } else {
          ntPublisherBooleanArray.close();
        }
      }
    }
  }

  // array of log entries
  ArrayList<LogEntry> logEntries = new ArrayList<>();
  
  // HashMap for faster name-based lookups
  private Map<String, LogEntry> entryMap = new HashMap<>();

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
  @SuppressWarnings("rawtypes")
  private LogEntry add(String name, StatusSignal phoenix6Status, StatusSignal[] phoenix6Statuses, DoubleSupplier getterDouble,
      BooleanSupplier getterBoolean, DoubleSupplier[] getterDoubleArray, BooleanSupplier[] getterBooleanArray, int logLevel) {
    // Check if entry already exists
    LogEntry existingEntry = entryMap.get(name);
    if (existingEntry != null) {
      return existingEntry;
    }
    
    LogEntry entry = new LogEntry(name, phoenix6Status, phoenix6Statuses, getterDouble, getterBoolean, getterDoubleArray, getterBooleanArray, logLevel);
    logEntries.add(entry);
    entryMap.put(name, entry);
    return entry;
  }

  /*
   * get a log entry - if not found, create one
   */
  private LogEntry get(String name) {
    LogEntry e = entryMap.get(name);
    if (e != null) {
      return e;
    }
    LogEntry newEntry = new LogEntry(name, null, null, null, null, null, null, 1);
    entryMap.put(name, newEntry);
    return newEntry;
  }

  public static void removeInComp() {
    ArrayList<LogEntry> entries = logManager.logEntries;
    for (int i = entries.size()-1; i >= 0; i--) {
      LogEntry entry = entries.get(i);
      entry.removeInComp();
      if (entry.logLevel == 1) {
        entries.remove(entry);
        logManager.entryMap.remove(entry.name);
      }
    }
  }

  /*
   * find a log entry by name - now using HashMap for O(1) lookup
   */
  private LogEntry find(String name) {
    return entryMap.get(name);
  }

  /*
   * Static function - add log entry with all data
   */
  @SuppressWarnings("rawtypes")
  public static LogEntry addEntry(String name, StatusSignal phoenixStatus, DoubleSupplier getterDouble, int logLevel) {
    return logManager.add(name, phoenixStatus, null, getterDouble, null, null, null, logLevel);
  }

  @SuppressWarnings("rawtypes")
  public static LogEntry addEntry(String name, StatusSignal[] phoenixStatuses, DoubleSupplier getterDouble, int logLevel) {
    return logManager.add(name, null, phoenixStatuses, getterDouble, null, null, null, logLevel);
  }

  /*
   * Static function - add log entry for status signal with option to add to
   * network table
   */
  @SuppressWarnings("rawtypes")
  public static LogEntry addEntry(String name, StatusSignal phoenixStatus, int logLevel) {
    return logManager.add(name, phoenixStatus, null, null, null, null, null, logLevel);
  }

  @SuppressWarnings("rawtypes")
  public static LogEntry addEntry(String name, StatusSignal[] phoenixStatuses, int logLevel) {
    return logManager.add(name, null, phoenixStatuses, null, null, null, null, logLevel);
  }

  /*
   * Static function - add log entry for status signal with network table
   */
  @SuppressWarnings("rawtypes")
  public static LogEntry addEntry(String name, StatusSignal phoenix6Status) {
    return logManager.add(name, phoenix6Status, null, null, null, null, null, 4);
  }

  @SuppressWarnings("rawtypes")
  public static LogEntry addEntry(String name, StatusSignal[] phoenix6Statuses) {
    return logManager.add(name, null, phoenix6Statuses, null, null, null, null, 4);
  }

  /*
   * Static function - add log entry for double supplier with option to add to
   * network table
   */
  public static LogEntry addEntry(String name, DoubleSupplier getterDouble, int logLevel) {
    return logManager.add(name, null, null, getterDouble, null, null, null, logLevel);
  }

  public static LogEntry addEntry(String name, DoubleSupplier[] getterDoubleArray, int logLevel) {
    return logManager.add(name, null, null, null, null, getterDoubleArray, null, logLevel);
  }

  /*
   * Static function - add log entry for double supplier with network table
   */
  public static LogEntry addEntry(String name, DoubleSupplier getterDouble) {
    return logManager.add(name, null, null, getterDouble, null, null, null,  4);
  }

  public static LogEntry addEntry(String name, DoubleSupplier[] getterDoubleArray) {
    return logManager.add(name, null, null, null, null, getterDoubleArray, null,  4);
  }

  public static LogEntry addEntry(String name, BooleanSupplier getterBoolean) {
    return logManager.add(name, null, null, null, getterBoolean, null, null, 4);
  }

  public static LogEntry addEntry(String name, BooleanSupplier[] getterBooleanArray) {
    return logManager.add(name, null, null, null, null, null, getterBooleanArray, 4);
  }

  public static LogEntry addEntry(String name, BooleanSupplier getterBoolean, int logLevel) {
    return logManager.add(name, null, null, null, getterBoolean, null, null, logLevel);
  }

  public static LogEntry addEntry(String name, BooleanSupplier[] getterBooleanArray, int logLevel) {
    return logManager.add(name, null, null, null, null, null, getterBooleanArray, logLevel);
  }
  

  /*
   * Static function - get an entry, create if not foune - will see network table
   * is crating new
   */
  public static LogEntry getEntry(String name) {
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
    skipCycles1++;
    if (skipCycles1 < STATIC_SKIP_INTERVAL || !issenabled) {
      return;
    }
    skipCycles1 = 0;

    for (LogEntry e : logEntries) {
      e.log();
    }
  }

  /*
   * Set skip interval for periodic logging (1 = every cycle, 2 = every other cycle, etc.)
   */
  public static void setStaticSkipInterval(int interval) {
    STATIC_SKIP_INTERVAL = Math.max(1, interval);
  }

  /*
   * Get current skip interval
   */
  public static int getStaticSkipInterval() {
    return STATIC_SKIP_INTERVAL;
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
      logManager.entryMap.clear();
    }
  }
}