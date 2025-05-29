package com.example.lab5_20202269;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    // Interfaz para enviar datos a la Activity
    public interface TimePickerListener {
        void onTimeSelected(int hour, int minute);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        return new TimePickerDialog(requireActivity(), this,
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
    }

    @Override
    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
        TimePickerListener listener = (TimePickerListener) getActivity();
        listener.onTimeSelected(hourOfDay, minute);
    }
}
