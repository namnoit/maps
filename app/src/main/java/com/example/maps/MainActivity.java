package com.example.maps;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private com.getbase.floatingactionbutton.FloatingActionButton fab_distance, fab_alarm;
    FloatingActionsMenu fab_menu;
    private FloatingActionButton loc;
    private Button btnSrc, btnDst;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addControls();
        addEvents();
    }

    private void addEvents() {
        fab_distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Nothing here", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                showDistanceDialog();
            }
        });
        fab_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Add alarm", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });


    }

    private void openSearch() {
        Intent t = new Intent(this, SearchActivity.class);
        startActivity(t);
    }

    private void showDistanceDialog() {
        final AlertDialog.Builder distanceDialog = new AlertDialog.Builder(MainActivity.this);
        distanceDialog.setTitle("Chọn 2 điểm");
        LayoutInflater inflater = this.getLayoutInflater();
        View distanceDialogView = inflater.inflate(R.layout.distance,null);
        btnSrc = distanceDialogView.findViewById(R.id.btnSrc);
        btnDst = distanceDialogView.findViewById(R.id.btnDst);

        btnSrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearch();
            }
        });

        btnDst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearch();
            }
        });

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // User clicked the Yes button
                        dialog.dismiss();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };
        distanceDialog.setPositiveButton("OK", dialogClickListener);
        distanceDialog.setNegativeButton("Cancel", dialogClickListener);

        distanceDialog.setView(distanceDialogView);
        distanceDialog.show();
    }

    private void addControls() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        fab_alarm = findViewById(R.id.fab_alarm);
        fab_distance = findViewById(R.id.fab_distance);
        loc = findViewById(R.id.loc);
        fab_menu = findViewById(R.id.add_menu);
        fab_menu.bringToFront();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_menu) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
