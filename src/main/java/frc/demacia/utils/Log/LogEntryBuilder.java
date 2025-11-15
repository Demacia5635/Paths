package frc.demacia.utils.Log;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.ctre.phoenix6.StatusSignal;

import frc.demacia.utils.Data;

public class LogEntryBuilder<T> {

    public static enum LogLevel { LOG_ONLY_NOT_IN_COMP, LOG_ONLY, LOG_AND_NT_NOT_IN_COMP, LOG_AND_NT} 

    private String name;
    private LogLevel logLevel = LogLevel.LOG_ONLY_NOT_IN_COMP;
    private String metadata = "";
    private BiConsumer<T[], Long> consumer = null;

    private Data<T> data;
    
    @SuppressWarnings("unchecked")
    LogEntryBuilder(String name, StatusSignal<T> ... statusSignals) {
        this.name = name;
        data = new Data<>(statusSignals);
    }
    
    @SuppressWarnings("unchecked")
    LogEntryBuilder(String name, Supplier<T> ... suppliers) {
        this.name = name;
        data = new Data<>(suppliers);
    }
    
    public LogEntryBuilder<T> withLogLevel(LogLevel level) {
        this.logLevel = level;
        return this;
    }
    
    public LogEntryBuilder<T> withMetaData(String metaData) {
        this.metadata = metaData;
        return this;
    }
    
    public LogEntryBuilder<T> WithIsMotor() {
        this.metadata = "motor";
        return this;
    }
    
    public LogEntryBuilder<T> WithConsumer(BiConsumer<T[], Long> consumer) {
        this.consumer = consumer;
        return this;
    }
    
    public LogEntry<T> build() {
        if (LogManager.logManager == null) {
            throw new IllegalStateException("LogManager2 not initialized. Create LogManager2 instance before building log entries.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Log entry name cannot be null or empty");
        }
        if (logLevel == null) {
            throw new IllegalArgumentException("Log level cant be null: ");
        }
        LogEntry<T> entry = LogManager.logManager.add(name, data, logLevel, metadata);
        if (consumer != null) {
            entry.setConsumer(consumer);
        }
        return entry;
    }
}