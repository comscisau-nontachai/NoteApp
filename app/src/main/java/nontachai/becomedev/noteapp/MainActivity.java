package nontachai.becomedev.noteapp;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private DatabaseReference fNoteDatabase;

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddNote;
    private FirebaseRecyclerAdapter adapter;

    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        fAuth = FirebaseAuth.getInstance();
        if (fAuth.getCurrentUser() != null) {
            fNoteDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(fAuth.getCurrentUser().getUid());   //fAuth.getCurrentUser().getUid()
        }

        //set toolbar
        mToolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(fAuth.getCurrentUser().getEmail());

        fetch();

        fabAddNote = findViewById(R.id.fab_add_note);
        fabAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewNoteActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_rate_app:{
                try{
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+getPackageName())));
                }catch (ActivityNotFoundException ignored){
                    Toast.makeText(this, "can't find app", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://play.google.com/store/apps/details?id="+getPackageName())));
                }
            }
                break;
            case R.id.nav_policy: {
                showPolicyApp();
            }
            break;
            case R.id.nav_exit: {
                fAuth.signOut();
                finish();
            }
            break;
        }
        return true;
    }

    private void fetch() {

        final SweetAlertDialog dialogLoading = new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE);
        dialogLoading.setContentText("Loading...");
        dialogLoading.setCancelable(false);
        dialogLoading.show();

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
                        .inflate(R.layout.item_note, parent, false);

                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(ViewHolder holder, final int position, Model model) {

                final String noteId = getRef(position).getKey();

                holder.txtTitle.setText(model.getmTitle());
                holder.txtDate.setText(convertDateFormat(model.getmDate()));

                holder.txtTimeAgo.setText(new GetTimeAgo().getTimeAgo(Long.parseLong(model.getmDesc()), getApplicationContext()));

                holder.linear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(MainActivity.this, NewNoteActivity.class);
                        intent.putExtra("noteID", noteId);
                        startActivity(intent);
                    }
                });
                dialogLoading.dismissWithAnimation();
            }
        };
        recyclerView.setAdapter(adapter);
        //dialogLoading.dismissWithAnimation();

    }

    private String convertDateFormat(String date) {
        SimpleDateFormat curFormat = new SimpleDateFormat("EEE dd-MM-yyyy");

        Date dateObj = null;
        try {
            dateObj = curFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat newFormat = new SimpleDateFormat("dd MMMM yyyy");
        String new_date = newFormat.format(dateObj);

        return new_date;
    }

    private void showPolicyApp() {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Privacy Policy");

        WebView webView = new WebView(this);
        webView.loadUrl("https://comscisau-nontachai.github.io/NoteApp/privacy_policy.html");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        alert.setView(webView);
        alert.setNegativeButton("close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linear;
        TextView txtTitle;
        TextView txtTimeAgo;
        TextView txtDate;
        TextView txtDay;


        ViewHolder(View itemView) {
            super(itemView);
            linear = itemView.findViewById(R.id.linear_layout);
            txtTitle = itemView.findViewById(R.id.txt_note_title);
            txtTimeAgo = itemView.findViewById(R.id.txt_note_time_ago);
            txtDate = itemView.findViewById(R.id.txt_note_date);
            //txtDay = itemView.findViewById(R.id.txt_note_day);
        }
    }
}
