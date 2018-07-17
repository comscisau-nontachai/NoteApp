package nontachai.becomedev.noteapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.tuyenmonkey.mkloader.MKLoader;

import org.w3c.dom.Text;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout inputEmail, inputPass;
    Button btnLogin;
    private CheckBox checkBox;

    private FirebaseAuth firebaseAuth;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.input_log_email);
        inputPass = findViewById(R.id.input_log_password);
        btnLogin = findViewById(R.id.btn_log);
        checkBox = findViewById(R.id.checkbox);

        firebaseAuth = FirebaseAuth.getInstance();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_save);

        ///load data from SharedPreferences....
        SharedPreferences sharedPref = getSharedPreferences("setting", MODE_PRIVATE);
        String mEmail = sharedPref.getString("email", null);
        String mPass = sharedPref.getString("password", null);
        inputEmail.getEditText().setText(mEmail);
        inputPass.getEditText().setText(mPass);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String lemail = inputEmail.getEditText().getText().toString().trim();
                String lpass = inputPass.getEditText().getText().toString().trim();


                if (lemail.isEmpty()) {
                    inputEmail.getEditText().setError("Enter email!!");
                } else if (lpass.isEmpty()) {
                    inputPass.getEditText().setError("Enter password!!");
                } else {
                    logIn(lemail, lpass);
                }

                if (checkBox.isChecked()) {
                    SharedPreferences sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("email", lemail);
                    editor.putString("password", lpass);
                    editor.commit();

                }
            }
        });

        ///admob
        MobileAds.initialize(this, "ca-app-pub-1787292132881960~8520030186");//app id
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-1787292132881960/5723735633");//unit id  ca-app-pub-3940256099942544/6300978111

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        ////end admob





    }

    private void logIn(String lemail, String lpass) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in , please wait....");
        progressDialog.show();


        firebaseAuth.signInWithEmailAndPassword(lemail, lpass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                    Toasty.success(getApplicationContext(), "Sign In Success!", Toast.LENGTH_SHORT, true).show();
                } else {
                    Toasty.error(getApplicationContext(), "ERROR:" + task.getException().getMessage(), Toast.LENGTH_SHORT, true).show();
                    Log.d("LoginActivity", task.getException().getMessage());
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
