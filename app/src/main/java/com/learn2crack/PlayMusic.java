package com.learn2crack;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.learn2crack.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by burakemresmsk on 17/05/2017.
 */

public class PlayMusic  extends AppCompatActivity {

    MediaPlayer mPlayer;
    Button buttonPlay;
    Button buttonStop;
    Button buttonPause;


    Button button_backmusic;
    Button button_playmusic;
    Button button_forwardmusic;
    Button button_shuffle;
    Button button_loopmusic;


    Button button_speech;
    Button button_aktifuser;


    Button button_oylama;


    String url = Constants.url;
    String playingurl;
    String groupname;
    String situation="sirali";


    private  int uygunluk=0;


    private int length=0;
    private  int pozisyon=0;
    ListView playlst;
    private ArrayAdapter<String> listAdapter ;

    private final int REQ_CODE_SPEECH_INPUT = 100;



    List<String> arrList;
    int[] nextsong = new int[10];


    private Socket socket;
    {
        try{
            socket = IO.socket(Constants.BASE_URL_socketio);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playmusic);

            Bundle b = getIntent().getExtras();
            ArrayList<String> arr = (ArrayList<String>)b.getStringArrayList("array_list");

            Arrays.fill(nextsong,new Integer(0));

            arrList = new ArrayList<String>();


        String username=b.getString("username");
           groupname=b.getString("groupname");
          // System.out.println(arr);




        playlst=(ListView) findViewById(R.id.listview_playlist);


        listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow,R.id.rowTextView, arr);

        playlst.setAdapter( listAdapter );

        createMusicroom(groupname,username);

        playlst.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                pozisyon=position;
                String song=arr.get(position);
                playingurl=url+song.toString();
                playingurl=playingurl.replaceAll(" ", "%20");
                buttonPlay.callOnClick();


