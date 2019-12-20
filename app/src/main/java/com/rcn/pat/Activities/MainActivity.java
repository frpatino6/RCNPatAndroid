   package com.rcn.pat.Activities;

   import android.content.ComponentName;
   import android.content.Context;
   import android.content.Intent;
   import android.content.ServiceConnection;
   import android.content.pm.PackageManager;
   import android.net.Uri;
   import android.os.Bundle;
   import android.os.IBinder;
   import android.provider.Settings;
   import android.view.View;
   import android.widget.Button;
   import android.widget.TextView;

   import androidx.annotation.NonNull;
   import androidx.appcompat.app.AppCompatActivity;
   import androidx.core.app.ActivityCompat;
   import androidx.core.content.ContextCompat;

   import com.rcn.pat.Global.BackgroundLocationService;
   import com.rcn.pat.BuildConfig;
   import com.rcn.pat.R;

   import static android.Manifest.permission.ACCESS_FINE_LOCATION;

   public class MainActivity extends AppCompatActivity implements View.OnClickListener {

       private final int PERMISSION_REQUEST_CODE = 200;
       Button startButton;
       Button stopButton;
       TextView statusTextView;

       public BackgroundLocationService gpsService;
       public boolean mTracking = false;

       @Override
       protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           setContentView(R.layout.activity_main);

           //initialize views
           setWidgetIds();
           //prepare service
           final Intent intent = new Intent(this.getApplication(), BackgroundLocationService.class);
           this.getApplication().startService(intent);
           this.getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

       }

       private void setWidgetIds() {
           startButton = (Button) findViewById(R.id.startButton);
           stopButton = (Button) findViewById(R.id.stopButton);
           statusTextView = (TextView) findViewById(R.id.statusTextView);

           startButton.setOnClickListener(this);
           stopButton.setOnClickListener(this);
       }

       @Override
       public void onClick(View v) {
           switch (v.getId()) {
               case R.id.startButton:
                   startTracking();
                   break;
               case R.id.stopButton:
                   stopTracking();
                   break;
           }
       }

       public void startTracking() {
           //check for permission
           if (ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
               gpsService.startTracking();
               mTracking = true;
               toggleButtons();
           } else {
               ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
           }
       }

       @Override
       public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
           super.onRequestPermissionsResult(requestCode, permissions, grantResults);

           if (requestCode == PERMISSION_REQUEST_CODE) {
               if (grantResults.length > 0
                       && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   startTracking();
               }
           }
       }

       public void stopTracking() {
           mTracking = false;
           gpsService.stopTracking();
           toggleButtons();
       }

       private void toggleButtons() {
           startButton.setEnabled(!mTracking);
           stopButton.setEnabled(mTracking);
           statusTextView.setText((mTracking) ? "TRACKING" : "GPS Ready");
       }

       private void openSettings() {
           Intent intent = new Intent();
           intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
           Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
           intent.setData(uri);
           intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
           startActivity(intent);
       }


       private ServiceConnection serviceConnection = new ServiceConnection() {
           public void onServiceConnected(ComponentName className, IBinder service) {
               String name = className.getClassName();
               if (name.endsWith("BackgroundLocationService")) {
                   gpsService = ((BackgroundLocationService.LocationServiceBinder) service).getService();
                   startButton.setEnabled(true);
                   statusTextView.setText("GPS Ready");
               }
           }

           public void onServiceDisconnected(ComponentName className) {
               if (className.getClassName().equals("BackgroundLocationService")) {
                   gpsService = null;
               }
           }
       };
   }
