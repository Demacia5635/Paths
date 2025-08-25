// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.demacia.utils.Log;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;

import edu.wpi.first.networktables.BooleanArrayPublisher;
import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.FloatArrayPublisher;
import edu.wpi.first.networktables.FloatPublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.Publisher;
import edu.wpi.first.util.datalog.BooleanArrayLogEntry;
import edu.wpi.first.util.datalog.BooleanLogEntry;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.DataLogEntry;
import edu.wpi.first.util.datalog.FloatArrayLogEntry;
import edu.wpi.first.util.datalog.FloatLogEntry;
import frc.robot.RobotContainer;

public class LogEntry<T> {

    private final LogManager logManager;

    DataLogEntry entry;
    StatusSignal<T>[] phoenix6Status;
    Supplier<T> getter;
    BiConsumer<float[], Long> consumer = null;
    String name;
    String metaData;
    Publisher ntPublisher;
    float[] lastValue;
    private double precision = 0; // Configurable precision for change detection
    private int skipedCycles2 = 0;
    private int SkipCycle = 1; // Default: log every cycle, can be changed for optimization

    private boolean isFloat;
    private boolean isBoolean;
    private boolean isArray;

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
    LogEntry(String name, StatusSignal<T>[] phoenix6Status, Supplier<T> getter, int logLevel, String metaData, boolean isFloat, boolean isBoolean, boolean isArray) {

        logManager = LogManager.logManager;

        this.name = name;
        this.logLevel = logLevel;
        this.phoenix6Status = phoenix6Status;
        this.getter = getter;
        this.metaData = metaData;

        this.isFloat = isFloat;
        this.isBoolean = isBoolean;
        this.isArray = isArray;

        this.entry = createLogEntry(logManager.log, name, metaData);

        if (logLevel == 4 || (logLevel == 3 && !RobotContainer.isComp())) {
        this.ntPublisher = createPublisher(logManager.table, name);
        } else {
        this.ntPublisher = null;
        }
        
    }

    /*
        * perform a periodic log
        * get the data from the getters and call the actual log
        */
    void log() {
        skipedCycles2++;
        if (skipedCycles2 < SkipCycle) {
        return;
        }
        skipedCycles2 = 0;
        float[] value  = null;
        long time = 0;

        if (phoenix6Status != null) {
            StatusCode  st = StatusSignal.refreshAll(phoenix6Status);
            if (st == StatusCode.OK) {
                float[] V = new float[phoenix6Status.length];
                for (int i = 0; i < phoenix6Status.length; i++) {
                    V[i] = (float)phoenix6Status[i].getValueAsDouble();
                if (i == 0) {
                    time = (long) (phoenix6Status[i].getTimestamp().getTime() * 1000);
                }
                }
                value = V;
            } else {
                return;
            }
        } else if (getter != null){
            value = (float[])getter.get();
            time = 0;
        } if (value != null) {
            log(value, time);
        }
    }

    /*
        * log a value use zero (current) time
        */
    public void log(float[] value) {
        log(value, 0);
    }

    private boolean hasSignificantChange(float[] value) {
        if (lastValue == null) {
        return true;
        }
        
        if (isFloat) {
            if (isArray) {
                float[] arrV = (float[]) value;
                float[] lastArrV = (float[]) lastValue;
                if (arrV.length != lastArrV.length) {
                    return true;
                }
                for (int i = 0; i < arrV.length; i++) {
                    if (Math.abs(arrV[i] - lastArrV[i]) >= precision) {
                    return true;
                    }
                }
                return false;
            } else {
                return Math.abs((float) value[0] - (float) lastValue[0]) >= precision;
            }
        } else {
            return !value.equals(lastValue);
        }
    }

    /*
        * Log data and time if data changed
        * also publish to network table (if required)
        * also call consumer if set
        */
    public void log(float[] value, long time) {
        if (!hasSignificantChange(value)) {
        return;
        }

        appendEntry(value, time);

        if (ntPublisher != null) {
        publishToNetworkTable(value);
        }
        
        // Call consumer if set
        if (consumer != null) {
            consumer.accept(value, time);
        }
        
        lastValue = value;
    }

    private DataLogEntry createLogEntry(DataLog log, String name, String metaData) {
        if (isFloat) {
            if (isArray) {
                return new FloatArrayLogEntry(log, name, metaData);
            }
            return new FloatLogEntry(log, name, metaData);
        } else if (isBoolean) {
            if (isArray) {
                return new BooleanArrayLogEntry(log, name, metaData);
            }
            return new BooleanLogEntry(log, name, metaData);
        }
        throw new IllegalArgumentException("Unsupported type");
    }

    private Publisher createPublisher(NetworkTable table, String name) {
        if (isFloat) {
            if (isArray) {
                return table.getFloatArrayTopic(name).publish();
            }
            return table.getFloatTopic(name).publish();
        } else if (isBoolean) {
            if (isArray) {
                return table.getBooleanArrayTopic(name).publish();
            }
            return table.getBooleanTopic(name).publish();
        }
        throw new IllegalArgumentException("Unsupported type");
    }

    private void appendEntry(float[] value, long time) {
        if (isFloat) {
            if (isArray) {
                ((FloatArrayLogEntry) entry).append((float[]) value, time);
            }
            ((FloatLogEntry) entry).append((Float) value[0], time);
        } else if (isBoolean) {
            if (isArray) {
                boolean[] bools = new boolean[value.length];
                for (int i = 0; i < value.length; i++) {
                    bools[i] = (value[i] != 0);
                }
                ((BooleanArrayLogEntry) entry).append(bools, time);
            }
            ((BooleanLogEntry) entry).append(value[0] != 0, time);
        }
    }

    private void publishToNetworkTable(float[] value) {
        if (isFloat) {
            if (isArray) {
                ((FloatArrayPublisher) ntPublisher).set((float[]) value);
            }
            ((FloatPublisher) ntPublisher).set((Float) value[0]);
        } else if (isBoolean) {
            if (isArray) {
                boolean[] bools = new boolean[value.length];
                for (int i = 0; i < value.length; i++) {
                    bools[i] = (value[i] != 0);
                }
                ((BooleanArrayPublisher) ntPublisher).set(bools);
            }
            ((BooleanPublisher) ntPublisher).set(value[0] != 0);
        }
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
    public void setConsumer(BiConsumer<float[], Long> consumer) {
        this.consumer = consumer;
    }

    public void removeInComp() {
        if (logLevel == 3 && ntPublisher != null) {
        ntPublisher.close();
        }
    }
}
