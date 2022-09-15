package pl.ms.ultrasound.ui;

public interface DecoderCallback {

    void onStarted();

    void onStopped();

    void updateFigures();

    void onDataFrameCorrReceived(byte address, byte command, byte[] dataStr);

}
