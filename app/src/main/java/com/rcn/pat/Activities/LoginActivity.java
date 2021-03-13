package com.rcn.pat.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.rcn.pat.BuildConfig;
import com.rcn.pat.Global.GlobalClass;
import com.rcn.pat.R;
import com.rcn.pat.Repository.LoginRepository;
import com.rcn.pat.ViewModels.LoginViewModel;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.conn.ConnectTimeoutException;
import cz.msebera.android.httpclient.entity.StringEntity;

public class LoginActivity extends AppCompatActivity {
    private Button btnLogin;
    private LoginViewModel data;
    private String deviceToken;
    private ProgressDialog dialogo;
    private StringEntity entity;
    private boolean isError;
    private LoginRepository loginRepository;
    private EditText txtPws;
    private EditText txtUserName;
    private TextView txtVersion;

    @RequiresApi(api = VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        InitializaControls();

        InitializaEvents();

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        getCurrentDeviceToken();

        existLoginAutenticated();

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    private void InitializaControls() {
        txtUserName = findViewById(R.id.txtUserName);
        txtVersion = findViewById(R.id.txtVersion);
        txtPws = findViewById(R.id.txtPws);
        btnLogin = findViewById(R.id.btnLogin);

        loginRepository = new LoginRepository(getApplicationContext());
        
        if (BuildConfig.DEBUG) {
            txtUserName.setText("frodriguezp");
            txtPws.setText("bogota1*");
        } else {
            txtUserName.setText("");
            txtPws.setText("");
        }
        txtVersion.setText(BuildConfig.VERSION_NAME);
    }

    private void InitializaEvents() {

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                validaUsertInfo();
            }
        });

    }

    private String getCurrentDeviceToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {

                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        deviceToken = task.getResult().getToken();
                        Log.d(TAG, deviceToken);
                        // Log and toast
                        @SuppressLint({"StringFormatInvalid", "LocalSuppress"}) String msg = getString(R.string.msg_token_fmt, deviceToken);
                        Log.d(TAG, msg);
                        //Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
        return "";
    }

    @RequiresApi(api = VERSION_CODES.KITKAT)
    private void existLoginAutenticated() {

        LoginViewModel loginViewModel = loginRepository.getLoginByUserName();

        if (loginViewModel != null) {
            updateLogin(loginViewModel);
            data = loginViewModel;
            Intent intent = null;
            intent = new Intent(LoginActivity.this, ListDriverServicesActivity.class);
            GlobalClass.getInstance().setDocNumber(data.getNumero_Documento());
            startActivity(intent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void validaUsertInfo() {

        txtPws.setError(null);
        txtUserName.setError(null);
        String email = txtPws.getText().toString();
        String password = txtUserName.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            txtPws.setError(getString(R.string.error_password_empty));
            focusView = txtPws;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            txtUserName.setError(getString(R.string.error_user_empty));
            focusView = txtUserName;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            asyncLogin();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void asyncLogin() {

        dialogo = new ProgressDialog(LoginActivity.this);
        dialogo.setMessage("Validando usuario...");
        dialogo.setIndeterminate(false);
        dialogo.setCancelable(false);
        dialogo.show();

        final String username = txtUserName.getText().toString();
        String pws = txtPws.getText().toString();
        String url = GlobalClass.getInstance().getUrlServices() + "login?UserName=" + username + "&Password=" + pws + "&Token=" + deviceToken + "&Plataforma=android";

        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(60000);
        String tipo = "application/json";
        RequestParams params = new RequestParams();
        entity = new StringEntity("", StandardCharsets.UTF_8);
        client.post(LoginActivity.this, url, entity, tipo, new TextHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
                isError = true;
                Log.e("DB", String.valueOf(error instanceof ConnectTimeoutException));

                if (statusCode == 0)
                    showMessage(error.getMessage());
                else
                    showMessage(responseBody);
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                TypeToken<LoginViewModel> token = new TypeToken<LoginViewModel>() {
                };
                Gson gson = new GsonBuilder().create();
                data = gson.fromJson(responseString, token.getType());
                GlobalClass.getInstance().setDocNumber(data.getNumero_Documento());
                isError = false;
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onFinish() {
                super.onFinish();
                if (!isError) {
                    dialogo.hide();

                    if (!data.isAuthenticated()) {
                        showMessage(getString(R.string.autenticationError));
                        return;
                    }
                    if (!data.isAuthorized()) {
                        showMessage(getString(R.string.AutorizationError));
                        return;
                    }

                    Intent intent = null;
                    intent = new Intent(LoginActivity.this, ListDriverServicesActivity.class);

                    intent.putExtra("token", deviceToken);
                    intent.putExtra("Plataforma", "android");
                    startActivity(intent);
                    insertLogin(data);
                } else
                    dialogo.hide();

            }
        });
    }

    private void updateLogin(LoginViewModel loginViewModel) {
        loginViewModel.setLastName(getCurrentDate());
        loginRepository.updateLogin(loginViewModel);
    }

    private void showMessage(String res) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(LoginActivity.this);

        dlgAlert.setMessage(res);
        dlgAlert.setTitle(getString(R.string.app_name));
        //dlgAlert.setPositiveButton(getString(R.string.Texto_Boton_Ok), null);
        dlgAlert.setPositiveButton(R.string.Texto_Boton_Ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // if this button is clicked, close
                // current activity

            }
        });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private void insertLogin(LoginViewModel data) {
        String currentDate = getCurrentDate();
        data.setLastLoginDate(currentDate);
        loginRepository.insert(data);
    }

    private String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }

    private static final String TAG = "LoginActivity";


}
