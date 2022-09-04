package pl.ms.ultrasound.ui;

import android.util.Log;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import pl.ms.ultrasound.BR;

public abstract class UserLog extends BaseObservable {

    private String message;
    public String tag = "LOG";

    @Bindable
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.setMessage(getTag(),message);
    }

    public void setMessage(String tag, String message) {

        message = LocalDateTime.now().format(DateTimeFormatter.ISO_TIME) + "\t" + message;

        if(this.message == null) {
            this.message = message;
        } else {
            this.message += '\n' + message;
        }

        //This would automatically update any binded views with this model whenever the message changes
        notifyPropertyChanged(BR.message);
        Log.i(tag,message);
    }

    public void clear() {
        this.message = null;
    }

    protected String getTag() {
        return tag;
    }


}