                button_playmusic.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_pause_black_24dp, 0, 0);

            }
        });


        button_backmusic = (Button) findViewById(R.id.button_backmusic);
        button_forwardmusic = (Button) findViewById(R.id.button_forwardmusic);
        button_playmusic = (Button) findViewById(R.id.button_playpause);
        button_shuffle = (Button) findViewById(R.id.button_shuffle);
        button_loopmusic = (Button) findViewById(R.id.button_loopmusic);
        button_speech = (Button) findViewById(R.id.button_speech);
        button_aktifuser = (Button) findViewById(R.id.button_aktifuser);


        button_speech.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
              //  showSnackBarMessage("Lütfen konuşun");

                promptSpeechInput();




            }
        });


        button_aktifuser.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
               // showSnackBarMessage("kullanıcı listesi");


                arrList.clear();

                socket.emit("aktifkullanici", "kimleraktif");


            }
        });



        button_oylama = (Button) findViewById(R.id.button_oylama);

        button_oylama.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub

                final CharSequence[] chars = arr.toArray(new CharSequence[arr.size()]);


                situation="oylama";

                if(uygunluk==0) {

                AlertDialog.Builder builder = new AlertDialog.Builder(PlayMusic.this);
                builder.setTitle("Sonraki şarkıyı oylayınız..");
                builder.setItems(chars, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // the user clicked on colors[which]

                        showSnackBarMessage(chars[which].toString());

                        nextsong[which]=nextsong[which]+1;

                        uygunluk=1;


                    }
                });
                builder.show();
                }else{
                    showSnackBarMessage("Maalesef sonraki şarkı için oyunuzu kullandınız.");
                }
            }
        });


        button_backmusic.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub

                pozisyon--;
                if(pozisyon<0){
                    pozisyon=arr.size()-1;
                }


                String song=arr.get(pozisyon);
                playingurl=url+song.toString();
                playingurl=playingurl.replaceAll(" ", "%20");
                buttonPlay.callOnClick();


                button_playmusic.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_pause_black_24dp, 0, 0);


            }
        });

        button_forwardmusic.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                pozisyon++;

               // Toast.makeText(getApplicationContext(),"poz:"+pozisyon,Toast.LENGTH_SHORT).show();

                if(pozisyon>=arr.size()){
                    pozisyon=0;
                }
                String song=arr.get(pozisyon);
                playingurl=url+song.toString();
                playingurl=playingurl.replaceAll(" ", "%20");
                buttonPlay.callOnClick();

                button_playmusic.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_pause_black_24dp, 0, 0);


            }
        });

        button_playmusic.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub


                if(mPlayer!=null && mPlayer.isPlaying()) {

                    mPlayer.pause();
                    length = mPlayer.getCurrentPosition();
                    // Toast.makeText(getApplicationContext(), "TEXT;"+buttonPause.getText().toString(), Toast.LENGTH_LONG).show();

                    JSONObject data = new JSONObject();  //diger uyelere bilgilendirme gonderilir.
                    try {

                        data.put("durum", "pause");
                        data.put("grupname",groupname);
                        data.put("seek", length);
                        data.put("song", playingurl);

                        //addMessage(sarki);

                    } catch (JSONException e) {
                        // return;
                    }

                    socket.emit("playpause", data);


                    button_playmusic.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_play_arrow_black_24dp, 0, 0);

                }else{
                    mPlayer.seekTo(length);
                    mPlayer.start();

                    JSONObject data = new JSONObject();  //diger uyelere bilgilendirme gonderilir.
                    try {

                        data.put("durum", "play");
                        data.put("grupname",groupname);
                        data.put("seek", length);
                        data.put("song", playingurl);

                        //addMessage(sarki);

                    } catch (JSONException e) {
                        // return;
                    }

                    socket.emit("playpause", data);


                    button_playmusic.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_pause_black_24dp, 0, 0);

                }



            }
        });
        button_shuffle.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                showSnackBarMessage("Shuffle moda geçildi");

                situation="shuffle";


            }
        });
        button_loopmusic.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                showSnackBarMessage("Sıralı moda geçildi");

                situation="sirali";

            }
        });



        buttonPlay = (Button) findViewById(R.id.button_play);
        buttonPlay.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if(mPlayer!=null && mPlayer.isPlaying()){
                    mPlayer.stop();
                }


                mPlayer = new MediaPlayer();
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {


                    mPlayer.setDataSource(playingurl);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
                } catch (SecurityException e) {
                    Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
                } catch (IllegalStateException e) {
                    Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mPlayer.prepare();
                } catch (IllegalStateException e) {
                    Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
                }
                mPlayer.start();

                uygunluk=0;


                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {  //bu fonksiyon ile sonraki sarki cagirilir.
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        Toast.makeText(PlayMusic.this, "Music Finished", Toast.LENGTH_SHORT).show();



                        if(situation.equals("sirali")) {

                            pozisyon++;
                            if (pozisyon >= arr.size()) {
                                pozisyon = 0;
                            }

                            playlst.performItemClick(playlst, pozisyon, playlst.getItemIdAtPosition(pozisyon));

                        }else if(situation.equals("shuffle")){
                            final Random rand = new Random();
                            int yer1 = rand.nextInt(arr.size());
                            pozisyon=yer1;

                            playlst.performItemClick(playlst, pozisyon, playlst.getItemIdAtPosition(pozisyon));


                        }else if(situation.equals("oylama")){

                            int enb=0;

                            int index=0;

                            for(int i=0;i<arr.size();i++){

                                if(nextsong[i]>=enb){
                                    enb=nextsong[i];
                                    index=i;
                                }


                            }

                            //showSnackBarMessage("sarki:"+nextsong[0]+nextsong[1]+nextsong[2]);


                            for(int i=0;i<arr.size();i++){

                               nextsong[i]=0;

                            }


                            pozisyon=index;



                            playlst.performItemClick(playlst, pozisyon, playlst.getItemIdAtPosition(pozisyon));


                        }

                    }
                });



                JSONObject data = new JSONObject();  //diger uyelere bilgilendirme gonderilir.
                try {

                    String sarki=playingurl;
                    int  length1 = mPlayer.getCurrentPosition();

                    data.put("song", sarki);
                    data.put("seek", length1);
                    data.put("grupname",groupname);

                    //addMessage(sarki);

                } catch (JSONException e) {
                    // return;
                }

                socket.emit("seeksong", data);



            }
        });


