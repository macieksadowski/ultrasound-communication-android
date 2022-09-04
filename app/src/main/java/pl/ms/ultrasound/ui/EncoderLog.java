package pl.ms.ultrasound.ui;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import pl.ms.ultrasound.BR;

/**
 * source: https://stackoverflow.com/questions/47743184/log-informations-in-a-textview
 */
public class EncoderLog extends UserLog {

    private static EncoderLog INSTANCE;
    public String tag = "ENC";

    private EncoderLog() {

    }

    public synchronized static EncoderLog getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new EncoderLog();
        }

        return INSTANCE;
    }

    @Override
    protected String getTag() {
        return tag;
    }

}
