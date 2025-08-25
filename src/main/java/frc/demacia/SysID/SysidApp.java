package frc.demacia.SysID;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

// ===================== SysidApp =====================
public class SysidApp {
    public static void main(String[] args) {
        Sysid app = new Sysid();
        app.show();
    }
}

// ===================== Sysid =====================
class Sysid implements Consumer<File> {
    JFrame frame = new JFrame("Sysid");
    FileChooserPanel fileChooser = new FileChooserPanel(this);
    JList<MotorData> motorList = new JList<>();
    SysidResultPanel result = new SysidResultPanel(this);
    JTextArea msgArea = new JTextArea();
    JScrollPane msgPane = new JScrollPane(msgArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    LogReader log;

    private static Sysid sysid = null;

    public Sysid() {
        sysid = this;
        frame.setSize(1024,800);
        frame.setMinimumSize(new Dimension(1024,800));
        frame.setLocation(300,200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        var pane = frame.getContentPane();
        pane.setLayout(new GridBagLayout());
        motorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        motorList.setMinimumSize(new Dimension(300,400));
        motorList.setBorder(BorderFactory.createEtchedBorder());
        pane.add(fileChooser, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 5, 5));
        pane.add(motorList, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 0), 5, 5));
        pane.add(msgPane, new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 0), 5, 5));
        pane.add(result, new GridBagConstraints(1, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 0), 5, 5));
        frame.pack();
    }

    public void show() {
        frame.setVisible(true);
    }

    public static void msg(String msg) {
        if(sysid != null) {
            sysid.msgArea.append(msg + "\n");
        }
    }

    @Override
    public void accept(File file) {
        System.out.println("File set to " + file);
        try {
            MotorData.motors.clear();
            log = new LogReader(file.getAbsolutePath());
            for(MotorData m : MotorData.motors) {
                m.getMotorData();
            }
            motorList.setListData(MotorData.motors.toArray(new MotorData[0]));
            msg("File " + file.getName() + " loaded");
        } catch (Exception e) {
            msg("IO error - for file " + file + " error=" + e);
            fileChooser.field.setText("");
        }
    }

    public MotorData getMotor() {
        var s = motorList.getSelectedValue();
        if(s != null) {
            return s;
        }
        return null;
    }


    public static void main(String[] args) {
        // יוצרים מופע של Sysid ומציגים את ה-GUI
        Sysid app = new Sysid();
        app.show();
    }

}

// ===================== FileChooserPanel =====================
class FileChooserPanel extends JPanel implements ActionListener {
    JButton button;
    JTextField field;
    JFileChooser chooser;
    Consumer<File> consumer;

    public FileChooserPanel(Consumer<File> consumer) {
        super(new FlowLayout(FlowLayout.LEFT, 10, 5));
        this.consumer = consumer;
        button = new JButton("File:");
        field = new JTextField(50);
        field.setEditable(false);
        button.addActionListener(this);
        chooser = new JFileChooser(System.getProperty("user.dir"));
        chooser.setFileFilter(new FileNameExtensionFilter("wpi log", "wpilog"));
        add(button);
        add(field);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int res = chooser.showOpenDialog(null);
        if(res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            field.setText(file.getAbsolutePath());
            if(consumer != null) {
                consumer.accept(file);
            }
        }
    }
}

// ===================== SysidResultPanel =====================
class SysidResultPanel extends JPanel {
    public static int nK = KTypes.values().length;
    JCheckBox[] checkBoxes;
    JLabel[][] k;
    JButton applyButton;
    JLabel[] velLabels = {new JLabel("Velocity Range"), new JLabel("0%-30%"), new JLabel("30%-70%"), new JLabel("70%-100%")};
    JLabel[] countLabels = {new JLabel("Number of records"), new JLabel("0"), new JLabel("0"), new JLabel("0")};
    JLabel[] avgErrorLabels = {new JLabel("Average Error %"), new JLabel("0"), new JLabel("0"), new JLabel("0")};
    JLabel[] maxErrorLabels = {new JLabel("Max Error %"), new JLabel("0"), new JLabel("0"), new JLabel("0")};
    JLabel[] kpLabels = {new JLabel("KP"), new JLabel("0"), new JLabel("0"), new JLabel("0")};
    Sysid app;

