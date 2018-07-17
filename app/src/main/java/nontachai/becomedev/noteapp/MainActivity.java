package nontachai.becomedev.noteapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.renderscript.Script;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private GridLayoutManager gridLayoutManager;
    private DatabaseReference fNoteDatabase;

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;

    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);


//        gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

//        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));

        fAuth = FirebaseAuth.getInstance();
        if (fAuth.getCurrentUser() != null) {
            fNoteDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(fAuth.getCurrentUser().getUid());   //fAuth.getCurrentUser().getUid()
        }

        fetch();


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

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void fetch() {

//        Query query = FirebaseDatabase.getInstance()
//                .getReference()
//                .child("Notes").child("vENReL7xcobRXnn6uihqXj89vfy1");//vENReL7xcobRXnn6uihqXj89vfy1 fAuth.getCurrentUser().getUid()

        Query query = fNoteDatabase.orderByChild("timestamp");


        FirebaseRecyclerOptions<Model> options =
                new FirebaseRecyclerOptions.Builder<Model>()
                        .setQuery(query, new SnapshotParser<Model>() {
                            @NonNull
                            @Override
                            public Model parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new Model(snapshot.child("content").getValue().toString(),
                                        snapshot.child("title").getValue().toString(),
                                        snapshot.child("timestamp").getValue().toString(),
                                        snapshot.child("date").getValue().toString()
                                );
                            }

                        })
                        .build();

        adapter = new FirebaseRecyclerAdapter<Model, ViewHolder>(options) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_note_layout, parent, false);

                return new ViewHolder(view);
            }


            @Override
            protected void onBindViewHolder(ViewHolder holder, final int position, Model model) {

                final String noteId = getRef(position).getKey();

                holder.setTxtTitle(model.getmTitle());
                holder.setTxtDate(model.getmDate());

                GetTimeAgo getTimeAgo = new GetTimeAgo();
                holder.setTxtContent(getTimeAgo.getTimeAgo(Long.parseLong(model.getmDesc()), getApplicationContext()));

                holder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(MainActivity.this, NewNoteActivity.class);
                        intent.putExtra("noteID", noteId);
                        startActivity(intent);
                    }
                });
            }


        };
        recyclerView.setAdapter(adapter);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout root;
        public TextView txtTitle;
        public TextView txtTime;
        public TextView txtDate;

        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.list_root);
            txtTitle = itemView.findViewById(R.id.note_title);
            txtTime = itemView.findViewById(R.id.note_time);
            txtDate = itemView.findViewById(R.id.note_date);
        }


        public void setTxtTitle(String string) {
            txtTitle.setText(string);
        }

        public void setTxtContent(String string) {
            txtTime.setText(string);
        }

        public void setTxtDate(String string) {
            txtDate.setText(string);
        }
    }

    private void updateUI() {

//        if (fAuth.getCurrentUser() != null) {
//        } else {
//            Intent intent = new Intent(MainActivity.this, StartActivity.class);
//            startActivity(intent);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.main_new_note_btn:
                Intent intent = new Intent(MainActivity.this, NewNoteActivity.class);
                startActivity(intent);
        }

        return true;
    }
}
