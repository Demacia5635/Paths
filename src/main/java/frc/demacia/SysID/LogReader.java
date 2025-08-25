package frc.demacia.SysID;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ejml.simple.SimpleMatrix;

public class LogReader {

    private static Map<Integer, EntryDescription> entries;

    private static class EntryDescription {
        int entryId;
        String name;
        String type;
        String metadata;
        List<DataPoint> data = new ArrayList<>();

        EntryDescription(int id, String name, String type, String meta) {
            this.entryId = id;
            this.name = name;
            this.type = type;
            this.metadata = meta;
        }
    }

    private static class DataPoint {
        long timestamp;
        double[] value;

        DataPoint(long timestamp, double[] value) {
            this.timestamp = timestamp;
            this.value = value.clone();
        }
    }

    public static class SysIDResults {
        public double kS;
        public double kV;
        public double kA;
        public double kP;
        public double avgError;
        public int dataPoints;

        // מידע לכל תחום
        public BucketResult slow;
        public BucketResult mid;
        public BucketResult high;

        public SysIDResults(double kS, double kV, double kA, double kP, double avgError, int points,
                            BucketResult slow, BucketResult mid, BucketResult high) {
            this.kS = kS;
            this.kV = kV;
            this.kA = kA;
            this.kP = kP;
            this.avgError = avgError;
            this.dataPoints = points;
            this.slow = slow;
            this.mid = mid;
            this.high = high;
        }

        @Override
        public String toString() {
            return String.format(
                    "KS=%.4f, KV=%.4f, KA=%.6f, KP=%.4f, AvgError=%.4f, Points=%d%n" +
                            "Slow: %s%nMid: %s%nHigh: %s",
                    kS, kV, kA, kP, avgError, dataPoints,
                    slow != null ? slow.toString() : "null",
                    mid != null ? mid.toString() : "null",
                    high != null ? high.toString() : "null"
            );
        }
    }

