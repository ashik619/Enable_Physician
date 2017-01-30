package in.co.codoc.enable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportUploadActivity extends AppCompatActivity {
    String filename = null;
    ImageView imageView;
    String note;
    EditText noteView;
    ProgressDialog pd;
    String user_id_str;
    String pname = "x";
    String expert_id = Constants.EXPERT_USER_ID;
    String age = "45";
    String diagnosis = "x";
    String sex = "x";
    String history = "x";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_upload);
        imageView = (ImageView) findViewById(R.id.xrayImageView);
        noteView = (EditText) findViewById(R.id.note);
        user_id_str = (PreferenceManager.getDefaultSharedPreferences(this).getString("user_id", null));
        getIntentData();
    }
    public void back(View v){
        finish();
    }
    void getIntentData(){
        Intent i = getIntent();
        filename = i.getStringExtra("file");
        File image = new File(Constants.root+"/Rhythmia", filename);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        imageView.setImageBitmap(bitmap);
    }
    public void upload(View v){
        uploadReport();
    }
    void uploadReport(){
        note = noteView.getText().toString();
        if (note.matches("")) {
            note = "---";
        }
        pd = ProgressDialog.show(ReportUploadActivity.this, "", "Sending...");
        uploadReportApiCall();
    }
    void uploadReportApiCall(){
        FileUploadService service =
                ServiceGenerator.createService(FileUploadService.class);
        System.out.println("uri" + filename.toString());
        File file = new File(Constants.root + "/Rhythmia", filename);
        RequestBody user_id =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), user_id_str);
        RequestBody p_name =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), pname);
        RequestBody p_age =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), age);
        RequestBody p_gender =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), sex);
        RequestBody p_history =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), history);
        RequestBody diff_diagnosis =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), diagnosis);
        RequestBody clinical_info =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), note);
        RequestBody expert =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), expert_id);
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        Call<ResponseBody> profilePicCall = service.reportUpload(user_id,p_name,p_age,p_gender,p_history,diff_diagnosis, clinical_info,expert,body);
        profilePicCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                try {
                    System.out.println("respo"+response.body().string());
                    pd.dismiss();
                    Toast.makeText(getApplicationContext(), "Report Send", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), DetailsActivity.class);
                    startActivity(i);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                pd.dismiss();
                Toast.makeText(getApplicationContext(), "Report not sent, please try again", Toast.LENGTH_SHORT).show();

            }
        });
    }

}
