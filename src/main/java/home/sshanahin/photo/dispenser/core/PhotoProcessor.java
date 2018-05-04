package home.sshanahin.photo.dispenser.core;

import home.sshanahin.photo.dispenser.ProgressIndicator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PhotoProcessor extends Thread {

    private String srcPath;
    private String destJpegsPath;
    private String destRawsPath;
    private ProgressIndicator progress;
    private boolean move = false;

    private Double overallProgress = 0d;
    private int errorsCount = 0;

    private String[] rawExtensions = {
            "3FR", "ARI", "ARW", "BAY", "CR2", "CRW", "CS1", "CXI", "DCR", "DNG", "EIP", "ERF",
            "FFF", "IIQ", "J6I", "K25", "KDC", "MEF", "MFW", "MOS", "MRW", "NEF", "NRW", "ORF",
            "PEF", "RAF", "RAW", "RW2", "RWL", "RWZ", "SR2", "SRF", "SRW", "X3F"};

    private String[] jpegExtensions = {"J", "J2C", "J2K", "JFI", "JFIF", "JIA", "JIF", "JIFF",
            "JNG", "JP2", "JPC", "JPD", "JPE", "JPEG", "JPF", "JPG", "JPG-LARGE", "JPG2", "JPS", "JPX", "JTF", "JXR"};

    public PhotoProcessor(String srcPath, String destJpegsPath, String destRawsPath, ProgressIndicator progress, boolean move) {
        this.srcPath = srcPath;
        this.destJpegsPath = destJpegsPath;
        this.destRawsPath = destRawsPath;
        this.progress = progress;
        this.move = move;
    }

    public void run() {
        doProcess();
    }


    public void doProcess() {
        try {
            File srcDirectory = new File(srcPath);
            File[] files = srcDirectory.listFiles();

            if (files != null && files.length > 0) {
                double fileCost = 20d / files.length;

                HashMap<String, File> rawsMap = new HashMap<>();
                HashMap<String, File> jpegsMap = new HashMap<>();

                for (File file : files) {
                    String fileName = file.getName();
                    String baseName = FilenameUtils.getBaseName(fileName);
                    String extension = FilenameUtils.getExtension(fileName);

                    if (ArrayUtils.contains(rawExtensions, StringUtils.upperCase(extension))) {
                        rawsMap.put(baseName, file);
                        updateProgress(fileCost, "RAW file: " + fileName + " found.");
                    } else if (ArrayUtils.contains(jpegExtensions, StringUtils.upperCase(extension))) {
                        jpegsMap.put(baseName, file);
                        updateProgress(fileCost, "JPEG file: " + fileName + " found.");
                    } else {
                        updateProgress(fileCost, "file: " + fileName + " skipped.");
                    }
                }

                int rawsDeleted = filterAndDeleteRaws(rawsMap, jpegsMap);

                File jpegsDir = new File(destJpegsPath);
                File rawsDir = new File(destRawsPath);
                FileUtils.forceMkdir(jpegsDir);
                FileUtils.forceMkdir(rawsDir);

                int jpegsProcessed = processFiles(jpegsDir, jpegsMap.values(), move);
                int rawsProcessed = processFiles(rawsDir, rawsMap.values(), move);
                String action = move ? "moved" : "copied";
                setDone("Processing done with "+errorsCount+" Errors: \n" + jpegsProcessed + " JPEGs " + action + ", \n"
                        + rawsProcessed + " RAWs " + action + ", \n"
                        + rawsDeleted + " RAWs DELETED. " );
            } else {
                setDone("No files found, nothing to move.");
            }
        } catch (Exception ex) {
            setErrorStatus("Fatal error:" + ex.getMessage(), ex);
        }
    }

    private int processFiles(File targetDir, Collection<File> files, boolean doMove) {
        int result = 0;
        double copyCost = 30d / (files.size() == 0? 1 : files.size());
        for (File file : files) {
            try {
                if (doMove) {
                    FileUtils.moveFileToDirectory(file, targetDir, true);
                    result++;
                    updateProgress(copyCost, "file: " + file.getName() + " moved to: " + targetDir.getPath());

                } else {
                    FileUtils.copyFileToDirectory(file, targetDir, true);
                    result++;
                    updateProgress(copyCost, "file: " + file.getName() + " copied to: " + targetDir.getPath());
                }
            } catch (Exception e) {
                errorsCount++;
                updateProgress(copyCost, "Cannot delete RAW file : " + file.getName() + ", " + e.getMessage());
            }
        }
        return result;
    }

    private int filterAndDeleteRaws(HashMap<String, File> rawsMap, HashMap<String, File> jpegsMap) {
        List<File> rawsToDelete = new ArrayList<>();
        List<String> keysToDelete = new ArrayList<>();
        int result = 0;
        double rawCost = 9d / (rawsMap.size() == 0? 1 : rawsMap.size() );

        for (Map.Entry<String, File> rawFileEntry : rawsMap.entrySet()) {
            File file = rawFileEntry.getValue();
            String baseName = rawFileEntry.getKey();
            if (jpegsMap.get(baseName) != null) {
                updateProgress(rawCost, "RAW file: " + file.getName() + ", has jpeg analog");
            } else {
                rawsToDelete.add(rawFileEntry.getValue());
                keysToDelete.add(baseName);
                updateProgress(rawCost, "RAW file: " + file.getName() + ", scheduled to DELETE.");
            }
        }
        for (String s : keysToDelete) {
            rawsMap.remove(s);
        }
        updateProgress(1d, "RAW files processed " + rawsToDelete.size() + " scheduled to DELETE.");

        double rawDelCost = 9d / (rawsToDelete.size() == 0 ? 1 : rawsToDelete.size());
        for (File file : rawsToDelete) {
            try {
                FileUtils.forceDelete(file);
                result++;
                updateProgress(rawDelCost, "RAW file DELETED: " + file.getName());
            } catch (Exception e) {
                errorsCount++;
                updateProgress(rawDelCost, "Cannot delete RAW file : " + file.getName() + ", " + e.getMessage());
            }
        }
        return result;
    }

    private void updateProgress(Double increment, String status) {
        overallProgress += increment;
        if (progress != null) {
            progress.setProgress(overallProgress.intValue(), status);
        } else {
            System.out.println("progress: " + overallProgress.intValue() + " %, status:  ");
        }
    }

    private void setDone(String message) {
        if (progress != null) {
            progress.setDoneStatus(message);
        } else {
            System.out.println("Process done. Status:" + message);
        }
    }

    private void setErrorStatus(String message, Throwable error) {
        if (progress != null) {
            progress.setErrorStatus(message, error);
        } else {
            System.err.println("FATAL error:" + message);
            error.printStackTrace();
        }
    }
}