package com.learn2crack;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.learn2crack.model.User;
import com.learn2crack.network.NetworkUtil;
import com.learn2crack.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by burakemresmsk on 18/05/2017.
 */

public class Joinmusic  extends AppCompatActivity {

    MediaPlayer mPlayer;
    Button buttonPlay;
    Button buttonStop;
    Button buttonPause;
    public String group_adi;

    Button button_backmusic;
    Button button_playmusic;
    Button button_forwardmusic;
    Button button_shuffle;
    Button button_loopmusic;



    Button button_speech;
    Button button_aktifuser;

    List<String> arrList;
    private final int REQ_CODE_SPEECH_INPUT = 100;



    private SharedPreferences mSharedPreferences;
    private String mToken;
    private String mEmail;
    public String username;
    private CompositeSubscription mSubscriptions;

    private  int pozisyon=0;

    private  int uygunluk=0;


    String situation="sirali";


    Button button_oylama;



    String url =Constants.url;

    String playingurl;

    private int length=0;

    private int seekjoin=0;


    ListView playlst;
    ArrayList<String> yourlist;

    private ArrayAdapter<String> listAdapter ;



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

        arrList = new ArrayList<String>();

        group_adi = getIntent().getStringExtra("grup_name");

        playlst=(ListView) findViewById(R.id.listview_playlist);

        yourlist = new ArrayList<String>();

        listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow,R.id.rowTextView, yourlist);

        playlst.setAdapter( listAdapter );


        mSubscriptions = new CompositeSubscription();

        initSharedPreferences();
        findname();

        playlst.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                pozisyon=position;

                String song=yourlist.get(position);
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
               // showSnackBarMessage("Lütfen konuşun");


                promptSpeechInput();

            }
        });

        button_aktifuser.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                // showSnackBarMessage("kullanıcı listesi");


                arrList.clear();

                socket.emit("aktifkullanici_1", "kimleraktif");


            }
        });



        button_oylama = (Button) findViewById(R.id.button_oylama);

        button_oylama.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub



                if(uygunluk==0) {

                    final CharSequence[] chars = yourlist.toArray(new CharSequence[yourlist.size()]);


                    AlertDialog.Builder builder = new AlertDialog.Builder(Joinmusic.this);
                    builder.setTitle("Sonraki şarkıyı oylayınız..");
                    builder.setItems(chars, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // the user clicked on colors[which]

                            showSnackBarMessage(chars[which].toString());

                            JSONObject data = new JSONObject();  //diger uyelere bilgilendirme gonderilir.
                            try {

                                data.put("grupname", group_adi);
                                data.put("secim", which);


                            } catch (JSONException e) {
                                // return;
                            }

                            socket.emit("oylama", data);
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
                    pozisyon=yourlist.size()-1;
                }


                String song=yourlist.get(pozisyon);
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

                Toast.makeText(getApplicationContext(),"poz:"+pozisyon,Toast.LENGTH_SHORT).show();

                if(pozisyon>=yourlist.size()){
                    pozisyon=0;
                }
                String song=yourlist.get(pozisyon);
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
                        data.put("grupname",group_adi);

                        //addMessage(sarki);

                    } catch (JSONException e) {
                        // return;
                    }

                    socket.emit("playpause1", data);


                    button_playmusic.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_play_arrow_black_24dp, 0, 0);

                }else{
                    mPlayer.seekTo(length);
                    mPlayer.start();

                    JSONObject data = new JSONObject();  //diger uyelere bilgilendirme gonderilir.
                    try {

                        data.put("durum", "play");
                        data.put("grupname",group_adi);

                        //addMessage(sarki);

                    } catch (JSONException e) {
                        // return;
                    }

                    socket.emit("playpause1", data);


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

                JSONObject data = new JSONObject();  //diger uyelere bilgilendirme gonderilir.
                try {

                    String sarki=playingurl;
                    int  length1 = mPlayer.getCurrentPosition();

                    data.put("song", sarki);
                    data.put("seek", length1);
                    data.put("grupname",group_adi);

                    //addMessage(sarki);

                } catch (JSONException e) {
                    // return;
                }

                socket.emit("seeksong_1", data);




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

        buttonPause = (Button) findViewById(R.id.button_pause);
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

        JSONObject sendText = new JSONObject();

        try {
            sendText.put("text", groupname);

        }catch (JSONException e){

        }

         socket.emit("newjoin", room);

        JSONObject data = new JSONObject();
        try {

            data.put("istek", "necaliyo");
            data.put("grupname", group_adi);
        } catch (JSONException e) {
            // return;
        }

         socket.emit("playingsong", data);

         socket.on("message", handleIncomingMessages);

         socket.on("newjoin", handlenewjoin);

         socket.on("seeksong", handleseeksong);

        socket.on("playpause", handleplaypause);

        //  socket.on("playingsong", handleplayingsong);


        // socket.on("message", handleIncomingMessages);

        socket.on("aktifkullanici_1", handleliste);


        socket.on("seslimesaj", handlelseslimesaj);

    }

    private Emitter.Listener handlelseslimesaj = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            Joinmusic.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String data="hey";
                    data=(String)args[0];

                    showSnackBarMessage("Sesli mesaj:"+data);



                }
            });
        }
    };


    private Emitter.Listener handleliste = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            Joinmusic.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String data="hey";
                    data=(String)args[0];

                  //  showSnackBarMessage(data);

                    addliste(data);

                }
            });
        }
    };

    private void addliste(String name){

        arrList.add(name);

        final CharSequence[] chars = arrList.toArray(new CharSequence[arrList.size()]);


        AlertDialog.Builder builder = new AlertDialog.Builder(Joinmusic.this);
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


    private void addMessage(String message) {


        showSnackBarMessage("gelen mesaj:"+message.toString());


    }




    private Emitter.Listener handleIncomingMessages = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            Joinmusic.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message;

                    try {
                        message = data.getString("text").toString();
                        addMessage(message);

                    } catch (JSONException e) {
                        // return;
                    }

                }
            });
        }
    };





    private Emitter.Listener handlenewjoin = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            Joinmusic.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  //  JSONObject data = (JSONObject) args[0];
                    String data="hey";
                    data=(String)args[0];
                    String message;
                    addMessage(data);
                    yourlist.add(data);
                    listAdapter.notifyDataSetChanged();

                  /*  try {
                        //message = data.getString("text").toString();
                       // addMessage(message);
                        addMessage(data);

                    } catch (JSONException e) {
                        // return;
                    } */

                }
            });
        }
    };


    private Emitter.Listener handleplayingsong = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            Joinmusic.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //  JSONObject data = (JSONObject) args[0];
                    String data="hey";
                    data=(String)args[0];
                    String message;
                    addMessage(data);

                  /*  try {
                        //message = data.getString("text").toString();
                       // addMessage(message);
                        addMessage(data);

                    } catch (JSONException e) {
                        // return;
                    } */

                }
            });
        }
    };

    private Emitter.Listener handleseeksong = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            Joinmusic.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                     JSONObject data = (JSONObject) args[0];
                    String songname;
                    int lenght1;

                    try {
                        songname =data.getString("song").toString();
                        lenght1 = data.getInt("seek");

                        playingurl=songname;
                        seekjoin=lenght1;
                        playcall(playingurl,seekjoin);

                        button_playmusic.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_pause_black_24dp, 0, 0);


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

     uygunluk=0;


}

    private Emitter.Listener handleplaypause = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            Joinmusic.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String durum;
                    String songname;

                    int lenght1;

                    try {
                        durum =data.getString("durum").toString();
                        lenght1 = data.getInt("seek");

                        songname =data.getString("song").toString();

                        playingurl=songname;


                        //button_playmusic.callOnClick();

                       callplaypause(lenght1,playingurl);

                    } catch (JSONException e) {
                        // return;
                    }

                }
            });
        }
    };

    private void callplaypause(int length2,String url1){

        if(mPlayer!=null && mPlayer.isPlaying()) {

            mPlayer.pause();
            length = mPlayer.getCurrentPosition();
            // Toast.makeText(getApplicationContext(), "TEXT;"+buttonPause.getText().toString(), Toast.LENGTH_LONG).show();
            JSONObject data = new JSONObject();  //diger uyelere bilgilendirme gonderilir.


            button_playmusic.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_play_arrow_black_24dp, 0, 0);

        }else if(mPlayer==null){

            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {

                mPlayer.setDataSource(url1);
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


            mPlayer.seekTo(length2);

            mPlayer.start();

            button_playmusic.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_pause_black_24dp, 0, 0);


        }else{

            mPlayer.seekTo(length);
            mPlayer.start();

            button_playmusic.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_pause_black_24dp, 0, 0);

        }

    }


    private  void findname(){

        mSubscriptions.add(NetworkUtil.getRetrofit(mToken).getProfile(mEmail)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));

    }

    private void initSharedPreferences() {

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = mSharedPreferences.getString(Constants.TOKEN,"");
        mEmail = mSharedPreferences.getString(Constants.EMAIL,"");
    }

    private void handleResponse(User user) {

        username=user.getName();

        showSnackBarMessage("username:"+username.toString());

        createMusicroom(group_adi,username);

    }

    private void handleError(Throwable error) {

        if (error instanceof HttpException) {

            Gson gson = new GsonBuilder().create();

            try {

                String errorBody = ((HttpException) error).response().errorBody().string();
                com.learn2crack.model.Response response = gson.fromJson(errorBody, com.learn2crack.model.Response.class);
                showSnackBarMessage(response.getMessage());

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            showSnackBarMessage("Network Error !");
        }
    }

    private void showSnackBarMessage(String message) {

        Snackbar.make(findViewById(R.id.activity_playmusic),message,Snackbar.LENGTH_SHORT).show();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();

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

                   // showSnackBarMessage(result.get(0));

                    socket.emit("seslimesaj_1", result.get(0));


                }
                break;
            }

        }
    }




}
