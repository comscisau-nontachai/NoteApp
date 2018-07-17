package nontachai.becomedev.noteapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {

    private Button btn_reg;
    private TextInputLayout inName,inEmail,inPassword;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference fUserDatabase;

    private ProgressDialog progressDialog;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btn_reg = findViewById(R.id.btn_reg);
        inName = findViewById(R.id.input_reg_name);
        inEmail = findViewById(R.id.input_reg_email);
        inPassword = findViewById(R.id.input_reg_password);

        firebaseAuth = FirebaseAuth.getInstance();
        fUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_person_add);

        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uname = inName.getEditText().getText().toString().trim();
                String uemail = inEmail.getEditText().getText().toString().trim();
                String upass = inPassword.getEditText().getText().toString().trim();

                if(uname.isEmpty())
                {
                    inName.getEditText().setError("Enter your name !!");
                }else if(uemail.isEmpty())
                {
                    inEmail.getEditText().setError("Enter your email !!");
                }else if(upass.isEmpty())
                {
                    inPassword.getEditText().setError("Enter your Password !!");
                }else
                {
                    registerUser(uname,uemail,upass);
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
    private void registerUser(final String name, String email, String password)
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing your request , please wait...");
        progressDialog.show();


        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){


                            fUserDatabase.child(firebaseAuth.getCurrentUser().getUid())
                                    .child("basic").child("name").setValue(name)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                progressDialog.dismiss();

                                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                                startActivity(mainIntent);
                                                finish();
                                                Toasty.success(getApplicationContext(), "Create User Success!", Toast.LENGTH_SHORT, true).show();

                                            } else {
                                                progressDialog.dismiss();
                                                Toasty.error(getApplicationContext(), "ERROR:"+task.getException().getMessage(), Toast.LENGTH_SHORT, true).show();
                                            }

                                        }
                                    });

                        } else {

                            progressDialog.dismiss();
                            Toasty.error(getApplicationContext(), "ERROR:"+task.getException().getMessage(), Toast.LENGTH_SHORT, true).show();

                        }

                    }
                });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId())
        {
            case android.R.id.home : finish(); break;
        }
        return true;
    }
}
