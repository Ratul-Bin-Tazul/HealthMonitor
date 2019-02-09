package com.ratulbintazul.app.healthmonitor.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ratulbintazul.app.healthmonitor.Activity.SignupActivity;
import com.ratulbintazul.app.healthmonitor.ApiClient.ApiClient;
import com.ratulbintazul.app.healthmonitor.ApiInterface.ApiInterface;
import com.ratulbintazul.app.healthmonitor.DataModel.LoginResponse;
import com.ratulbintazul.app.healthmonitor.DataModel.UserData;
import com.ratulbintazul.app.healthmonitor.R;

import androidx.work.Worker;
import androidx.work.WorkerParameters;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StepCounter extends Worker implements SensorEventListener {
    private Gson gson  = new Gson();

    //private SensorManager sensorManager;

    //private static SensorEventListener sensorEventListener;

    public StepCounter(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);

        //sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public Result doWork() {

        // Do the work here--in this case, compress the stored images.
        // In this example no parameters are passed; the task is
        // assumed to be "compress the whole library."
        //myCompress();

        Log.e("user"," sync entered");

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getApplicationContext().getString(R.string.step_count_pref), Context.MODE_PRIVATE);

        int steps = sharedPref.getInt(getApplicationContext().getString(R.string.step_count),0);

//                SharedPreferences.Editor editor = sharedPref.edit();
//                editor.putInt(getApplicationContext().getString(R.string.step_count), (int) event.values[0]);
//                editor.apply();

        SharedPreferences userPref = getApplicationContext().getSharedPreferences(
                getApplicationContext().getString(R.string.user_data_pref), Context.MODE_PRIVATE);

        String userString = userPref.getString(getApplicationContext().getString(R.string.user_data), "");

        try {
            UserData userData = gson.fromJson(userString,UserData.class);

            Log.e("user",userString);
            sync(""+steps,userData.getID(),""+(steps * Double.parseDouble(userData.getHeight()) * 0.425),userData.getHash());
        }catch (Exception e) {

            Log.e("user error",e.toString());
        }


//        SensorEventListener sensorEventListener = new SensorEventListener() {
//            @Override
//            public void onSensorChanged(SensorEvent sensorEvent) {
//                Log.e("sensor",String.valueOf(sensorEvent.values[0]));
//
//                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
//                        getApplicationContext().getString(R.string.step_count_pref), Context.MODE_PRIVATE);
//
//                SharedPreferences.Editor editor = sharedPref.edit();
//                editor.putInt(getApplicationContext().getString(R.string.step_count), 0);
//                editor.apply();
//
//                //Toast.makeText(getApplicationContext(), String.valueOf(sensorEvent.values[0]), Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onAccuracyChanged(Sensor sensor, int i) {
//
//            }
//        };
//
//        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
//        if (countSensor != null) {
//            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
//        } else {
//            Log.e("no sensor","Count sensor not available!");
////            Toast.makeText(getApplicationContext(), "Count sensor not available!", Toast.LENGTH_LONG).show();
//        }


        // Indicate success or failure with your return value:
        return Result.success();

        // (Returning Result.retry() tells WorkManager to try this task again
        // later; Result.failure() says not to try again.)
    }

    private void sync(String id, final String step, String distance, String hash) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Call<ResponseBody> venues = apiService.sync(id,step,distance,hash);
        venues.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                //LoginResponse loginResponse = response.body();

                if (response.code()==200)
                    Log.e("res",response.body().toString());
                else
                    Log.e("res",response.raw().toString());





            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

//                if (progressDialog.isShowing())
//                    progressDialog.dismiss();

                //Toast.makeText(SignupActivity.this," signup "+t.getLocalizedMessage(),Toast.LENGTH_SHORT).show();

                Log.e("res",t.getMessage());

            }

        });

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getApplicationContext().getString(R.string.step_count_pref), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getApplicationContext().getString(R.string.step_count), (int) sensorEvent.values[0]);
        editor.apply();


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}