package com.khipu.android.demo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class MainActivity extends Activity {


    //ESTA INFORMACION SE USA PARA CREAR EL PAGO, DEBERIA HACERSE EN EL SERVIDOR
    private static final long ID_DEL_COBRADOR = 2581;
    private static final String SECRET_DEL_COBRADOR = "0ce9d2d383eb387ddf757fcd9eac8e4c53ee2c1f";


    public static final String KHIPU_URI = "KHIPU-URI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Uri data = getIntent().getData();

        if (data != null && data.getScheme().equals("khipuinstalled")) {

            //SE BUSCA LA URI DE KHIPU QUE ESTABA POR EJECUTARSE CUANDO SE ENVIO AL USUARIO A INSTALAR
            String storedKhipuUri = getSharedPreferences(KHIPU_URI, Context.MODE_PRIVATE).getString(KHIPU_URI, "");
            if (!storedKhipuUri.equals("")) {
                SharedPreferences.Editor editor = getSharedPreferences(KHIPU_URI, Context.MODE_PRIVATE).edit();
                editor.remove(KHIPU_URI);
                editor.commit();
                Intent khipuIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(storedKhipuUri));
                startActivity(khipuIntent);
            }
        } else if (data != null && data.getScheme().equals("khipudemo")) {

            CharSequence text = "";
            if ("cancel.khipu.com".equals(data.getAuthority())) {
                text = "El usuario rechazó el pago";
            } else if ("success.khipu.com".equals(data.getAuthority())) {
                text = "El pago se está verificando";
            }
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
        }
    }


    public void doPay(View view) {


        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... params) {
                return createPayment();
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                try {
                    Intent khipuIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(jsonObject.get("mobile-url").toString()));
                    if (getPackageManager().queryIntentActivities(khipuIntent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
                        startActivity(khipuIntent);
                    } else {

                        //SE GUARDA LA URI QUE SE ESTABA INTENTANDO EJECUTAR PARA CONTINUAR LUEGO QUE KHIPU SE INSTALE
                        SharedPreferences.Editor editor = getSharedPreferences(KHIPU_URI, Context.MODE_PRIVATE).edit();
                        editor.putString(KHIPU_URI, jsonObject.get("mobile-url").toString());
                        editor.commit();


                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.khipu.android")));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();


    }


    private JSONObject createPayment() {

        //ESTO SE DEBE HACER EN EL SERVIDOR, NO EN LA APP, ESTA HECHO AQUI POR SIMPLICIDAD DE LA DEMO

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("https://khipu.com/api/1.2/createPaymentURL");
        TextView subjectView = (TextView) this.findViewById(R.id.subject);
        TextView amountView = (TextView) this.findViewById(R.id.amount);
        TextView payerView = (TextView) this.findViewById(R.id.payer);
        try {
            String receiver_id = "" + ID_DEL_COBRADOR;
            String subject = subjectView.getText().toString();
            String body = "";
            String amount = amountView.getText().toString();
            String notify_url = "";
            String return_url = "khipudemo://success.khipu.com";
            String cancel_url = "khipudemo://cancel.khipu.com";
            String transaction_id = "";
            String payer_email = payerView.getText().toString();
            String bank_id = "";
            String picture_url = "";
            String custom = "";


            String concatenated = "receiver_id=" + receiver_id +
                    "&subject=" + subject +
                    "&body=" + body +
                    "&amount=" + amount +
                    "&payer_email=" + payer_email +
                    "&bank_id=" + bank_id +
                    "&transaction_id=" + transaction_id +
                    "&custom=" + custom +
                    "&notify_url=" + notify_url +
                    "&return_url=" + return_url +
                    "&cancel_url=" + cancel_url +
                    "&picture_url=" + picture_url;


            String hash = hmacSHA256(concatenated, SECRET_DEL_COBRADOR);


            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("receiver_id", receiver_id));
            nameValuePairs.add(new BasicNameValuePair("subject", subject));
            nameValuePairs.add(new BasicNameValuePair("body", body));
            nameValuePairs.add(new BasicNameValuePair("amount", amount));
            nameValuePairs.add(new BasicNameValuePair("notify_url", notify_url));
            nameValuePairs.add(new BasicNameValuePair("return_url", return_url));
            nameValuePairs.add(new BasicNameValuePair("cancel_url", cancel_url));
            nameValuePairs.add(new BasicNameValuePair("transaction_id", transaction_id));
            nameValuePairs.add(new BasicNameValuePair("payer_email", payer_email));
            nameValuePairs.add(new BasicNameValuePair("bank_id", bank_id));
            nameValuePairs.add(new BasicNameValuePair("picture_url", picture_url));
            nameValuePairs.add(new BasicNameValuePair("custom", custom));
            nameValuePairs.add(new BasicNameValuePair("hash", hash));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);


            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            for (String line = null; (line = reader.readLine()) != null; ) {
                builder.append(line).append("\n");
            }

            JSONObject object = (JSONObject) new JSONTokener(builder.toString()).nextValue();
            return object;

        } catch (IOException e) {

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static String hmacSHA256(String baseString, String keyString) {

        SecretKey secretKey = null;
        byte[] keyBytes = keyString.getBytes();
        secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            mac.init(secretKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


        byte[] text = baseString.getBytes();
        return byteToHex(mac.doFinal(text));
    }


}
