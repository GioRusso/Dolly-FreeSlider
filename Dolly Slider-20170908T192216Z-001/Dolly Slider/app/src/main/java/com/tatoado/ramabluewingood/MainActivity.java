package com.tatoado.ramabluewingood;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;



public class MainActivity extends AppCompatActivity implements YouTubePlayer.OnInitializedListener {


            public static final String API_KEY = "AIzaSyAjD9AZT9M0uMFdsE9ZkGOnT1CITElWzvo";

            public static final String VIDEO_ID = "z2X__8tgIF8";

            Handler bluetoothIn;


            final int handlerState = 0;                         //used to identify handler message
            private BluetoothAdapter btAdapter = null;
            private BluetoothSocket btSocket = null;
            private StringBuilder recDataString = new StringBuilder();

            private ConnectedThread mConnectedThread;

            // SPP UUID service - this should work for most devices
            private final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

            // String for MAC address
            String address = null;

            ViewFlipper vf;
            float init_x;
            int duracion = 300;
            int Vista = 2;

            String Distancia,Velocidad,Grados,NumFotos,Mensaje;

            int LimInfDistancia=0;
            int LimSupDistancia=10000;
            int LimInfGrados=0;
            int LimSupGrados=360;
            boolean EstadoFoto=false;
            boolean EstadoVideo=false;

            Button BotonFoto;
            Button BotonVideo;

            boolean Bluetooth=false;

            @Override
            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                //Toast.makeText(getBaseContext(), "CREATE", Toast.LENGTH_LONG).show();
                setContentView(R.layout.flipper);
                vf = (ViewFlipper) findViewById(R.id.viewFlipper);
                vf.setOnTouchListener(new ListenerTouchViewFlipper());
                vf.showNext();

                YouTubePlayerSupportFragment frag =
                        (YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.youtube_fragment);
                frag.initialize(API_KEY, this);