    public static Map<String, SysIDResults> getResult(String fileName) {
        entries = new HashMap<>();
        try {
            WpilogReader(fileName);
            return performAnalysis();
        } catch (IOException e) {
            System.err.println("Error reading log file: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // ------------------- קריאת WPILog -------------------
    private static void WpilogReader(String fileName) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(fileName);
             DataInputStream dataInputStream = new DataInputStream(fileInputStream)) {
            if (!Arrays.equals(readHeader(dataInputStream), "WPILOG".getBytes())) {
                throw new IOException("Invalid WPILOG file format");
            }
            skipHeaderExtra(dataInputStream);
            readRecords(dataInputStream);
        }
    }

    private static byte[] readHeader(DataInputStream dataInputStream) throws IOException {
        byte[] signature = new byte[6];
        dataInputStream.readFully(signature);
        return signature;
    }

    private static void skipHeaderExtra(DataInputStream dataInputStream) throws IOException {
        Short.reverseBytes(dataInputStream.readShort());
        int extraLength = Integer.reverseBytes(dataInputStream.readInt());
        dataInputStream.skipBytes(extraLength);
    }

    private static void readRecords(DataInputStream dataInputStream) throws IOException {
        while (dataInputStream.available() > 0) {
            try {
                readRecord(dataInputStream);
            } catch (EOFException e) {
                break;
            }
        }
    }

    private static void readRecord(DataInputStream dataInputStream) throws IOException {
        int headerByte = dataInputStream.readUnsignedByte();
        int idLength = (headerByte & 0x3) + 1;
        int payloadLength = (headerByte >> 2 & 0x3) + 1;
        int timestampLength = (headerByte >> 4 & 0x7) + 1;

        int recordId = readLittleEndianInt(dataInputStream, idLength);
        int payloadSize = readLittleEndianInt(dataInputStream, payloadLength);
        long timestamp = readLittleEndianLong(dataInputStream, timestampLength);

        if (recordId == 0) {
            addEntryFromControlRecord(dataInputStream, payloadSize);
        } else {
            EntryDescription entry = entries.get(recordId);
            if (entry != null && "double[]".equals(entry.type) && payloadSize % 8 == 0) {
                int count = payloadSize / 8;
                double[] value = new double[count];
                for (int i = 0; i < count; i++) {
                    long raw = Long.reverseBytes(dataInputStream.readLong());
                    value[i] = Double.longBitsToDouble(raw);
                }
                entry.data.add(new DataPoint(timestamp, value));
            } else {
                dataInputStream.skipBytes(payloadSize);
            }
        }
    }

    private static void addEntryFromControlRecord(DataInputStream dataInputStream, int payloadSize) throws IOException {
        int recordType = dataInputStream.readUnsignedByte();
        if (recordType == 0) {
            int entryId = Integer.reverseBytes(dataInputStream.readInt());

            int nameLength = Integer.reverseBytes(dataInputStream.readInt());
            String name = readString(dataInputStream, nameLength);

            int typeLength = Integer.reverseBytes(dataInputStream.readInt());
            String type = readString(dataInputStream, typeLength);

            int metaLength = Integer.reverseBytes(dataInputStream.readInt());
            String metadata = readString(dataInputStream, metaLength);

            if ("motor".equals(metadata)) {
                entries.put(entryId, new EntryDescription(entryId, name, type, metadata));
            }
        } else {
            dataInputStream.skipBytes(payloadSize - 1);
        }
    }

    private static String readString(DataInputStream dataInputStream, int length) throws IOException {
        byte[] bytes = new byte[length];
        dataInputStream.readFully(bytes);
        return new String(bytes, "UTF-8");
    }

    private static int readLittleEndianInt(DataInputStream dis, int bytes) throws IOException {
        int result = 0;
        for (int i = 0; i < bytes; i++) {
            result |= (dis.readUnsignedByte() << (i * 8));
        }
        return result;
    }

    private static long readLittleEndianLong(DataInputStream dis, int bytes) throws IOException {
        long result = 0L;
        for (int i = 0; i < bytes; i++) {
            result |= ((long) dis.readUnsignedByte() << (i * 8));
        }
        return result;
    }

    // ------------------- ניתוח -------------------
    private static Map<String, SysIDResults> performAnalysis() {
        Map<String, SysIDResults> results = new HashMap<>();
        Set<String> groups = findGroups();
        for (String group : groups) {
            SysIDResults result = analyzeGroup(group);
            if (result != null) {
                results.put(group, result);
                System.out.println("Group " + group + ": " + result);
            }
        }
        return results;
    }

    private static Set<String> findGroups() {
        Set<String> groups = new HashSet<>();
        for (EntryDescription entry : entries.values()) {
            String name = entry.name;
            int lastSlash = name.lastIndexOf('/');
            if (lastSlash > 0) {
                groups.add(name.substring(0, lastSlash));
            }
        }
        return groups;
    }

    private static SysIDResults analyzeGroup(String groupName) {
        EntryDescription entry = findEntry(
                groupName + "/Position and Velocity and Acceleration and Voltage and Current and CloseLoopError and CloseLoopSP");

        if (entry == null || entry.data.isEmpty()) return null;

        List<SyncedDataPoint> syncedData = synchronizeData(entry.data);

        return performSysIdLikeAnalysis(syncedData);
    }

    private static EntryDescription findEntry(String name) {
        for (EntryDescription entry : entries.values()) {
            if (entry.name.equals(name)) return entry;
        }
        return null;
    }

    private static class SyncedDataPoint {
        double velocity, position, acceleration, rawAcceleration, voltage;
        long timestamp;
        SyncedDataPoint prev;

        SyncedDataPoint(double velocity, double position, double acceleration, double voltage, long timestamp) {
            this.velocity = velocity;
            this.position = position;
            this.acceleration = acceleration;
            this.voltage = voltage;
            this.timestamp = timestamp;
        }
    }

    private static List<SyncedDataPoint> synchronizeData(List<DataPoint> dataPoints) {
        List<SyncedDataPoint> result = new ArrayList<>();
        for (DataPoint dp : dataPoints) {
            result.add(new SyncedDataPoint(dp.value[1], dp.value[0], dp.value[2], dp.value[3], dp.timestamp));
        }
        updateAccelerationAndMinPower(result);
        return result;
    }

    private static void updateAccelerationAndMinPower(List<SyncedDataPoint> data) {
        SyncedDataPoint prev = null;
        for (SyncedDataPoint m : data) {
            m.rawAcceleration = m.acceleration;
            if (prev != null) {
                double dt = (m.timestamp - prev.timestamp) / 1000.0;
                if (dt <= 0) dt = 1e-6;
                double acc = (m.velocity - prev.velocity) / dt;
                m.acceleration = (m.acceleration * dt + acc * 0.02) / (dt + 0.02);
            }
            m.prev = prev;
            prev = m;
        }
    }

    // ------------------- חישוב SysID -------------------
    public static class BucketResult {
        double ks, kv, ka, avgError;
        int points;

        BucketResult(double ks, double kv, double ka, double avgError, int points) {
            this.ks = ks;
            this.kv = kv;
            this.ka = ka;
            this.avgError = avgError;
            this.points = points;
        }

        @Override
        public String toString() {
            return String.format("KS=%.4f, KV=%.4f, KA=%.4f, AvgError=%.4f, Points=%d", ks, kv, ka, avgError, points);
        }
    }

    private static SysIDResults performSysIdLikeAnalysis(List<SyncedDataPoint> data) {
        if (data.isEmpty()) return new SysIDResults(0,0,0,0,0,0,null,null,null);

        double maxV = 0.0;
        double minPowerToMove = Double.MAX_VALUE;
        SyncedDataPoint prev = null;
        for (SyncedDataPoint m : data) {
            maxV = Math.max(maxV, Math.abs(m.velocity));
            if (prev != null) {
                double absVolt = Math.abs(m.voltage);
                if (prev.velocity==0 && m.velocity!=0 && absVolt>0.01 && (m.velocity*m.voltage)>0 && absVolt<minPowerToMove) {
                    minPowerToMove = absVolt;
                }
            }
            prev = m;
        }
        if (minPowerToMove==Double.MAX_VALUE) minPowerToMove=0.0;

        double[] vRange = new double[]{maxV*0.3, maxV*0.7, maxV};
        List<SyncedDataPoint> slow = new ArrayList<>();
        List<SyncedDataPoint> mid = new ArrayList<>();
        List<SyncedDataPoint> high = new ArrayList<>();

        for (SyncedDataPoint d : data) {
            int r = rangeBucket(d, vRange);
            if(r==0) slow.add(d);
            else if(r==1) mid.add(d);
            else if(r==2) high.add(d);
        }

        BucketResult slowR = solveBucket(slow);
        BucketResult midR = solveBucket(mid);
        BucketResult highR = solveBucket(high);

        BucketResult best = midR!=null ? midR : (highR!=null ? highR : slowR);

        double kp=0;
        if(best!=null){
            kp = CalculateFeedbackGains.calculateFeedbackGains(best.kv, best.ka)[0];
        }

        return new SysIDResults(
                minPowerToMove,
                best!=null ? best.kv : 0,
                best!=null ? best.ka : 0,
                kp,
                best!=null ? best.avgError : 0,
                data.size(),
                slowR,
                midR,
                highR
        );
    }

    private static int rangeBucket(SyncedDataPoint d, double[] vRange){
        double vAbs=Math.abs(d.velocity);
        int i=vAbs<vRange[0]?0:(vAbs<vRange[1]?1:2);
        if(valid(vAbs,0.1)&&valid(d.voltage,0.05)){
            if(d.prev!=null){
                if(valid(Math.abs(d.prev.velocity),0.1)&&valid(Math.abs(d.prev.voltage),0.2)) return i;
                else return -1;
            }else return i;
        }else return -1;
    }

    private static boolean valid(double val,double min){return val>min||val<-min;}

    private static BucketResult solveBucket(List<SyncedDataPoint> arr){
        if(arr==null||arr.size()<=50) return null;
        int rows=arr.size();
        SimpleMatrix mat=new SimpleMatrix(rows,3);
        SimpleMatrix volt=new SimpleMatrix(rows,1);
        for(int r=0;r<rows;r++){
            SyncedDataPoint d=arr.get(r);
            mat.set(r,0,Math.signum(d.velocity));
            mat.set(r,1,d.velocity);
            mat.set(r,2,d.acceleration);
            volt.set(r,0,d.voltage);
        }
        SimpleMatrix res=mat.solve(volt);
        SimpleMatrix pred=mat.mult(res);
        SimpleMatrix error=volt.minus(pred);
        double sum=0;
        for(int i=0;i<rows;i++){
            SyncedDataPoint d=arr.get(i);
            double rel=Math.abs(error.get(i,0)/d.voltage);
            sum+=rel;
        }
        double avg=sum/rows;
        return new BucketResult(res.get(0,0),res.get(1,0),res.get(2,0),avg,rows);
    }

    public static class CalculateFeedbackGains{
        public static double[] calculateFeedbackGains(double kv,double ka){
            double kP=(2.0*kv)/ka;
            double kV=1.0/ka;
            return new double[]{kP,kV};
        }
    }
}
