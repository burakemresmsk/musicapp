package com.learn2crack;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.learn2crack.model.User;
import com.learn2crack.network.NetworkUtil;
import com.learn2crack.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;




/**
 * Created by burakemresmsk on 05/05/2017.
 */

public class CreateGroup extends AppCompatActivity {

    public static final int PICK_IMAGE = 100;


    Servis service1;

    public String playlistname;
    ArrayList<String> playlist = new ArrayList<String>();



    private Socket socket;
    {
        try{
            socket = IO.socket(Constants.BASE_URL_socketio);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    private SharedPreferences mSharedPreferences;
    private String mToken;
    private int yüklenme=0;
    private String mEmail;
    public String username;
    public String group_adi;

    private CompositeSubscription mSubscriptions;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_group);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        // Change base URL to your upload server URL.
        service1 = new Retrofit.Builder().baseUrl(Constants.BASE_URL_socketio_upload).client(client).build().create(Servis.class);


        group_adi = getIntent().getStringExtra("grup_name");

        TextView grup_name = (TextView) findViewById(R.id.textview_grupname);

        Button create_playlist = (Button) findViewById(R.id.button_create_playlist);

        Button start_music = (Button) findViewById(R.id.button_startmusic);


        grup_name.setText(group_adi.toString());

        group_adi=group_adi.toString();


        mSubscriptions = new CompositeSubscription();

        initSharedPreferences();
        findname();

       // createMusicroom(group_adi);




        if (create_playlist != null) {
            create_playlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {




                    final EditText edittext = new EditText(getApplicationContext());

                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateGroup.this);

                    builder.setTitle("Create Playlist");
                    builder.setMessage("Playlist adı giriniz!!!");
                    builder.setView(edittext);
                    edittext.setTextColor(Color.RED);
                    builder.setNegativeButton("İPTAL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            showSnackBarMessage("Playlist iptal edildi..");

                        }
                    });


                    builder.setPositiveButton("TAMAM", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //Tamam butonuna basılınca yapılacaklar

                            Editable YouEditTextValue = edittext.getText();
                            //Toast.makeText(getApplicationContext(), "Name:" + YouEditTextValue, Toast.LENGTH_SHORT).show();
                            playlistname = YouEditTextValue.toString();

                            Intent intent = new Intent();
                            intent.setType("audio/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            startActivityForResult(Intent.createChooser(intent, "Select music"), PICK_IMAGE);



                        }
                    });


                    builder.show();





                }




            });



        }



        start_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(playlist.isEmpty()||yüklenme==0){

                   // Toast.makeText(getApplicationContext(),"daha liste oluşturmadınız...",Toast.LENGTH_SHORT).show();
                    showSnackBarMessage("Please Create Playlist...");

                }else {


                    Intent intent = new Intent(CreateGroup.this, PlayMusic.class);
                    intent.putExtra("array_list", playlist);
                    intent.putExtra("groupname", group_adi);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();

                }



            }

        });



    }



    private void createMusicroom(String groupname,String username){

        String room=groupname;

        socket.connect();

        socket.emit("adduser", username);

        socket.emit("room", room);

       // socket.on("message", handleIncomingMessages);


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

        Snackbar.make(findViewById(R.id.activity_create_group),message,Snackbar.LENGTH_SHORT).show();

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Uploading........");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();


        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {




           ClipData clipData = data.getClipData();

            for (int i = 0; i < clipData.getItemCount(); i++) {


                final Handler handler = new Handler();
                Runnable task = new Runnable() {
                    @Override
                    public void run() {

                        handler.postDelayed(this, 1000);

                    }
                };
                handler.post(task);



                mProgressDialog.show();



                ClipData.Item item = clipData.getItemAt(i);
                Uri uri = item.getUri();

                String realfilePath = getRealPathFromURI_API19(getBaseContext(), uri);

               // Toast.makeText(getApplicationContext(), "realfilepath:" + realfilePath, Toast.LENGTH_SHORT).show();


                File file = new File(realfilePath);

                RequestBody reqFile = RequestBody.create(MediaType.parse("audio/*"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("upload", file.getName(), reqFile);
                RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");


                retrofit2.Call<okhttp3.ResponseBody> req = service1.postImage(body, name);

                req.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                      //  Toast.makeText(getApplicationContext(), "basari ile gonderildi", Toast.LENGTH_SHORT).show();
                     showSnackBarMessage("Playlist uploaded successfully");

                        mProgressDialog.dismiss();


                        String[] items = realfilePath.split("/");
                        String musicname="bos";

                        for (String item : items)
                        {
                            //Toast.makeText(getApplicationContext(), "item:" + item, Toast.LENGTH_SHORT).show();
                            musicname=item;
                        }
                        playlist.add(musicname);

                       // socket.emit("playlist",musicname,group_adi);

                      //  Toast.makeText(getApplicationContext(), "grupadi:"+group_adi, Toast.LENGTH_SHORT).show();

                        JSONObject data = new JSONObject();
                        try {

                            data.put("musicname", musicname);
                            data.put("grupname", group_adi);
                        } catch (JSONException e) {
                            // return;
                        }

                        socket.emit("playlist",data);


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {



                        String[] items = realfilePath.split("/");
                        String musicname="bos";

                        for (String item : items)
                        {
                            //Toast.makeText(getApplicationContext(), "item:" + item, Toast.LENGTH_SHORT).show();
                            musicname=item;
                        }
                        playlist.add(musicname);

                        // socket.emit("playlist",musicname,group_adi);

                        //  Toast.makeText(getApplicationContext(), "grupadi:"+group_adi, Toast.LENGTH_SHORT).show();

                        JSONObject data = new JSONObject();
                        try {

                            data.put("musicname", musicname);
                            data.put("grupname", group_adi);
                        } catch (JSONException e) {
                            // return;
                        }

                        socket.emit("playlist",data);

                        mProgressDialog.dismiss();

                        t.printStackTrace();

                       // Toast.makeText(getApplicationContext(), "maalesef yollayamadik", Toast.LENGTH_SHORT).show();


                    }




                });








            }



            yüklenme=1;





        }



    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Audio.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Audio.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }


}