                Spinner spinner = (Spinner) findViewById(R.id.spinnervelocidadfoto);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                        R.array.itemsvelocidad, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                Spinner spinner2 = (Spinner) findViewById(R.id.spinnervelocidadvideo);
                ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                        R.array.itemsvelocidad, android.R.layout.simple_spinner_item);
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner2.setAdapter(adapter2);

                btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
                checkBTState();

                BotonFoto = (Button) findViewById(R.id.BotonFoto);
                BotonVideo = (Button) findViewById(R.id.BotonVideo);


            }

            @Override
            public void onResume() {
                super.onResume();

                   // Toast.makeText(getBaseContext(), "RESUME", Toast.LENGTH_LONG).show();

                    //Get MAC address from DeviceListActivity via intent
                    Intent intent = getIntent();

                    //Get the MAC address from the DeviceListActivty via EXTRA
                    address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    
                    //create device and set the MAC address

                    BluetoothDevice device = btAdapter.getRemoteDevice(address);

                    try {
                        btSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), R.string.FalloConex, Toast.LENGTH_LONG).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        btSocket.connect();
                    } catch (IOException e) {
                        try {
                            btSocket.close();
                        } catch (IOException e2) {
                            //insert code to deal with this
                        }
                    }
                    mConnectedThread = new ConnectedThread(btSocket);
                    mConnectedThread.start();

                if (mConnectedThread.write("PRUEBA-")) {
                    Toast.makeText(this, "Conexión exitosa", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPause() {
                super.onPause();
                if (Bluetooth) {
                   // Toast.makeText(getBaseContext(), "PAUSE", Toast.LENGTH_LONG).show();
                    try {
                        //Don't leave Bluetooth sockets open when leaving activity
                        btSocket.close();
                    } catch (IOException e2) {
                        //insert code to deal with this
                    }
                }
            }

            public void ConexionBluetooth(View vie){
                Intent i = new Intent(MainActivity.this, DeviceListActivity.class);
               startActivity(i);
            }

            public void IniciarFoto(View vfoto){
                EstadoFoto=!EstadoFoto;

                if (EstadoFoto) {

                    if (ObtenerParametros(true)) {
                        BotonFoto.setText(R.string.Detener);
                        BotonFoto.setBackgroundColor(Color.parseColor("#FF0000"));
                        Log.i("Foto", Mensaje);
                        mConnectedThread.write(Mensaje);
                    }else{EstadoFoto=!EstadoFoto;}
                }else{
                    BotonFoto.setText(R.string.Iniciar);
                    BotonFoto.setBackgroundColor(Color.parseColor("#3841E5"));
                    mConnectedThread.write("D-");
                }
            }

            public void IniciarVideo(View vvideo){
                EstadoVideo=!EstadoVideo;

                if (EstadoVideo) {
                    if(ObtenerParametros(false)) {
                        BotonVideo.setText(R.string.Detener);
                        BotonVideo.setBackgroundColor(Color.parseColor("#FF0000"));
                        Log.i("Video", Mensaje);
                        mConnectedThread.write(Mensaje);
                    }else{ EstadoVideo=!EstadoVideo; }
                }else{
                    BotonVideo.setText(R.string.Iniciar);
                    BotonVideo.setBackgroundColor(Color.parseColor("#3841E5"));
                    mConnectedThread.write("D-");
                }
            }


            public boolean ObtenerParametros(boolean Opcion){

                if (Opcion){
                    try {
                        EditText EditDistanciaFoto = (EditText) findViewById(R.id.editdistanciafoto);
                        EditText EditGradosFoto = (EditText) findViewById(R.id.editgirofoto);
                        EditText EditNumFotos = (EditText) findViewById(R.id.editnumfotos);
                        Spinner SpinnerFoto = (Spinner) findViewById(R.id.spinnervelocidadfoto);

                        NumFotos = EditNumFotos.getText().toString();
                        if(NumFotos.equals("")){Toast.makeText(this,"El campo Número de fotos no puede estar vacío",Toast.LENGTH_SHORT).show();
                            return false;}

                        Distancia = EditDistanciaFoto.getText().toString();
                        if(Distancia.equals("")){
                            Toast.makeText(this,"El campo Distancia no puede estar vacío",Toast.LENGTH_SHORT).show();
                            return false;}
                        else{if(Integer.parseInt(Distancia) < LimInfDistancia || Integer.parseInt(Distancia) > LimSupDistancia) {
                            Toast.makeText(this,
                                    "El campo Distancia debe estar entre "+String.valueOf(LimInfDistancia)+" y "+String.valueOf(LimSupDistancia),
                                    Toast.LENGTH_SHORT).show();
                            return false;}}

                        Grados = EditGradosFoto.getText().toString();
                        if(Grados.equals("")){
                            Toast.makeText(this,"El campo Grados no puede estar vacío",Toast.LENGTH_SHORT).show();
                            return false;}
                        else{if (Integer.parseInt(Grados) < LimInfGrados || Integer.parseInt(Grados) > LimSupGrados) {
                            Toast.makeText(this,
                                    "El campo Grados debe estar entre "+String.valueOf(LimInfGrados)+" y "+String.valueOf(LimSupGrados),
                                    Toast.LENGTH_SHORT).show();
                            return false;}}


                        Velocidad = SpinnerFoto.getSelectedItem().toString();
                        Mensaje = "F-" + Distancia + "-" + Grados + "-" + Velocidad + "-" + NumFotos + "-";
                    }catch (Error error){return false;}


                }else{
                    try{
                    EditText EditDistanciaVideo = (EditText) findViewById(R.id.editdistanciavideo);
                    EditText EditGradosVideo = (EditText) findViewById(R.id.editgirovideo);
                    Spinner SpinnerVideo=(Spinner) findViewById(R.id.spinnervelocidadvideo);

                        Distancia = EditDistanciaVideo.getText().toString();
                        if(Distancia.equals("")){
                            Toast.makeText(this,"El campo Distancia no puede estar vacío",Toast.LENGTH_SHORT).show();
                            return false;}
                        else{if(Integer.parseInt(Distancia) < LimInfDistancia || Integer.parseInt(Distancia) > LimSupDistancia) {
                            Toast.makeText(this,
                                    "El campo Distancia debe estar entre "+String.valueOf(LimInfDistancia)+" y "+String.valueOf(LimSupDistancia),
                                    Toast.LENGTH_SHORT).show();
                            return false;}}

                        Grados = EditGradosVideo.getText().toString();
                        if(Grados.equals("")){
                            Toast.makeText(this,"El campo Grados no puede estar vacío",Toast.LENGTH_SHORT).show();
                            return false;}
                        else{if (Integer.parseInt(Grados) < LimInfGrados || Integer.parseInt(Grados) > LimSupGrados) {
                            Toast.makeText(this,
                                    "El campo Grados debe estar entre "+String.valueOf(LimInfGrados)+" y "+String.valueOf(LimSupGrados),
                                    Toast.LENGTH_SHORT).show();
                            return false;}}

                    Velocidad = SpinnerVideo.getSelectedItem().toString();
                    Mensaje = "V-"+Distancia+"-"+Grados+"-"+Velocidad+ "-";
                    }catch (Error error){return false;}


                }
                return true;
            }

           private class ListenerTouchViewFlipper implements View.OnTouchListener {

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: //Cuando el usuario toca la pantalla por primera vez
                            init_x = event.getX();
                            return true;
                        case MotionEvent.ACTION_UP: //Cuando el usuario deja de presionar//
                            float distance = init_x - event.getX();
                            if (distance < -100) {
                                vf.setInAnimation(inFromLeftAnimation());
                                vf.setOutAnimation(outToRightAnimation());
                                vf.showPrevious();
                                if (Vista == 3 || Vista == 2) {
                                    Vista--;
                                } else {
                                    Vista = 3;
                                }
                            }

                            if (distance > 100) {
                                vf.setInAnimation(inFromRightAnimation());
                                vf.setOutAnimation(outToLeftAnimation());
                                vf.showNext();
                                if (Vista == 1 || Vista == 2) {
                                    Vista++;
                                } else {
                                    Vista = 1;
                                }
                            }

                        default:
                            break;
                    }
                    return false;
                }
            }

            @Override
            public boolean onCreateOptionsMenu(Menu menu) {
                // Inflate the menu; this adds items to the action bar if it is present.
                getMenuInflater().inflate(R.menu.menu_maindolly, menu);
                return true;
            }

            @Override
            public boolean onOptionsItemSelected(MenuItem item) {

                switch (item.getItemId()) {


                    case R.id.camara:
                        if (Vista == 2) {
                            vf.setInAnimation(inFromRightAnimation());
                            vf.setOutAnimation(outToLeftAnimation());
                            vf.showNext();
                        } else if (Vista == 1) {
                            vf.setInAnimation(inFromLeftAnimation());
                            vf.setOutAnimation(outToRightAnimation());
                            vf.showPrevious();
                        }
                        Vista = 3;
                        break;

                    case R.id.video:
                        if (Vista == 2) {
                            vf.setInAnimation(inFromLeftAnimation());
                            vf.setOutAnimation(outToRightAnimation());
                            vf.showPrevious();
                        } else if (Vista == 3) {
                            vf.setInAnimation(inFromRightAnimation());
                            vf.setOutAnimation(outToLeftAnimation());
                            vf.showNext();
                        }
                        Vista = 1;
                        break;

                    case R.id.home:
                        if (Vista == 3) {
                            vf.setInAnimation(inFromLeftAnimation());
                            vf.setOutAnimation(outToRightAnimation());
                            vf.showPrevious();
                        } else if (Vista == 1) {
                            vf.setInAnimation(inFromRightAnimation());
                            vf.setOutAnimation(outToLeftAnimation());
                            vf.showNext();
                        }
                        Vista = 2;
                        break;


                    default:
                        Toast.makeText(this, "Botón config pulsado", Toast.LENGTH_SHORT).show();
                        return super.onOptionsItemSelected(item);
                }
                Log.i("MENU:", String.valueOf(Vista));

                return super.onOptionsItemSelected(item);
            }


            private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

                return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
                //creates secure outgoing connecetion with BT device using UUID
            }


            //Checks that the Android device Bluetooth is available and prompts to be turned on if off
            private void checkBTState() {

                if (btAdapter == null) {
                    Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
                } else {
                    if (btAdapter.isEnabled()) {
                        //Toast.makeText(getBaseContext(), "soporta bluetooth", Toast.LENGTH_LONG).show();
                    } else {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, 1);
                    }
                }
            }

            //create new class for connect thread
            private class ConnectedThread extends Thread {
                private final InputStream mmInStream;
                private final OutputStream mmOutStream;

                //creation of the connect thread
                public ConnectedThread(BluetoothSocket socket) {
                    InputStream tmpIn = null;
                    OutputStream tmpOut = null;

                    try {
                        //Create I/O streams for connection
                        tmpIn = socket.getInputStream();
                        tmpOut = socket.getOutputStream();
                    } catch (IOException e) {
                    }

                    mmInStream = tmpIn;
                    mmOutStream = tmpOut;
                }


                public void run() {
                    byte[] buffer = new byte[256];
                    int bytes;

                    // Keep looping to listen for received messages
                    while (true) {
                        try {
                            bytes = mmInStream.read(buffer);            //read bytes from input buffer
                            String readMessage = new String(buffer, 0, bytes);
                            // Send the obtained bytes to the UI Activity via handler
                            bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                        } catch (IOException e) {
                            break;
                        }
                    }
                }

                //write method
                public boolean write(String input) {
                    byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
                    try {
                        mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
                    } catch (IOException e) {
                        //if you cannot write, close the application
                        Toast.makeText(getBaseContext(), "No se puede establecer conexión con el dispositivo remoto", Toast.LENGTH_LONG).show();
                        //finish();
                        Intent i = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivity(i);
                        return false;
                    }
                    return true;
                }
            }

        @Override
        public void onInitializationFailure(Provider provider, YouTubeInitializationResult result) {
            Toast.makeText(this, "Failured to Initialize!", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
            player.setPlayerStateChangeListener(playerStatechangeListener);
            player.setPlaybackEventListener(playbackEventListener);
            if (!wasRestored) {
                player.cueVideo(VIDEO_ID);
            }
        }

        private PlaybackEventListener playbackEventListener = new PlaybackEventListener() {
            @Override
            public void onPlaying() {

            }

            @Override
            public void onPaused() {

            }

            @Override
            public void onStopped() {

            }

            @Override
            public void onBuffering(boolean b) {

            }

            @Override
            public void onSeekTo(int i) {

            }
        };

        private PlayerStateChangeListener playerStatechangeListener = new PlayerStateChangeListener() {

            @Override
            public void onAdStarted() {

            }

            @Override
            public void onError(ErrorReason errorReason) {

            }

            @Override
            public void onLoaded(String s) {

            }

            @Override
            public void onLoading() {

            }


            @Override
            public void onVideoStarted() {

            }

            @Override
            public void onVideoEnded() {

            }


        };

        private Animation inFromRightAnimation() {

            Animation inFromRight = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);

            inFromRight.setDuration(duracion);
            inFromRight.setInterpolator(new AccelerateInterpolator());

            return inFromRight;

        }

        private Animation outToLeftAnimation() {
            Animation outtoLeft = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, -1.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f);
            outtoLeft.setDuration(duracion);
            outtoLeft.setInterpolator(new AccelerateInterpolator());
            return outtoLeft;
        }

        private Animation inFromLeftAnimation() {
            Animation inFromLeft = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, -1.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f);
            inFromLeft.setDuration(duracion);
            inFromLeft.setInterpolator(new AccelerateInterpolator());
            return inFromLeft;
        }

        private Animation outToRightAnimation() {
            Animation outtoRight = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, +1.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f);
            outtoRight.setDuration(duracion);
            outtoRight.setInterpolator(new AccelerateInterpolator());
            return outtoRight;
        }




    }

