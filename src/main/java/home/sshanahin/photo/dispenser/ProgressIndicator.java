package home.sshanahin.photo.dispenser;

public interface ProgressIndicator {
     void setProgress(int progress, String statusMessage);
     void setDoneStatus(String doneStatus);
     void setErrorStatus(String statusMessage, Throwable error);
}
