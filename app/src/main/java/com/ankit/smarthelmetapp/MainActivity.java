package com.ankit.smarthelmetapp;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;

import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;

import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_PERMISSION = 786;
    private String deviceName = null;
    private String deviceAddress;
    private String strc = null;
    private String strn = null;
    private boolean HELMET, CHINSTRAP, ALCOHOL, DEVICECONN;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;
    private Uri filePath;
    private String arduinoMsg;
    // request code
    private final int PICK_IMAGE_REQUEST = 22;
    FirebaseStorage storage;
    StorageReference storageReference;
    private final static int MY_CAMERA_PERMISSION_CODE= 100;
    private final static int CAMERA_REQUEST = 1888;
    private final static int REQUEST_CODE_PERMISSIONS = 101;
    private String[] REQUIRED_PERMISSION = new String[]{"android.permission.CAMERA","android.permission.WRITE_EXTERNAL_STORAGE"};


    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private static final int PICK_CONTACT_REQUEST_CODE = 101;
    //private static final String TAG = SelectContactActivity.class.getSimpleName();

    double latitude;
    double longitude;
    FusedLocationProviderClient fusedLocationProviderClint;
    String cmdText = null;
    private boolean select = false;

    private UDPSocket mUdpClient;
    private String mServerAddressBroadCast = "255.255.255.255";
    InetAddress mServerAddr;
    int mServerPort = 6868;
    final byte[] mRequestConnect      = new byte[]{'w','h','o','a','m','i'};
    private Bitmap mBitmapDebug;
    private String mServerExactAddress;
    private WebSocketClient mWebSocketClient;
    ImageView mServerImageView;
    private boolean mProcessing = false;
    private Bitmap mBitmapGrab = null;
    private boolean mStream = false;
    private final Size CamResolution = new Size(640, 480);

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    PreviewView previewView;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private Executor executor = Executors.newSingleThreadExecutor();
    String stor,stor2 = null;
    UploadTask uploadTask;
    private StorageReference storageRef;


    Button buttontest;
    Button buttonContact;
    Button buttonConnect;
    Toolbar toolbar;
    ProgressBar progressBar;
    TextView textViewInfo;
    TextView Helmet;
    TextView Chinstrap;
    TextView Alcohol;
    TextView Helmet2;
    TextView Chinstrap2;
    TextView Alcohol2;
    DAOVehicle dao = new DAOVehicle();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_roundshape);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        //this.imageView = (ImageView)this.findViewById(R.id.imageView1);
        previewView = findViewById(R.id.viewFinder);

        //FirebaseApp.initializeApp(this);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://smart-helmet-v2.appspot.com");



        // UI Initialization
        buttontest = findViewById(R.id.testbutton);
        buttonContact = findViewById(R.id.buttoncontact);
        buttonConnect = findViewById(R.id.buttonConnect);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        textViewInfo = findViewById(R.id.textViewInfo);
        progressBar.setVisibility(View.GONE);

        //textViewInfo.setVisibility(View.VISIBLE);


        TextView count;
        fusedLocationProviderClint = LocationServices.getFusedLocationProviderClient(this);
        textViewInfo.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE}, 1001);
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);
            requestPermissions(new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        }




        //select and view contact from select contact activity

        buttonContact.setOnClickListener(new View.OnClickListener() {
           /* @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SelectContactActivity.class);
                startActivity(intent);
            }*/

            @Override
            public void onClick(View v) {
                pickContact();
            }

        });


        //Intent intent = getIntent();
        //strc = intent.getStringExtra("name");
        //strn = intent.getStringExtra("number");
        //txtContacts.setText(strc);

        buttontest.setVisibility(View.VISIBLE);
        getlocation(); //For location

        // connect to esp 32 cam
        mUdpClient = new UDPSocket(12345);
        mUdpClient.runUdpServer();

        try {
            mServerAddr = InetAddress.getByName(mServerAddressBroadCast);
        }catch (Exception e){

        }

        AssetManager assetManager = getAssets();
        if (MyConstants.DEBUG) {
            try {
                InputStream istr = assetManager.open("image1.jpg");
                Bitmap tmpBitmap = BitmapFactory.decodeStream(istr);
                mBitmapDebug = Bitmap.createScaledBitmap(tmpBitmap, CamResolution.getWidth(), CamResolution.getHeight(), false);
            } catch (IOException e) {
                // handle exception
            }
        }


        // If a bluetooth device has been selected from SelectDeviceActivity
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null) {
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progress and connection status
            toolbar.setSubtitle("Connecting to " + deviceName + "...");
            progressBar.setVisibility(View.VISIBLE);
            buttonConnect.setEnabled(false);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress);
            createConnectThread.start();
        }

        /*
        Second most important piece of Code. GUI Handler
         */
        handler = new Handler(Looper.getMainLooper()) {
            @SuppressLint("SetTextI18n")
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                toolbar.setSubtitle("Connected to " + deviceName);
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                DEVICECONN = true;
                                break;
                            case -1:
                                toolbar.setSubtitle("Device fails to connect");
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                DEVICECONN = false;
                                break;
                            default:
                                toolbar.setSubtitle("Device is not connect");
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(false);
                                DEVICECONN = false;
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        /*if (arduinoMsg == "okh"){
                            textViewInfo.setText("Arduino Message : " + arduinoMsg);
                            Helmet.setBackgroundColor(getResources().getColor(R.color.green));
                            HELMET = true;
                            break;
                        }else if(arduinoMsg == "noth"){
                            textViewInfo.setText("Arduino Message : " + arduinoMsg);
                            Helmet.setBackgroundColor(getResources().getColor(R.color.red));
                            HELMET = false;
                            break;
                        }*/

                        sensorcapture();
                        safetodrive();
                        break;
                }
            }

        };



        // Select Bluetooth Device
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Move to adapter list
                Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                startActivity(intent);
            }
        });


        buttontest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendSMS();
                /*if (!mStream) {
                    try {
                        mServerAddr = InetAddress.getByName(mServerAddressBroadCast);
                    }catch (Exception e){

                    }
                    mUdpClient.sendBytes(mServerAddr, mServerPort, mRequestConnect);
                    Pair<SocketAddress, String> res = mUdpClient.getResponse();
                    int cnt = 3;
                    while (res.first == null && cnt > 0) {
                        res = mUdpClient.getResponse();
                        cnt--;
                    }
                    if (res.first != null) {
                        Log.d(TAG, res.first.toString() + ":" + res.second);
                        mServerExactAddress = res.first.toString().split(":")[0].replace("/","");
                        mStream = true;
                        connectWebSocket();


                        try {
                            mServerAddr = InetAddress.getByName(mServerExactAddress);
                        }catch (Exception e){

                        }
                    }else{
                        Toast toast =
                                Toast.makeText(getApplicationContext(), "Cannot connect to ESP32 Camera", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                } else {
                    mStream = false;
                    mWebSocketClient.close();

                }*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                    }
                    else
                    {
                        //Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        //startActivityForResult(cameraIntent, CAMERA_REQUEST);
                        //startCamera();
                        capturePhoto();
                        Vehicle vehicle = new Vehicle("Pulser","XYZ123",
                                "https://www.google.com/maps/@" + latitude + "," + longitude +",17z" ,stor);
                        dao.add(vehicle);
                    }
                }
            }
        });
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());


    }

    /* ============================ Create Menu =================================== */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.about:

                startActivity(new Intent(this, About.class));
                return true;

            case R.id.exit:
                finishAffinity();
                //MainActivity.this.finish();
                System.exit(0);
                return true;

            default:

                return super.onOptionsItemSelected(item);

        }
    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[2048];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n') {
                        readMessage = new String(buffer, 0, bytes);
                        Log.e("Arduino Message", readMessage);
                        handler.obtainMessage(MESSAGE_READ, readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error", "Unable to send message", e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    /* ============================ Terminate Connection at BackPress ====================== */
    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        if (createConnectThread != null) {
            createConnectThread.cancel();
        }
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    /* ============================ SEND SMS ====================== */
    public void SendSMS() {
        if (strn != null) {

            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getlocation();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            }

            SmsManager smsManager = SmsManager.getDefault();
            String message = "Test happen at:" + " " + "http://maps.google.com?q="  + latitude + "," + longitude;
            String message2 = "Vehical no.: XYZ123, "+"Test happen at:" + "\n" +"https://www.google.com/maps/@" + latitude + "," + longitude +",17z";
            smsManager.sendTextMessage(strn, null, message2, null, null);
            Toast.makeText(getApplicationContext(), "Longitude:" +
                    Double.toString(longitude) + "\nLatitude:" +
                    Double.toString(latitude), Toast.LENGTH_SHORT).show();


        } else {
            Toast.makeText(getApplicationContext(), "Select contact", Toast.LENGTH_SHORT).show();
        }
    }

    private void getlocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClint.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this,
                                Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );
                        latitude = addresses.get(0).getLatitude();
                        longitude = addresses.get(0).getLongitude();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void safetodrive(){
        // Check conditions for safe drive
        final TextView Safe_To_Drive = findViewById(R.id.safedriveview);
        if (HELMET == true && CHINSTRAP == true && ALCOHOL == true && DEVICECONN == true && strn != null) {
            Safe_To_Drive.setBackgroundColor(getResources().getColor(R.color.green));
            //buttontest.setVisibility(View.VISIBLE);
            //cmdText = "100";
            //connectedThread.write(cmdText);
        } else {

            //buttontest.setVisibility(View.INVISIBLE);
            if (DEVICECONN == true && strn == null) {
                Safe_To_Drive.setBackgroundColor(getResources().getColor(R.color.red));
                //cmdText = "99";
                //connectedThread.write(cmdText);
                //Toast.makeText(getApplicationContext(), "Select Contact", Toast.LENGTH_SHORT).show();
            } else {
                Safe_To_Drive.setBackgroundColor(getResources().getColor(R.color.red));
                //Toast.makeText(getApplicationContext(), "Module is Offline", Toast.LENGTH_SHORT).show();
            }

        }
    }

    /* ============================ Select Contact ====================== */
    private void pickContact() {
        Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        pickContact.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(pickContact, PICK_CONTACT_REQUEST_CODE);
    }

    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT_REQUEST_CODE && data != null) {
            Uri contactData = data.getData();
            if (contactData != null) {
                Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    strn = cursor.getString(phoneIndex);
                    //txtContacts2.setText(num);



                    int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    strc = cursor.getString(nameIndex);
                    final TextView txtContacts = findViewById(R.id.txt_contact_name);
                    txtContacts.setText(strc);

                    @SuppressLint("Range") String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

                    for (String column : cursor.getColumnNames()) {
                        Log.i(TAG, "ContactPicked column " + column + " : " + cursor.getString(cursor.getColumnIndex(column)));
                    }
                    //Close cursor to prevent from memory leak
                    cursor.close();
                }
            }
        }

        /*if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
        }
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
        }*/
    }

    /* ============================ Connect esp 32 cam websocket ======================
    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://"+mServerExactAddress+":86/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.d("Websocket", "Open");
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.d("Websocket", "Closed " + s);
            }

            @Override
            public void onMessage(String message){
                Log.d("Websocket", "Receive");
            }

            @Override
            public void onMessage(ByteBuffer message){
//                Log.d("Websocket", "Receive");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] imageBytes= new byte[message.remaining()];
                        message.get(imageBytes);
                        final Bitmap bmp= BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.length);
                        if (bmp == null)
                        {
                            return;
                        }
                        int viewWidth = mServerImageView.getWidth();
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        final Bitmap bmp_traspose = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true );
                        float imagRatio = (float)bmp_traspose.getHeight()/(float)bmp_traspose.getWidth();
                        int dispViewH = (int)(viewWidth*imagRatio);
                        mServerImageView.setImageBitmap(Bitmap.createScaledBitmap(bmp_traspose, viewWidth, dispViewH, false));

                        mBitmapGrab = bmp;

                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.d("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }*/

    /* ============================ Smartphone camera interface ====================== */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }

    }


    private boolean AllPermissionGranted(){

        for (String permission : REQUIRED_PERMISSION){
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        Preview preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image capture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        // Video capture use case
        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .build();

        // Image analysis use case

        //bind to lifecycle:
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture, videoCapture);
    }

    private void capturePhoto() {

        long timestamp = System.currentTimeMillis();
        stor = "XYZ123_"+timestamp;
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, stor);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (stor2==null){
            stor2=stor;
        }

        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                        Toast.makeText(MainActivity.this, "Photo has been saved successfully.", Toast.LENGTH_SHORT).show();

                        uploadImage();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(MainActivity.this, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );


    }

    private void uploadImage()
    {

        if (stor != null) {
            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            Uri file = Uri.fromFile(new File("storage/emulated/0/Pictures/" + stor + ".jpg"));
            StorageReference riversRef = storageRef.child("Pictures/"+file.getLastPathSegment());
            uploadTask = riversRef.putFile(file);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    progressDialog.dismiss();
                    Toast
                            .makeText(MainActivity.this,
                                    "Failed " + exception.getMessage(),
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // Image uploaded successfully
                    // Dismiss dialog
                    progressDialog.dismiss();
                    Toast
                            .makeText(MainActivity.this,
                                    "Image Uploaded!!",
                                    Toast.LENGTH_SHORT)
                            .show();

                }
            }).addOnProgressListener(
                    new OnProgressListener<UploadTask.TaskSnapshot>() {

                        // Progress Listener for loading
                        // percentage on the dialog box
                        @Override
                        public void onProgress(
                                UploadTask.TaskSnapshot taskSnapshot) {
                            double progress
                                    = (100.0
                                    * taskSnapshot.getBytesTransferred()
                                    / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage(
                                    "Uploaded "
                                            + (int) progress + "%");
                        }
                    });

        }
        stor2=stor;

    }

    private void sensorcapture(){

        Helmet = findViewById(R.id.helmetview1);
        Chinstrap = findViewById(R.id.chinstrapview1);
        Alcohol = findViewById(R.id.alcoholview1);
        Helmet2 = findViewById(R.id.helmetview2);
        Chinstrap2 = findViewById(R.id.chinstrapview2);
        Alcohol2 = findViewById(R.id.alcoholview2);


                                    switch (arduinoMsg.toLowerCase()) {

                                        case "ht2":
                                            textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                            Helmet.setBackgroundColor(getResources().getColor(R.color.green));
                                            Helmet2.setBackgroundColor(getResources().getColor(R.color.green));
                                            HELMET = true;
                                            break;
                                        case "hf2":
                                            textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                            Helmet.setBackgroundColor(getResources().getColor(R.color.red));
                                            Helmet2.setBackgroundColor(getResources().getColor(R.color.red));
                                            HELMET = false;
                                            break;
                                        case "st2":
                                            textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                            Chinstrap.setBackgroundColor(getResources().getColor(R.color.green));
                                            Chinstrap2.setBackgroundColor(getResources().getColor(R.color.green));
                                            CHINSTRAP = true;
                                            break;
                                        case "sf2":
                                            textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                            Chinstrap.setBackgroundColor(getResources().getColor(R.color.red));
                                            Chinstrap2.setBackgroundColor(getResources().getColor(R.color.red));
                                            CHINSTRAP = false;
                                            break;
                                        case "at2":
                                            textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                            Alcohol.setBackgroundColor(getResources().getColor(R.color.green));
                                            Alcohol2.setBackgroundColor(getResources().getColor(R.color.green));
                                            ALCOHOL = true;
                                            break;
                                        case "af2":
                                            textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                            Alcohol.setBackgroundColor(getResources().getColor(R.color.red));
                                            Alcohol2.setBackgroundColor(getResources().getColor(R.color.red));
                                            ALCOHOL = false;
                                            break;
                                        case "acc":
                                            textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                            SendSMS();
                                            capturePhoto();
                                            Vehicle vehicle = new Vehicle("Pulser","XYZ123",
                                                    "https://www.google.com/maps/@" + latitude + "," + longitude +",17z",stor);
                                            dao.add(vehicle);
                                            HELMET = false;
                                            CHINSTRAP = false;
                                            ALCOHOL = false;
                                            break;


                                        case "ht1":
                                            textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                            Helmet.setBackgroundColor(getResources().getColor(R.color.green));
                                            Helmet2.setBackgroundColor(getResources().getColor(R.color.gray));
                                            HELMET = true;
                                            break;
                                        case "hf1":
                                            textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                            Helmet.setBackgroundColor(getResources().getColor(R.color.red));
                                            Helmet2.setBackgroundColor(getResources().getColor(R.color.gray));
                                            HELMET = false;
                                            break;
                                        case "st1":
                                            textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                            Chinstrap.setBackgroundColor(getResources().getColor(R.color.green));
                                            Chinstrap2.setBackgroundColor(getResources().getColor(R.color.gray));
                                            CHINSTRAP = true;
                                            break;
                                        case "sf1":
                                            textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                            Chinstrap.setBackgroundColor(getResources().getColor(R.color.red));
                                            Chinstrap2.setBackgroundColor(getResources().getColor(R.color.gray));
                                            CHINSTRAP = false;
                                            break;
                                        case "at1":
                                            textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                            Alcohol.setBackgroundColor(getResources().getColor(R.color.green));
                                            Alcohol2.setBackgroundColor(getResources().getColor(R.color.gray));
                                            ALCOHOL = true;
                                            break;
                                        case "af1":
                                            textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                            Alcohol.setBackgroundColor(getResources().getColor(R.color.red));
                                            Alcohol2.setBackgroundColor(getResources().getColor(R.color.gray));
                                            ALCOHOL = false;
                                            break;
                                        default:
                                   /* Helmet.setBackgroundColor(getResources().getColor(R.color.red));
                                    Helmet2.setBackgroundColor(getResources().getColor(R.color.red));
                                    Chinstrap.setBackgroundColor(getResources().getColor(R.color.red));
                                    Chinstrap2.setBackgroundColor(getResources().getColor(R.color.red));
                                    Alcohol.setBackgroundColor(getResources().getColor(R.color.red));
                                    Alcohol2.setBackgroundColor(getResources().getColor(R.color.red));*/
                                            break;


                        }



    }



}