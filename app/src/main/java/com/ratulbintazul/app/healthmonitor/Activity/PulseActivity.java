package com.ratulbintazul.app.healthmonitor.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ratulbintazul.app.healthmonitor.R;

import net.kibotu.heartrateometer.HeartRateOmeter;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class PulseActivity extends AppCompatActivity implements Function1<Boolean, Unit> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse);

        //HeartRateOmeter.Bpm;
        //HeartRateOmeter.Bpm.class

        //HeartRateOmeter heartRateOmeter = new HeartRateOmeter().withAverageAfterSeconds(3).setFingerDetectionListener(this).bpmUpdates(getApplicationContext(),preview).subscribe();

    }

    @Override
    public Unit invoke(Boolean aBoolean) {
        return null;
    }
}
