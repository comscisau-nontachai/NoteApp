package nontachai.becomedev.noteapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;

public class StartActivity extends AppCompatActivity {

    private Button btnReg,btnLog;

    private FirebaseAuth fAuth;

    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btnReg = findViewById(R.id.btn_register);
        btnLog = findViewById(R.id.btn_login);

        fAuth = FirebaseAuth.getInstance();

//        updateUI();


        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                login();
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

    private void login() {
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
    }

    private void register() {
        Intent intent = new Intent(getApplicationContext(),RegisterActivity.class);
        startActivity(intent);

    }
    private void updateUI()
    {
        if(fAuth.getCurrentUser() != null)
        {

            Log.d("MainActivity", "!= null start");
        }else
        {
//            Intent intent = new Intent(StartActivity.this,MainActivity.class);
//            startActivity(intent);
//            finish();
            Log.d("MainActivity", "== null start");
        }
    }
}
