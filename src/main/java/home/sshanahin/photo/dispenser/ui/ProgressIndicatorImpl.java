package home.sshanahin.photo.dispenser.ui;

import home.sshanahin.photo.dispenser.ProgressIndicator;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProgressIndicatorImpl implements ProgressIndicator {
    private JProgressBar progressBar;
    private JFrame mainWindow;

    private PrintStream log;

    ProgressIndicatorImpl(JProgressBar progressBar, JFrame mainWindow) throws IOException {
        this.progressBar = progressBar;
        this.mainWindow = mainWindow;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String logFileName = "report_" + sdf.format(new Date()) + ".log";
        File logFile = new File(logFileName);
        if (!logFile.createNewFile()) {
            throw new IllegalAccessError("Cannot create log file");
        }
        this.log = new PrintStream(logFile);
        init();
    }

    private void init() {
        setProgress(0, "Ready");
    }

    @Override
    public void setProgress(int progress, String status) {
        progressBar.setString(status);
        progressBar.setValue(progress);
        String statusMessage = "Progress: " + progress + ", Status: " + status;
        log.println(statusMessage);
    }

    @Override
    public void setDoneStatus(String doneStatus) {
        progressBar.setValue(100);
        progressBar.setString("Done");
        JOptionPane.showMessageDialog(mainWindow, doneStatus, "Done", JOptionPane.INFORMATION_MESSAGE);
        log.println("Done, Status: " + doneStatus);
        init();
    }

    @Override
    public void setErrorStatus(String statusMessage, Throwable ex) {
        log.println("FATAL error: " + statusMessage);

        ex.printStackTrace(log);
        JOptionPane.showMessageDialog(mainWindow, ex.getMessage(), "Fatal Error happened", JOptionPane.ERROR_MESSAGE);
        init();
    }
}
