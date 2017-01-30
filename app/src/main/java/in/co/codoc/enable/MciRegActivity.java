package in.co.codoc.enable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.URL;

public class MciRegActivity extends AppCompatActivity implements OnTaskCompleted {
    EditText nameView;
    EditText MciRegNoView;
    String user_id;
    String key;
    String name;
    String image_url;
    String MciRegNo;
    ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mci_reg);
        nameView = (EditText) findViewById(R.id.name);
        MciRegNoView = (EditText) findViewById(R.id.mci_no);
        user_id = (PreferenceManager.getDefaultSharedPreferences(this).getString("user_id", null));
        key = (PreferenceManager.getDefaultSharedPreferences(this).getString("key", null));
        getIntentData();

    }

    void getIntentData() {
        Intent i = getIntent();
        image_url = i.getStringExtra("image_url");
        name = i.getStringExtra("name");
    }

    public void next(View v) {
        MciRegNo = MciRegNoView.getText().toString();
        name = nameView.getText().toString();
        if (MciRegNo.matches("")) {
            Toast.makeText(getApplicationContext(), "Enter Your MCI reg number", Toast.LENGTH_SHORT).show();
        } else if (name.matches("")) {
            Toast.makeText(getApplicationContext(), "Enter Your Name", Toast.LENGTH_SHORT).show();
        } else {
            mciRegApiCall();
        }

    }

    void mciRegApiCall() {
        try {

            JSONObject object = new JSONObject();
            object.put("user_id", user_id);
            object.put("mci_name", name);
            object.put("mci_no", MciRegNo);
            URL url = new URL(Constants.BASE_URL + "/users/initverification");
            JsonAsyncTask task = new JsonAsyncTask(this, url, key, false);
            pd = ProgressDialog.show(MciRegActivity.this, "", "Loading");
            task.execute(object.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTaskCompleted(String response) {
        // Just showing the response in a Toast message
        pd.dismiss();
        if (response != null) {
            try {
                JSONObject resultJson = new JSONObject(response);
                //System.out.println("result"+resultJson.get("success"));
                System.out.println("response" + response);
                boolean success = resultJson.getBoolean("success");
                if (success) {
                    start_update_profile_activity();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void start_update_profile_activity() {
        Intent in1 = new Intent(this, UpdateProfileActivity.class);
        in1.putExtra("name", name);
        in1.putExtra("flag", 0);
        in1.putExtra("image_url", image_url);
        startActivity(in1);
    }

    public void help(View v) {
        Uri uri = Uri.parse("http://www.mciindia.org/InformationDesk/IndianMedicalRegister.aspx");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}