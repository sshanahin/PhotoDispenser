package home.sshanahin.photo.dispenser.ui;

import home.sshanahin.photo.dispenser.ProgressIndicator;
import home.sshanahin.photo.dispenser.core.PhotoProcessor;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SetupWindowManager {
    private String srcPath = "D:\\##Photo\\##PhotoToSort\\###FromSonyCamera";
    private String destJpegPath = "D:\\##Photo\\##PhotoArchive\\" + new GregorianCalendar().get(Calendar.YEAR);
    private String destRawPath = "D:\\##Photo\\##PhotoArchiveRaw\\" + new GregorianCalendar().get(Calendar.YEAR);


    public SetupWindowManager() {
        //todo: read pathes from properties
    }

    public void initWindow() {

        JFrame mainWindow = new JFrame("Photo Dispenser");

        mainWindow.getContentPane().setLayout(new GridBagLayout());
        Insets defaultInsets = new Insets(10, 10, 10, 10);
        Insets smallInsets = new Insets(5, 5, 5, 5);

        JPanel sourcePanel = new JPanel();
        sourcePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        JFilePicker sourcePicker = new JFilePicker("Source folder:", "Browse...");
        sourcePicker.getTextField().setText(srcPath);
        sourcePicker.getFileChooser().setCurrentDirectory(new File(srcPath));
        sourcePanel.add(sourcePicker);

        JPanel destinationPanel = new JPanel(new GridLayout(2, 1));
        destinationPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

        JFilePicker jpegPicker = new JFilePicker("JPEG files folder:", "Browse...");
        jpegPicker.getTextField().setText(destJpegPath);
        jpegPicker.getFileChooser().setCurrentDirectory(new File(destJpegPath));

        JFilePicker rawPicker = new JFilePicker("RAW files folder:", "Browse...");
        rawPicker.getTextField().setText(destRawPath);
        rawPicker.getFileChooser().setCurrentDirectory(new File(destRawPath));

        destinationPanel.add(jpegPicker);
        destinationPanel.add(rawPicker);


        JCheckBox createFolders = new JCheckBox("Create Destination folders witn Name as Source", true);
        JCheckBox moveFiles = new JCheckBox("Move files instead of Copy:", true);

        JProgressBar progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        progressBar.setBorderPainted(true);
        progressBar.setStringPainted(true);
        progressBar.setString("Idle");
        progressBar.setValue(0);

        JButton doitButton = new JButton("Do it!");

        JPanel checkBoxPanel = new JPanel(new GridBagLayout());
        checkBoxPanel.add(createFolders,
                new GridBagConstraints(0, 1, 2, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, smallInsets, 0, 0));
        checkBoxPanel.add(moveFiles,
                new GridBagConstraints(0, 2, 2, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, smallInsets, 0, 0));
        checkBoxPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));



        mainWindow.getContentPane().add(sourcePanel,
                new GridBagConstraints(0, 0, 3, 1, 1, 1, GridBagConstraints.BASELINE, GridBagConstraints.NONE, defaultInsets, 1, 1));
        mainWindow.getContentPane().add(destinationPanel,
                new GridBagConstraints(0, 1, 3, 1, 1, 1, GridBagConstraints.BASELINE, GridBagConstraints.NONE, defaultInsets, 1, 1));
        mainWindow.getContentPane().add(checkBoxPanel,
                new GridBagConstraints(0, 2, 2, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.REMAINDER, defaultInsets, 0, 0));


        mainWindow.add(doitButton,
                new GridBagConstraints(2, 2, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, smallInsets, 0, 0));
        mainWindow.add(progressBar,
                new GridBagConstraints(0, 3, 3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, smallInsets, 0, 0));

        mainWindow.pack();

        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        doitButton.addActionListener(e -> {
            try {
                String source = sourcePicker.getSelectedFilePath();
                String jpegDest = jpegPicker.getSelectedFilePath();
                String rawDest = rawPicker.getSelectedFilePath();

                if (pathsAreOk(source, jpegDest, rawDest)) {
                    String folderName = getFolderName(source);
                    if(createFolders.isSelected()) {
                        jpegDest = jpegDest + File.separator + folderName;
                        rawDest = rawDest + File.separator + folderName;
                    }
                    boolean confirmed = JOptionPane.showConfirmDialog(mainWindow, "We are about to " + (moveFiles.isSelected()? "move":"copy")
                                    + " from:  " + source + "  \n\n JPEGs are going to:  " + jpegDest + " \n RAWs will be copied to:  " + rawDest,
                            "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
                    if (confirmed) {
                        ProgressIndicator progressIndicator = new ProgressIndicatorImpl( progressBar, mainWindow);
                        PhotoProcessor processor = new PhotoProcessor(source, jpegDest, rawDest, progressIndicator, moveFiles.isSelected());
                        processor.start();
                    }
                } else {
                    JOptionPane.showMessageDialog(mainWindow, "Please specify correct paths: source folder, parent folder for JPEGs folder, parent folder for RAWs folder", "Input error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainWindow, ex.getMessage(), "Error starting process...", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        mainWindow.setVisible(true);
    }

    private boolean pathsAreOk(String source, String jpegDest, String rawDest) {
        if (isEmptyTrim(source) || isEmptyTrim(jpegDest) || isEmptyTrim(rawDest)) {
            return false;
        }
        File test = new File(source);
        if (!test.exists() || !test.isDirectory() || !test.canRead()) {
            return false;
        }
        test = new File(jpegDest);
        if (!test.exists() || !test.isDirectory() || !test.canWrite()) {
            return false;
        }
        test = new File(rawDest);
        return test.exists() && test.isDirectory() && test.canWrite();
    }

    private boolean isEmptyTrim(String str) {
        return str == null || str.trim().length() == 0;
    }

    private String getFolderName(String src) {
        File srcFile = new File(src);
        return srcFile.getName();
    }
}



