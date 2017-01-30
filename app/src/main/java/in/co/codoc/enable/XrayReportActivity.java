package in.co.codoc.enable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

public class XrayReportActivity extends AppCompatActivity implements OnTaskCompleted {
    ImageView xrayImageView;
    IconTextView clinicalInfo;
    IconTextView responseView;
    String report_id;
    String user_id ;
    String key ;
    ProgressDialog pd;
    String clinicalInfoStr;
    String expertResponse;
    String image_url;
    Boolean is_accepted;
    IconTextView heading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xray_report);
        user_id = (PreferenceManager.getDefaultSharedPreferences(this).getString("user_id", null));
        key = (PreferenceManager.getDefaultSharedPreferences(this).getString("key", null));
        xrayImageView = (ImageView) findViewById(R.id.xrayImageView);
        clinicalInfo = (IconTextView) findViewById(R.id.clinical_info );
        responseView = (IconTextView) findViewById(R.id.response );
        heading = (IconTextView) findViewById(R.id.heading );
        getIntentData();
        getReportDetailsApicall();
    }

    void getIntentData(){
        Intent i = getIntent();
        report_id = i.getStringExtra("report_id");

    }
    void getReportDetailsApicall(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("report_id", report_id);
            obj.put("user_id",user_id);
            URL url = new URL(Constants.BASE_URL+"/reports/getReportOnId");
            JsonAsyncTask task = new JsonAsyncTask(this,url,key,true);
            pd = ProgressDialog.show(XrayReportActivity.this, "", "Loading...");
            task.execute(obj.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void onTaskCompleted(String response) {
        pd.dismiss();
        if (response != null) {
            try {
                JSONObject resultJson = new JSONObject(response);
                System.out.println("response" + response);
                boolean success = resultJson.getBoolean("success");
                if (success) {
                    JSONArray resultarray = resultJson.getJSONArray("result");
                    JSONObject result = resultarray.getJSONObject(0);
                    clinicalInfoStr = result.getString("note_to_expert");
                    image_url = result.getString("image_url");
                    expertResponse = result.getString("result");
                    is_accepted = result.getBoolean("is_accepted");
                    setData();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            //null response from server
        }
    }
    void setData(){
        Picasso.with(this)
                .load(image_url)
                .placeholder(R.drawable.loading)
                .into(xrayImageView);
        heading.setText("XRAY #"+report_id);
        clinicalInfo.setText(clinicalInfoStr);
        if(is_accepted) {
            responseView.setText(expertResponse);

        }else{
            responseView.setText("Expert has not yet reviewed the report");
        }
    }
    public void back(View v){
        finish();
    }

}
