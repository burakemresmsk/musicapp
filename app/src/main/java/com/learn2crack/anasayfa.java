package com.learn2crack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by burakemresmsk on 22/03/2017.
 */

public class anasayfa extends AppCompatActivity {

    private Button mBtprofil;

    private Button button_create;
    private Button button_join;

    public String groupname;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_anasayfa);
        mBtprofil = (Button) findViewById(R.id.profil_button);

        mBtprofil.setOnClickListener(view -> showProfile());


        button_create = (Button) findViewById(R.id.button_create);


        button_join = (Button) findViewById(R.id.button_join_room);



        button_create.setOnClickListener(view -> createGroup());

        button_join.setOnClickListener(view -> joinGroup());

    }

    private void showProfile(){

        Intent intent = new Intent(anasayfa.this,ProfileActivity.class);
        startActivity(intent);
    }




    private void  createGroup(){

        final EditText edittext = new EditText(getApplicationContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(anasayfa.this);

        builder.setTitle("Create Group");
        builder.setMessage("Group adı giriniz!!!");
        builder.setView(edittext);
        edittext.setTextColor(Color.RED);
        builder.setNegativeButton("İPTAL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                //İptal butonuna basılınca yapılacaklar.Sadece kapanması isteniyorsa boş bırakılacak

            }
        });


        builder.setPositiveButton("TAMAM", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Tamam butonuna basılınca yapılacaklar

                Editable YouEditTextValue = edittext.getText();
               // Toast.makeText(getApplicationContext(), "Name:" + YouEditTextValue, Toast.LENGTH_SHORT).show();
                groupname = YouEditTextValue.toString();

               Intent intent = new Intent(anasayfa.this,CreateGroup.class);
                intent.putExtra("grup_name",groupname);

                startActivity(intent);


               // showSnackBarMessage("Maalesef daha önce öyle bir grup oluşturulmadı. Lütfen olan bir grup ismi giriniz.");


            }
        });

        builder.show();




    }

    private void showSnackBarMessage(String message) {

        Snackbar.make(findViewById(R.id.activity_anasayfa),message,Snackbar.LENGTH_SHORT).show();

    }


    private void  joinGroup(){

        final EditText edittext = new EditText(getApplicationContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(anasayfa.this);

        builder.setTitle("Join Group");
        builder.setMessage("Grubun adı giriniz!!!");
        builder.setView(edittext);
        edittext.setTextColor(Color.RED);
        builder.setNegativeButton("İPTAL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                //İptal butonuna basılınca yapılacaklar.Sadece kapanması isteniyorsa boş bırakılacak

            }
        });


        builder.setPositiveButton("TAMAM", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Tamam butonuna basılınca yapılacaklar

                Editable YouEditTextValue = edittext.getText();
               // Toast.makeText(getApplicationContext(), "Name:" + YouEditTextValue, Toast.LENGTH_SHORT).show();
                groupname = YouEditTextValue.toString();

                Intent intent = new Intent(anasayfa.this,Joinmusic.class);
                intent.putExtra("grup_name",groupname);
                startActivity(intent);


            }
        });

        builder.show();




    }



}
