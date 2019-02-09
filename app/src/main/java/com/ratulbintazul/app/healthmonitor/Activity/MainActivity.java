package com.ratulbintazul.app.healthmonitor.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.ratulbintazul.app.healthmonitor.DataModel.UserData;
import com.ratulbintazul.app.healthmonitor.R;
import com.ratulbintazul.app.healthmonitor.Utils.RoundCornersDrawable;
import com.ratulbintazul.app.healthmonitor.Utils.StepCounter;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 9;
    private SensorManager sensorManager;
    private TextView count,distance,time,cal;
    boolean activityRunning;

    double height = 0;

    double weight = 0;

    double weightConstant = 2.20462;
    double caloryConstant = 0.57;
    double timeConstant = 0.00885;

    double inchToMeter = 0.0254;

    Gson gson = new Gson();

    TextView age,blood,heightText,weightText,name;

    TextView distanceUnit;

    Button measurePulse;

    Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        count = (TextView) findViewById(R.id.mainStepCount);

        distance = (TextView) findViewById(R.id.mainDistanceCount);
        time = (TextView) findViewById(R.id.mainTimeCount);
        cal = (TextView) findViewById(R.id.mainCalCount);

        age = (TextView) findViewById(R.id.maiAge);
        blood = (TextView) findViewById(R.id.mainBlood);
        heightText = (TextView) findViewById(R.id.mainHeight);
        weightText = (TextView) findViewById(R.id.mainWeight);
        name = (TextView) findViewById(R.id.mainName);

        distanceUnit = (TextView) findViewById(R.id.mainDistanceUnit);

        measurePulse = findViewById(R.id.pulseMeasure);

        logout = findViewById(R.id.mainLogout);

        measurePulse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(MainActivity.this,lib.class));
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getSharedPreferences(
                        getString(R.string.user_data_pref), Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.user_data), "");
                editor.apply();

                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                finish();
            }
        });

        SharedPreferences userPref = getSharedPreferences(
                getString(R.string.user_data_pref), Context.MODE_PRIVATE);

        String userString = userPref.getString(getString(R.string.user_data), "");

        try {
            UserData userData = gson.fromJson(userString,UserData.class);
            height = Double.parseDouble(userData.getHeight()) * inchToMeter;

            weight = Double.parseDouble(userData.getWeight());

            name.setText(userData.getName());

            age.setText("Age: "+userData.getAge());
            blood.setText("Blood: "+userData.getBlood());
            heightText.setText("Height: "+userData.getHeight() +" m");
            weightText.setText("Weight: "+userData.getWeight()+" kg");

        }catch (Exception e) {

            Log.e("user error",e.toString());
        }

        ImageView imageView = (ImageView) findViewById(R.id.cardBg);
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.card_bg);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            //Default
            imageView.setBackgroundResource(R.drawable.card_bg);
        } else {
            //RoundCorners

            CardView cardView = (CardView) findViewById(R.id.profileCard);
            cardView.setPreventCornerOverlap(false); //it is very important

//            RoundCornersDrawable round = new RoundCornersDrawable(mBitmap,
//                    getResources().getDimension(R.dimen.cardview_default_radius), 0); //or your custom radius

            RoundCornersDrawable round = new RoundCornersDrawable(mBitmap,
                    cardView.getRadius(), 0); //or your custom radius

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                imageView.setBackground(round);
            else
                imageView.setBackgroundDrawable(round);
        }

        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            accessGoogleFit();
        }

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getApplicationContext().getString(R.string.step_count_pref), Context.MODE_PRIVATE);

        int defaultValue = 0;
        int step = sharedPref.getInt(getString(R.string.step_count), defaultValue);
        //Toast.makeText(this,"step "+step,Toast.LENGTH_SHORT).show();

        //TODO DELETE
        //step = 1000;

        double timeMinutes = step * timeConstant;
        double caloriesBurnt = weight * weightConstant * caloryConstant;
        double distanceMeters = (double)step * height * 0.425;

        double stepDouble = step;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        if (step>1000) {


            Log.e("stepDouble"," "+stepDouble);


            double twoDigitsF ;

            Log.e("decimalFormat"," "+decimalFormat.format(stepDouble)+"K");

            String stepString = decimalFormat.format(stepDouble);
            count.setText(stepString);

        }else {
            count.setText(""+(int)step);
        }

        if (distanceMeters > 1000) {
            distance.setText(""+ decimalFormat.format(distanceMeters/1000.0) );
            distanceUnit.setText("KM");
        }else {
            distance.setText(""+ decimalFormat.format(distanceMeters) );
        }


        time.setText(""+ decimalFormat.format(timeMinutes));
        cal.setText(""+ decimalFormat.format(caloriesBurnt));



        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Create a Constraints object that defines when the task should run
        Constraints netConstraint = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                //.setRequiresCharging(true)
                // Many other constraints are available, see the
                // Constraints.Builder reference
                .build();

        PeriodicWorkRequest.Builder photoCheckBuilder =
                new PeriodicWorkRequest
                        .Builder(StepCounter.class, 1,
                        TimeUnit.SECONDS)
                .setConstraints(netConstraint);
// ...if you want, you can apply constraints to the builder here...

// Create the actual work object:
        PeriodicWorkRequest photoCheckWork = photoCheckBuilder.build();
// Then enqueue the recurring task:
        WorkManager.getInstance().enqueue(photoCheckWork);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                accessGoogleFit();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityRunning = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;
        // if you unregister the last listener, the hardware will stop detecting step events
//        sensorManager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout:
                SharedPreferences sharedPref = getSharedPreferences(
                        getString(R.string.user_data_pref), Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.user_data), "");
                editor.apply();

                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (activityRunning) {

            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                    getApplicationContext().getString(R.string.step_count_pref), Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getApplicationContext().getString(R.string.step_count), (int) event.values[0]);
            editor.apply();

            float step = event.values[0];
            Log.e("step"," " +step);

            if (step>1000) {
                double stepDouble = step;

                Log.e("stepDouble"," "+stepDouble);

                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                double twoDigitsF ;

                Log.e("decimalFormat"," "+decimalFormat.format(stepDouble));

                String stepString = decimalFormat.format(stepDouble);
                count.setText(stepString);
            }else {
                count.setText(""+(int)step);
            }

            //count.setText(String.valueOf(event.values[0]));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void accessGoogleFit() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.YEAR, -1);
        long startTime = cal.getTimeInMillis();


        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();


        GoogleSignInOptionsExtension fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .build();

        GoogleSignInAccount gsa = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        OnDataPointListener mListener =
                new OnDataPointListener() {
                    @Override
                    public void onDataPoint(DataPoint dataPoint) {
                        for (Field field : dataPoint.getDataType().getFields()) {
                            Value val = dataPoint.getValue(field);
                            Log.i("FIT", "Detected DataPoint field: " + field.getName());
                            Log.i("FIT", "Detected DataPoint value: " + val);
                            Toast.makeText(MainActivity.this,"Detected DataPoint value: " + val,Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        Task<Void> response = Fitness.getSensorsClient(this, gsa)
                .add(new SensorRequest.Builder()
                                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                .setSamplingRate(1, TimeUnit.MINUTES)  // sample once per minute
                                .build(),mListener
                        )
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i("FIT", "Listener registered!");
                        } else {
                            Log.e("FIT", "Listener not registered.", task.getException());
                        }
                    }
                });


        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        Log.d("FIT", "onSuccess()");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FIT", "onFailure()", e);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<DataReadResponse> task) {
                        Log.d("FIT", "onComplete()");
                    }
                });
    }
}
