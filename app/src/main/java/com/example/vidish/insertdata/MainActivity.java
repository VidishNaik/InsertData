package com.example.vidish.insertdata;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public Spinner spinner;

    SubjectsAsyncTask subjectAsyncTask;

    Bundle mSavedInstanceState;

    String selectedItem="";

    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_main);
        activity=this;
        final RadioGroup radioGroup1 = (RadioGroup) findViewById(R.id.radiogroup_year);
        final RadioGroup radioGroup2 = (RadioGroup) findViewById(R.id.radiogroup_div);
        final RadioGroup radioGroup3 = (RadioGroup) findViewById(R.id.radiogroup_lec);
        final RadioGroup radioGroup4 = (RadioGroup) findViewById(R.id.radiogroup_batch);
        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearlayout);
        for (int i = 0; i < radioGroup2.getChildCount(); i++) {
            (radioGroup2.getChildAt(i)).setEnabled(false);
            (radioGroup3.getChildAt(i)).setEnabled(false);
        }
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setEnabled(false);
        Button proceed = (Button) findViewById(R.id.button_proceed);
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton r1 = (RadioButton) findViewById(checkedId);
                spinner.setAdapter(null);
                spinner.setEnabled(false);
                if (subjectAsyncTask != null)
                    subjectAsyncTask.cancel(true);
                subjectAsyncTask = new SubjectsAsyncTask();
                subjectAsyncTask.execute("http://192.168.0.3/getsubjects.php?year=" + r1.getText().toString());
                for (int i = 0; i < radioGroup2.getChildCount(); i++) {
                    (radioGroup2.getChildAt(i)).setEnabled(true);
                    (radioGroup3.getChildAt(i)).setEnabled(true);
                }

            }
        });
        radioGroup3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton r1= (RadioButton) findViewById(checkedId);
                if(r1.getText().toString().equals("Practical"))
                {
                    linearLayout.setVisibility(View.VISIBLE);
                }
                else if(r1.getText().toString().equals("Lecture"))
                {
                    linearLayout.setVisibility(View.GONE);
                }
            }
        });
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int radioButtonId1 = radioGroup1.getCheckedRadioButtonId();
                int radioButtonId2 = radioGroup2.getCheckedRadioButtonId();
                int radioButtonId3 = radioGroup3.getCheckedRadioButtonId();
                int radioButtonId4 = radioGroup4.getCheckedRadioButtonId();
                final RadioButton year = (RadioButton) findViewById(radioButtonId1);
                final RadioButton div = (RadioButton) findViewById(radioButtonId2);
                final RadioButton lec = (RadioButton) findViewById(radioButtonId3);
                final RadioButton batch = (RadioButton) findViewById(radioButtonId4);
                if (year == null) {
                    Toast.makeText(MainActivity.this, "Please select a Year", Toast.LENGTH_SHORT).show();
                } else if (div == null) {
                    Toast.makeText(MainActivity.this, "Please select a Division", Toast.LENGTH_SHORT).show();
                } else if (lec == null) {
                    Toast.makeText(MainActivity.this, "Please select Lecture/Practical", Toast.LENGTH_SHORT).show();
                } else if(spinner.getAdapter()==null || selectedItem == "" || selectedItem == null ) {
                    Toast.makeText(MainActivity.this, "Please select a Subject", Toast.LENGTH_SHORT).show();
                } else {
                    AttendanceAsyncTask attendanceAsyncTask = new AttendanceAsyncTask();
                    if(batch == null) {
                        attendanceAsyncTask.execute(CreateString(year.getText().toString(),div.getText().toString(),
                                "",selectedItem));
                    }
                    else {
                        attendanceAsyncTask.execute(CreateString(year.getText().toString(),div.getText().toString(),
                                batch.getText().toString(),selectedItem));
                    }
                }

            }
        });

    }

    private class SubjectsAsyncTask extends AsyncTask<String, Void, List<String>> {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected List<String> doInBackground(String... urls) {
            publishProgress();
            if (urls.length < 1 || urls[0] == null)
                return null;
            URL url;
            try {
                url = new URL(urls[0]);
            } catch (MalformedURLException exception) {
                Log.e("ClassSelector", "Error with creating URL", exception);
                return null;
            }
            String jsonResponse = "";
            publishProgress();
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
            }
            if (jsonResponse == null) {
                return null;
            }
            List<String> subjects = SubjectJSONParser.extractSubjects(jsonResponse);
            return subjects;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
            progressDialog.setTitle("Loading");
            progressDialog.setMessage("Please Wait");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(List<String> subjects) {
            progressDialog.dismiss();
            if (subjects == null)
                return;

            spinner = (Spinner) findViewById(R.id.spinner);
            spinner.setPrompt("Please select a subject...");
            spinner.setEnabled(true);
            // Creating adapter for spinner
            //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ClassSelector.this, subjects, R.layout.spinner_layout);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_layout, subjects);
            // Drop down layout style - list view with radio button
            adapter.setDropDownViewResource(R.layout.spinner_layout);

            // attaching data adapter to spinner
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(spinner.getAdapter() != null)
                        selectedItem = parent.getItemAtPosition(position).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

    }

    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            jsonResponse = readFromStream(inputStream);
        } catch (IOException e) {
            jsonResponse = null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private String CreateString(String year,String div,String batch,String subject)
    {
        String link = "http://172.16.19.164/getcount.php?table="+year+"%20"+div+
                ""+batch+"&subject=";
        String a[]=subject.split(" ");
        for(int i = 0; i<a.length;i++)
        {
            if(i == a.length-1)
                link = link + a[i];
            else
                link = link + a[i] + "%20";
        }
        link = link + "&record=";
        int count = 80;
        while(count != 0)
        {
            double number = Math.random();
            if( ( number >= 0 && number <= 0.50 ) || ( number >= 0.70 && number <= 0.90))
            {
                link = link + "P";
            }
            else
            {
                link = link + "A";
            }
            count--;
        }
        Log.v("LINK",link);
        return link;
    }

    private class AttendanceAsyncTask extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected String doInBackground(String... urls) {
            publishProgress();
            if (urls.length < 1 || urls[0] == null)
                return null;
            URL url;
            try {
                url = new URL(urls[0]);
            } catch (MalformedURLException exception) {
                Log.e("ClassSelector", "Error with creating URL", exception);
                return null;
            }
            String jsonResponse = "";
            publishProgress();
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
            }
            if (jsonResponse == null) {
                return null;
            }
            if(jsonResponse.length()>1)
            {
                return "0";
            }
            else {
                return jsonResponse;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
            progressDialog.setTitle("Loading");
            progressDialog.setMessage("Please Wait");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String string) {
                progressDialog.dismiss();
                startActivity(new Intent(MainActivity.this,MainActivity.class));
                finish();
        }

    }
}
