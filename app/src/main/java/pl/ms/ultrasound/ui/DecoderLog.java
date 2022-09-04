package pl.ms.ultrasound.ui;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import pl.ms.ultrasound.BR;

/**
 * source: https://stackoverflow.com/questions/47743184/log-informations-in-a-textview
 */
public class DecoderLog extends UserLog {

    private static DecoderLog INSTANCE;

    public String tag = "DEC";

    private DecoderLog() {

    }

    public synchronized static DecoderLog getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new DecoderLog();
        }

        return INSTANCE;
    }

    @Override
    protected String getTag() {
        return tag;
    }

}
