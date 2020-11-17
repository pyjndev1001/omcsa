package logo.omcsa_v9.utils;

public interface FileDownload {
    void startDownload();
    void endDownload();
    void setProgress(int nProgress);
    void errorDownload(String errorString);
}