    public SysidResultPanel(Sysid app) {
        super(new GridLayout(nK + 7, 4, 5, 5));
        this.app = app;

        for(JLabel l : velLabels) add(l);
        for(JLabel l : countLabels) add(l);
        for(JLabel l : avgErrorLabels) add(l);
        for(JLabel l : maxErrorLabels) add(l);

        checkBoxes = new JCheckBox[nK];
        k = new JLabel[nK][3];
        for(int i = 0; i < nK; i++) {
            checkBoxes[i] = new JCheckBox(KTypes.values()[i].name());
            if(i < 3) { checkBoxes[i].setEnabled(false); checkBoxes[i].setSelected(true); } else { checkBoxes[i].setSelected(false);}
            add(checkBoxes[i]);
            for(int j=0;j<3;j++){
                k[i][j] = new JLabel("0");
                add(k[i][j]);
            }
        }

        for(JLabel l : kpLabels) add(l);

        applyButton = new JButton("Calculate");
        add(applyButton);

        applyButton.addActionListener(e -> {
            MotorData motorData = app.getMotor();
            if(motorData != null) {
                SysidCalculate calc = new SysidCalculate(motorData, getSelectedTypes());
                for(VelocityRange range : VelocityRange.values()) {
                    int i = range.ordinal();
                    for(KTypes type : KTypes.values()) {
                        k[type.ordinal()][i].setText(String.format("%7.5f", calc.getK(type, range)));
                    }
                    countLabels[i+1].setText(Integer.toString(calc.getCount(range)));
                    avgErrorLabels[i+1].setText(String.format("%4.2f%%", calc.getAverageError(range)));
                    maxErrorLabels[i+1].setText(String.format("%4.2f%%", calc.getMaxError(range)));
                    kpLabels[i+1].setText(String.format("%.5f", calc.getKP(range)));
                }
            } else {
                Sysid.msg("No motor selected");
            }
        });
    }

    private EnumSet<KTypes> getSelectedTypes() {
        EnumSet<KTypes> set = EnumSet.noneOf(KTypes.class);
        for(int i=0;i<checkBoxes.length;i++){
            if(checkBoxes[i].isSelected()) set.add(KTypes.values()[i]);
        }
        return set;
    }
}

// ===================== MotorData =====================
class MotorData {
    static List<MotorData> motors = new ArrayList<>();
    List<LogReader.SyncedDataPoint> data = new ArrayList<>();
    String name = "Motor";

    public void getMotorData() {
        // נתונים לדמו - בפועל מגיעים מה־LogReader
        data = new ArrayList<>();
        for(int i=0;i<500;i++){
            LogReader.SyncedDataPoint dp = new LogReader.SyncedDataPoint(i*0.1, i*0.01, i*0.005, i*0.02, i);
            data.add(dp);
        }
    }
}

// ===================== VelocityRange =====================
enum VelocityRange {SLOW,MID,HIGH}

// ===================== KTypes =====================
enum KTypes {KS,KV,KA}

// ===================== SysidCalculate =====================
class SysidCalculate {
    MotorData motor;
    EnumSet<KTypes> types;

    Map<VelocityRange, LogReader.BucketResult> results = new HashMap<>();

    public SysidCalculate(MotorData motor, EnumSet<KTypes> types) {
        this.motor = motor;
        this.types = types;
        analyze();
    }

    private void analyze() {
        List<LogReader.SyncedDataPoint> d = motor.data;
        int n = d.size();
        int split = n/3;
        results.put(VelocityRange.SLOW,new LogReader.BucketResult(0.1,0.2,0.3,0.01,split));
        results.put(VelocityRange.MID,new LogReader.BucketResult(0.2,0.3,0.4,0.02,split));
        results.put(VelocityRange.HIGH,new LogReader.BucketResult(0.3,0.4,0.5,0.03,n-2*split));
    }

    public double getK(KTypes type, VelocityRange range) {
        LogReader.BucketResult b = results.get(range);
        if(b==null) return 0;
        switch(type) {
            case KS: return b.ks;
            case KV: return b.kv;
            case KA: return b.ka;
        }
        return 0;
    }

    public int getCount(VelocityRange range) {return results.get(range).points;}
    public double getAverageError(VelocityRange range) {return results.get(range).avgError*100;}
    public double getMaxError(VelocityRange range) {return results.get(range).avgError*120;}
    public double getKP(VelocityRange range) {return results.get(range).ks*2;} // לדמו
}

// ===================== LogReader =====================
class LogReader {
    String fileName;

    public LogReader(String fileName) { this.fileName = fileName; }

    public static class BucketResult {
        double ks, kv, ka, avgError;
        int points;
        public BucketResult(double ks,double kv,double ka,double avgError,int points){
            this.ks=ks; this.kv=kv; this.ka=ka; this.avgError=avgError; this.points=points;
        }
    }

    public static class SyncedDataPoint {
        double velocity, position, acceleration, rawAcceleration, voltage;
        long timestamp;
        SyncedDataPoint prev;
        SyncedDataPoint(double velocity,double position,double acceleration,double voltage,long timestamp){
            this.velocity=velocity; this.position=position; this.acceleration=acceleration; this.voltage=voltage; this.timestamp=timestamp;
        }
    }
}
