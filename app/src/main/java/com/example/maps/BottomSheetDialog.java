package com.example.maps;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import afu.org.checkerframework.checker.nullness.qual.Nullable;

public class BottomSheetDialog extends BottomSheetDialogFragment {
    View rate;
    View about;
    View setting;
    String file = "myfile";
    String fileContent;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet,container,false);
        addControl(v);
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("market://details?id=" + getContext().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getContext().getPackageName())));
                }

            }
        });
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final  AlertDialog.Builder aboutDialog = new AlertDialog.Builder(getContext());

                final View aboutView = getLayoutInflater().inflate(R.layout.about,null);
                Button ok = aboutView.findViewById(R.id.okbtn);
                final AlertDialog dialog = aboutDialog.create();
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
//                        getDialog().dismiss();

                    }
                });


                dialog.setView(aboutView);
                dialog.show();
            }
        });
//        setting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final  AlertDialog.Builder settingDialog = new AlertDialog.Builder(getContext());
//
//                final View settingView = getLayoutInflater().inflate(R.layout.setting,null);
//                final Button ok = settingView.findViewById(R.id.okbtn);
//                final Switch ringtone = settingView.findViewById(R.id.ringtone);
//                final Switch ring = settingView.findViewById(R.id.ring);
//                final AlertDialog dialog = settingDialog.create();
//                ok.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        boolean isRingtone = ringtone.isChecked();
//                        boolean isRing = ring.isChecked();
//                        String isR;
//                        String isRt;
//                        if(isRingtone==true) isRt = "1";
//                        else isRt = "0";
//                        if(isRingtone==true) isR = "1";
//                        else isR = "0";
//
//                        fileContent = isRt + "\n" + isR;
//                        try {
//                            FileOutputStream fos = getContext().openFileOutput(file,MODE_PRIVATE);
//                            fos.write(fileContent.getBytes());
//                            fos.close();
//                            File fileDir = new File(getContext().getFilesDir(),file);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        dialog.dismiss();
//                    }
//                });
//                dialog.setView(settingView);
//                dialog.show();
//            }
//        });
        return v;

    }
    private void addControl(View v){
        rate = v.findViewById(R.id.rate);
        about = v.findViewById(R.id.about);
        setting = v.findViewById(R.id.setting);
    }
}
