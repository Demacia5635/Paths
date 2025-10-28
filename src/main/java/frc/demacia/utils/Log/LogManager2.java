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
import frc.demacia.utils.Data;

public class LogManager2 extends SubsystemBase {

  public static LogManager2 logManager;

  DataLog log;
  NetworkTable table = NetworkTableInstance.getDefault().getTable("Log");

  private static ArrayList<ConsoleAlert> activeConsole;
  
  private static int SkipedCycles = 0;
  private static int SKIP_CYCLES = 1;
  private static boolean isLoggingEnabled = true;

  ArrayList<LogEntry2<?>> logEntries = new ArrayList<>();
  
  String name = "";
  Data<?> data = null;
  LogEntry2<?> logEntry = null;
  String name2 = "";
  Data<?> data2 = null;
  LogEntry2<?> logEntry2 = null;
  String name3 = "";
  Data<?> data3 = null;
  LogEntry2<?> logEntry3 = null;
  String name4 = "";
  Data<?> data4 = null;
  LogEntry2<?> logEntry4 = null;

  public LogManager2() {
    logManager = this;

    DataLogManager.start();
    DataLogManager.logNetworkTables(false);
    log = DataLogManager.getLog();
    DriverStation.startDataLog(log);
    
    activeConsole = new ArrayList<>();
    log("log manager is ready");
  }

  public static <T> LogEntryBuilder<T> addEntry(String name, StatusSignal<T> ... statusSignals) {
      return new LogEntryBuilder<T>(name, statusSignals);
  }

  public static <T> LogEntryBuilder<T> addEntry(String name, Supplier<T> ... suppliers) {
    return new LogEntryBuilder<T>(name, suppliers);
  }

  public static void removeInComp() {
    for (int i = 0; i < logManager.logEntries.size(); i++) {
      logManager.logEntries.get(i).removeInComp();
      if (logManager.logEntries.get(i).logLevel == 1) {
        logManager.logEntries.remove(logManager.logEntries.get(i));
        i--;
      }
    }
  }
  
  public static void clearEntries() {
    if (logManager != null) {
      logManager.logEntries.clear();
    }
  }
  
  public static int getEntryCount() {
    return logManager != null ? logManager.logEntries.size() : 0;
  }

