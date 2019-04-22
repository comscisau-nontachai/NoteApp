package nontachai.becomedev.noteapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class NewNoteActivity extends AppCompatActivity {

    private EditText edt_title, edt_content;
    private Toolbar mToolbar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference fNoteDatabase;

    private Menu mainMenu;
    private String noteID = "";

    private boolean isExist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        try {
            noteID = getIntent().getStringExtra("noteID");

            if (!noteID.trim().equals("")) {
                isExist = true;
            } else {
                isExist = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        edt_title = findViewById(R.id.new_note_title);
        edt_content = findViewById(R.id.new_note_content);
        mToolbar = findViewById(R.id.new_note_toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        fNoteDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(firebaseAuth.getCurrentUser().getUid());

        if(isExist){
            //edit
            getSupportActionBar().setTitle("Edit Note");
        }else{
            //new
            getSupportActionBar().setTitle("New Note");
        }

        putData();
    }

    private void putData() {
        if (isExist) {
            fNoteDatabase.child(noteID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("content")) {
                        String title = dataSnapshot.child("title").getValue().toString();
                        String content = dataSnapshot.child("content").getValue().toString();

                        edt_title.setText(title);
                        edt_content.setText(content);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {
        }

    }

    private void createNote(String title, String content) {
        if (firebaseAuth.getCurrentUser() != null) {

            ///date
            SimpleDateFormat sdf = new SimpleDateFormat("EEE dd-MM-yyyy");
            final String currentDateandTime = sdf.format(new Date());
            ///////////

            if (isExist) {
                //Update note

                Map updateMap = new HashMap();
                updateMap.put("title", edt_title.getText().toString().trim());
                updateMap.put("content", edt_content.getText().toString().trim());
                updateMap.put("timestamp", ServerValue.TIMESTAMP);
                updateMap.put("date", currentDateandTime);

                fNoteDatabase.child(noteID).updateChildren(updateMap);

                Toasty.success(this, "Note Updated...", Toast.LENGTH_SHORT, true).show();
                finish();

            } else {
                //Create a new note


                final DatabaseReference newNoteRef = fNoteDatabase.push();

                final Map noteMap = new HashMap();
                noteMap.put("title", title);
                noteMap.put("content", content);
                noteMap.put("timestamp", ServerValue.TIMESTAMP);
                noteMap.put("date", currentDateandTime);

                Thread mainThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        newNoteRef.setValue(noteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    Toasty.success(NewNoteActivity.this, "Note Added...", Toast.LENGTH_SHORT, true).show();
                                    finish();

                                } else {
                                    Toast.makeText(NewNoteActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });
                mainThread.start();
            }


        } else {
            Toasty.error(this, "Singed In first!!.", Toast.LENGTH_SHORT, true).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.new_note_menu, menu);
        mainMenu = menu;


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.new_note_del_btn:
                if (isExist) {

                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(NewNoteActivity.this);
                    builder.setMessage("Delete this note ?");
                    builder.setIcon(android.R.drawable.stat_sys_warning);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteNote();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();


                } else {
                    Toasty.warning(this, "Nothing to delete.", Toast.LENGTH_SHORT, true).show();
                }
                break;
            case R.id.ic_save:
                String title = edt_title.getText().toString().trim();
                String content = edt_content.getText().toString().trim();

                if(title.isEmpty())
                {
                    edt_title.setError("Enter Title!!");
                }else if(content.isEmpty())
                {
                    edt_content.setError("Enter Description");
                }else
                {
                    createNote(title, content);
                }
        }

        return true;
    }

    private void deleteNote() {
        fNoteDatabase.child(noteID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(NewNoteActivity.this, "Note Deleted...", Toast.LENGTH_SHORT, true).show();
                    noteID = "no";
                    finish();
                } else {
                    Toast.makeText(NewNoteActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
