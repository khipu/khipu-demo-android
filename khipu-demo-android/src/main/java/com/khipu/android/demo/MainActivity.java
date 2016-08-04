package com.khipu.android.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {


	private TextView subjectView;
	private TextView amountView;
	private TextView payerView;

	private static final String KHIPU_URI = "KHIPU-URI";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		subjectView = (TextView) this.findViewById(R.id.subject);
		amountView = (TextView) this.findViewById(R.id.amount);
		payerView = (TextView) this.findViewById(R.id.payer);
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
				return DemoHelper.createPayment(subjectView.getText().toString(), amountView.getText().toString(), payerView.getText().toString());
			}

			@Override
			protected void onPostExecute(JSONObject jsonObject) {
				try {
					Intent khipuIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(jsonObject.get("app_url").toString()));
					if (getPackageManager().queryIntentActivities(khipuIntent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
						startActivity(khipuIntent);
					} else {

						//SE GUARDA LA URI QUE SE ESTABA INTENTANDO EJECUTAR PARA CONTINUAR LUEGO QUE KHIPU SE INSTALE
						SharedPreferences.Editor editor = getSharedPreferences(KHIPU_URI, Context.MODE_PRIVATE).edit();
						editor.putString(KHIPU_URI, jsonObject.get("app_url").toString());
						editor.commit();


						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.khipu.android")));
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.execute();
	}


}