  public static ConsoleAlert log(Object message, AlertType alertType) {
    DataLogManager.log(String.valueOf(message));
    
    ConsoleAlert alert = new ConsoleAlert(String.valueOf(message), alertType);
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

  public static void setStaticSkipCycles(int cycles) {
    SKIP_CYCLES = Math.max(1, cycles);
  }

  public static int getStaticSkipCycles() {
    return SKIP_CYCLES;
  }
  
  public static void setLoggingEnabled(boolean isenabled) {
    isLoggingEnabled = isenabled;
  }

  public static boolean getLoggingEnabled() {
    return isLoggingEnabled;
  }

  @Override
  public void periodic() {
    SkipedCycles++;
    if (SkipedCycles < SKIP_CYCLES || !isLoggingEnabled) {
      return;
    }
    SkipedCycles = 0;

    for (LogEntry2<?> e : logEntries) {
      e.log();
    }
    if (logEntry != null){
      logEntry.log();
    }
    if (logEntry2 != null){
      logEntry2.log();
    }
    if (logEntry3 != null){
      logEntry3.log();
    }
    if (logEntry4 != null){
      logEntry4.log();
    }
  }

  public <T> LogEntry2<T> add(String name, Data<T> data, int logLevel, String metaData) {
    LogEntry2<T> entry = null;
    
    if(data.isDouble() && data.getSignals() != null) {
        if(this.data == null) {
            this.name = name;
            this.data = data;
        } else {
            try {
                StatusSignal<?>[] existingSignals = this.data.getSignals();
                StatusSignal<?>[] newSignal = data.getSignals();
                
                @SuppressWarnings("unchecked")
                StatusSignal<Double>[] combined = new StatusSignal[existingSignals.length + newSignal.length];
                System.arraycopy(existingSignals, 0, combined, 0, existingSignals.length);
                for (int i = 0; i < newSignal.length; i++){
                  combined[existingSignals.length+i] = (StatusSignal<Double>) newSignal[i];
                }
                this.name = this.name + " | " + name;
                this.data = new Data<>(combined);
                
            } catch (Exception e) {
              
            }
        }
        logEntry = new LogEntry2<>(this.name, this.data, 3, "");
    } else if(data.isBoolean() && data.getSignals() != null) {
      if(this.data2 == null) {
          this.name2 = name;
          this.data2 = data;
      } else {
          try {
              StatusSignal<?>[] existingSignals = this.data2.getSignals();
              StatusSignal<?>[] newSignal = data.getSignals();
              
              @SuppressWarnings("unchecked")
              StatusSignal<Double>[] combined = new StatusSignal[existingSignals.length + newSignal.length];
              System.arraycopy(existingSignals, 0, combined, 0, existingSignals.length);
              for (int i = 0; i < newSignal.length; i++){
                combined[existingSignals.length+i] = (StatusSignal<Double>) newSignal[i];
              }
              this.name2 = this.name2 + " | " + name;
              this.data2 = new Data<>(combined);
          } catch (Exception e) {
            
          }
      }
      logEntry2 = new LogEntry2<>(this.name3, this.data3, 3, "");
    } else if(data.isDouble() && data.getSuppliers() != null) {
      if(this.data3 == null) {
          this.name3 = name;
          this.data3 = data;
      } else {
          try {
              Supplier<?>[] existingSuppliers = this.data3.getSuppliers();
              Supplier<?>[] newSupplier = data.getSuppliers();
              
              @SuppressWarnings("unchecked")
              Supplier<Double>[] combined = new Supplier[existingSuppliers.length + newSupplier.length];
              System.arraycopy(existingSuppliers, 0, combined, 0, existingSuppliers.length);
              for (int i = 0; i < newSupplier.length; i++){
                combined[existingSuppliers.length+i] = (Supplier<Double>) newSupplier[i];
              }
              this.name3 = this.name3 + " | " + name;
              this.data3 = new Data<>(combined);
              
          } catch (Exception e) {
            
          }
      }
      logEntry3 = new LogEntry2<>(this.name3, this.data3, 3, "");
    } else if(data.isBoolean() && data.getSuppliers() != null) {
      if(this.data4 == null) {
          this.name4 = name;
          this.data4 = data;
      } else {
          try {
              Supplier<?>[] existingSuppliers = this.data4.getSuppliers();
              Supplier<?>[] newSupplier = data.getSuppliers();
              
              @SuppressWarnings("unchecked")
              Supplier<Double>[] combined = new Supplier[existingSuppliers.length + newSupplier.length];
              System.arraycopy(existingSuppliers, 0, combined, 0, existingSuppliers.length);
              for (int i = 0; i < newSupplier.length; i++){
                combined[existingSuppliers.length+i] = (Supplier<Double>) newSupplier[i];
              }
              this.name4 = this.name4 + " | " + name;
              this.data4 = new Data<>(combined);
          } catch (Exception e) {
            
          }
      }
      logEntry4 = new LogEntry2<>(this.name4, this.data4, 3, "");
    } else{
      entry = new LogEntry2<T>(name, data, logLevel, metaData);
      logEntries.add(entry);
    }
    
    return entry;
  }


  private LogEntry2<?> get(String name) {
    LogEntry2<?> e = find(name);
    return e != null 
    ?e 
    :new LogEntry2<>(name, null, 1, "");
  }

  private LogEntry2<?> find(String name) {
    for (LogEntry2<?> entry : logEntries) {
      if (entry.name.equals(name)) {
        return entry;
      }
    }
    return null;
  }
}