/*
        buttonStop = (Button) findViewById(R.id.button_stop);
        buttonStop.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(mPlayer!=null && mPlayer.isPlaying()){
                    mPlayer.stop();
                }
            }
        });

*/

  /*      buttonPause = (Button) findViewById(R.id.button_pause);
        buttonPause.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(buttonPause.getText().toString().equals("PAUSE MUSIC")) {

                    mPlayer.pause();
                     length = mPlayer.getCurrentPosition();
                    buttonPause.setText("CONTINUE MUSIC");
               // Toast.makeText(getApplicationContext(), "TEXT;"+buttonPause.getText().toString(), Toast.LENGTH_LONG).show();


                }else{
                    mPlayer.seekTo(length);
                    mPlayer.start();
                    buttonPause.setText("PAUSE MUSIC");

                }

            }
        });
  */



    }






    private void createMusicroom(String groupname,String username){

        String room=groupname;

        socket.connect();

        socket.emit("adduser", username);

        socket.emit("room", room);

        // socket.on("message", handleIncomingMessages);

        socket.on("playingsong", handleplayingsong);


        socket.on("seeksong_1", handleseeksong_1);

        socket.on("playpause1", handleplaypause1);

        socket.on("oylama", handleoylama);

        socket.on("aktifkullanici", handleliste);

        socket.on("seslimesaj_1", handlelseslimesaj_1);


    }


    private Emitter.Listener handlelseslimesaj_1 = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            PlayMusic.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String data="hey";
                    data=(String)args[0];

                    showSnackBarMessage("Sesli mesaj:"+data);



                }
            });
        }
    };



    private Emitter.Listener handleplaypause1 = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            PlayMusic.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String durum;
                    int lenght1;

                    try {
                        durum =data.getString("durum").toString();

                      //  button_playmusic.callOnClick();

                        callplaypause();


                    } catch (JSONException e) {
                        // return;
                    }

                }
            });
        }
    };


    private Emitter.Listener handleliste = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            PlayMusic.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String data="hey";
                    data=(String)args[0];

                    showSnackBarMessage(data);

                    addliste(data);

                }
            });
        }
    };

    private void addliste(String name){

        arrList.add(name);

        final CharSequence[] chars = arrList.toArray(new CharSequence[arrList.size()]);

            AlertDialog.Builder builder = new AlertDialog.Builder(PlayMusic.this);
            builder.setTitle("Grup Listesi..");
            builder.setItems(chars, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on colors[which]

                    showSnackBarMessage(chars[which].toString());
                }
            });
            builder.show();

    }


    private Emitter.Listener handleoylama = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            PlayMusic.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String durum;
                    int secim;

                    try {
                        secim=data.getInt("secim");

                        nextsong[secim]=nextsong[secim]+1;

                       // showSnackBarMessage("secilen:"+secim);

                    } catch (JSONException e) {
                        // return;
                    }

                }
            });
        }
    };


    private void callplaypause(){

        if(mPlayer!=null && mPlayer.isPlaying()) {

            mPlayer.pause();
            length = mPlayer.getCurrentPosition();
            // Toast.makeText(getApplicationContext(), "TEXT;"+buttonPause.getText().toString(), Toast.LENGTH_LONG).show();
            JSONObject data = new JSONObject();  //diger uyelere bilgilendirme gonderilir.


            button_playmusic.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_play_arrow_black_24dp, 0, 0);

        }else{
            mPlayer.seekTo(length);
            mPlayer.start();

            button_playmusic.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_pause_black_24dp, 0, 0);

        }

    }


    private Emitter.Listener handleseeksong_1 = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            PlayMusic.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String songname;
                    int lenght1;

                    try {
                        songname =data.getString("song").toString();
                        lenght1 = data.getInt("seek");

                        playingurl=songname;
                       int  seekjoin=lenght1;
                        uygunluk=0;

                        playcall(playingurl,seekjoin);


                    } catch (JSONException e) {
                        // return;
                    }

                }
            });
        }
    };

    private  void  playcall(String playingurl,int seekjoin){

        if(mPlayer!=null && mPlayer.isPlaying()){
            mPlayer.stop();
        }


        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


        try {

            mPlayer.setDataSource(playingurl);
        } catch (IllegalArgumentException e) {
            Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        } catch (SecurityException e) {
            Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        } catch (IllegalStateException e) {
            Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mPlayer.prepare();
        } catch (IllegalStateException e) {
            Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        }


        mPlayer.seekTo(seekjoin);

        mPlayer.start();




    }




    private Emitter.Listener handleplayingsong = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            PlayMusic.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String data="hey";
                    data=(String)args[0];
                    //addMessage(data);
                    comingrequest(data);
                }
            });
        }
    };

    private void comingrequest(String istek){

         if(mPlayer!=null && mPlayer.isPlaying()) {


             JSONObject data = new JSONObject();
             try {

                 String sarki = playingurl;
                 int length1 = mPlayer.getCurrentPosition();

                 data.put("song", sarki);
                 data.put("seek", length1);
                 data.put("grupname", groupname);

                 //addMessage(sarki);

             } catch (JSONException e) {
                 // return;
             }

             socket.emit("seeksong", data);


         }

    }

    private void addMessage(String message) {


        showSnackBarMessage("gelen mesaj:"+message.toString());


    }

    private void showSnackBarMessage(String message) {

        Snackbar.make(findViewById(R.id.activity_playmusic),message,Snackbar.LENGTH_SHORT).show();

    }


    @Override
    protected void onDestroy() {

        super.onDestroy();

        if(mPlayer!=null && mPlayer.isPlaying()){
            mPlayer.stop();
        }


    }



    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //txtSpeechInput.setText(result.get(0));

                  //  showSnackBarMessage(result.get(0));

                    socket.emit("seslimesaj", result.get(0));


                }
                break;
            }

        }
    }





}

