package in.co.codoc.enable;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

public class LoginActivity extends AppCompatActivity implements OnTaskCompleted,GetOnTaskCompleted,GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks{
    Button normalSignUp;
    Button normalLogin;
    EditText email_idView;
    EditText passwordView;
    String email_id;
    String password;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    CheckBox ch1;
    ProgressDialog pd;
    int signUpType;
    String key;
    String user_id;
    int msg;
    String status;
    boolean inactive;
    boolean fraud;
    String username;
    String profimageUrl;
    IconTextView loginSelector;
    LinearLayout checkboxLayout;
    boolean loginFlag = false;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 007;
    String googleFbId;
    boolean logoutFlag = false;
    int count = 0;
    boolean googleFlag;
    boolean facebookFlag;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        normalSignUp = (Button) findViewById(R.id.normal_signup_button);
        normalLogin = (Button) findViewById(R.id.normal_login_button);
        email_idView = (EditText) findViewById(R.id.email_id);
        passwordView = (EditText) findViewById(R.id.password);
        loginSelector = (IconTextView) findViewById(R.id.login_mode_view);
        checkboxLayout = (LinearLayout) findViewById(R.id.checkbox_layout);
        ch1 = (CheckBox) findViewById(R.id.checkbox);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        normalSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                normalSignUp();
            }
        });
        normalLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                normalLogin();
            }
        });
        loginSelector.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                login_mode();
            }
        });
    }
    void normalSignUp(){
        signUpType = 0;
        email_id = email_idView.getText().toString();
        password = passwordView.getText().toString();
        if (email_id.matches("")){
            Toast.makeText(getApplicationContext(), "Email cant be empty", Toast.LENGTH_LONG).show();
        }/*else if(!(email_id.matches(emailPattern))){
            Toast.makeText(getApplicationContext(), "Enter valid email", Toast.LENGTH_LONG).show();
        }*/ else if(password.matches("")){
            Toast.makeText(getApplicationContext(), "Enter Password", Toast.LENGTH_LONG).show();
        }else if(!ch1.isChecked()){
            Toast.makeText(getApplicationContext(), "Please accept the terms and conditions", Toast.LENGTH_LONG).show();
        }else {
            System.out.println("mail" + email_id + "pass" + password);
            signUpApiCAll("0");
        }
    }
    void normalLogin(){
        email_id = email_idView.getText().toString();
        password = passwordView.getText().toString();
        if (email_id.matches("")){
            Toast.makeText(getApplicationContext(), "Email cant be empty", Toast.LENGTH_LONG).show();
        } else if(password.matches("")){
            Toast.makeText(getApplicationContext(), "Enter Password", Toast.LENGTH_LONG).show();
        } else {
            signInApiCAll();
        }

    }
    void signUpApiCAll(String acc_type){
        try {
            URL url = new URL(Constants.BASE_URL+"/users/register");
            JsonAsyncTask task = new JsonAsyncTask(this,url,"",false);
            pd = ProgressDialog.show(LoginActivity.this, "", "Signing UP");
            task.execute(createSignUpJson(acc_type));
        }catch (Exception e){}
    }
    String createSignUpJson(String acc_type){
        JSONObject jsonObject = new JSONObject();
        try {
            if (acc_type.matches("0")) {
                signUpType = 0;
                jsonObject.put("acc_type", acc_type);
                jsonObject.put("email", email_id);
                jsonObject.put("passwd", password);
                jsonObject.put("is_expert", false);
                jsonObject.put("expert_id", Constants.EXPERT_USER_ID);
            }else if(acc_type.matches("1")){
                signUpType = 1;
                facebookFlag = true;
                jsonObject.put("acc_type", acc_type);
                jsonObject.put("fb_id", googleFbId);
                jsonObject.put("is_expert",false);
                jsonObject.put("expert_id",Constants.EXPERT_USER_ID);
            }
            else if(acc_type.matches("2")){
                googleFlag = true;
                signUpType = 2;
                jsonObject.put("acc_type", acc_type);
                jsonObject.put("gp_id", googleFbId);
                jsonObject.put("is_expert",false);
                jsonObject.put("expert_id",Constants.EXPERT_USER_ID);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
    void signInApiCAll() {
        try {
            URL url = new URL(Constants.BASE_URL+"/users/login");
            JsonAsyncTask task = new JsonAsyncTask(this,url,"",false);
            pd = ProgressDialog.show(LoginActivity.this, "", "Signing In");
            task.execute(createNormalLoginJson());
        }catch (Exception e){}
    }
    String createNormalLoginJson(){
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("email", email_id);
            jsonObject.put("passwd", password);
            jsonObject.put("is_expert", false);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
    public void googleSignIn(View v){
        googlesignIn();
    }
    private void googlesignIn() {
        System.out.println("google sign in");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        pd = ProgressDialog.show(LoginActivity.this, "", "Google Sign In");
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            pd.dismiss();
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        else {
           // callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    public void onConnectionSuspended(int arg0) {
        if(count<2) {
            mGoogleApiClient.connect();
            count = count+1;
        }else {
            pd.dismiss();
            Toast.makeText(getApplicationContext(), "Google sign in failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //SIGN OUT HERE
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if(logoutFlag) {
                            pd.dismiss();
                            System.out.println("Sigining out");
                            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                                    new ResultCallback<Status>() {
                                        @Override
                                        public void onResult(Status status) {
                                            revokeAccess();
                                        }
                                    });
                        }

                    }
                });
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        pd.dismiss();
        Toast.makeText(getApplicationContext(), "Google sign in failed", Toast.LENGTH_SHORT).show();
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        closeApplication();
                    }
                });
    }
    void closeApplication(){
        if(logoutFlag) {
            this.finishAffinity();
        }
    }
    private void handleSignInResult(GoogleSignInResult result) {

        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            username = acct.getDisplayName();
            if(acct.getPhotoUrl()!=null) {
                profimageUrl = acct.getPhotoUrl().toString();
            }
            email_id = acct.getEmail();
            googleFbId = acct.getId();
            System.out.println("acc id " + googleFbId);
            if(googleFbId != null) {
                if(loginFlag) {
                    googleFbSignInApiCall("2");
                }else {
                    email_id = googleFbId;
                    signUpApiCAll("2");
                }
            }
        } else {
            // Signed out, show unauthenticated UI.
            System.out.println("not sucess");
        }
    }
    void googleFbSignInApiCall(String acc_type){
        try {
            URL url = new URL(Constants.BASE_URL +"/users/login");
            JsonAsyncTask task = new JsonAsyncTask(this,url,"",false);
            pd = ProgressDialog.show(LoginActivity.this, "", "Signing In");
            task.execute(createGoogleFbLoginJson(acc_type).toString());
        }catch (Exception e){}
    }
    JSONObject createGoogleFbLoginJson(String acc_type){
        if(acc_type.matches("1")){
            signUpType = 1;
        }else if(acc_type.matches("2")){
            signUpType = 2;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("acc_type", acc_type);
            jsonObject.put("email", googleFbId);
            jsonObject.put("passwd", googleFbId);
            jsonObject.put("is_expert", false);
            System.out.println("sendin json"+jsonObject.toString());
            return jsonObject;
        }catch (JSONException e){
            return null;
        }

    }


    @Override
    public void onTaskCompleted(String response) {
        try {
            pd.dismiss();
            System.out.println("response"+response);
            if (response != null) {
                JSONObject resultJson = new JSONObject(response);
                //System.out.println("result"+resultJson.get("success"));
                if (resultJson.getBoolean("success")) {
                    key = resultJson.getString("key");
                    user_id = resultJson.getString("user_id");
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putString("user_id", user_id).apply();
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putString("password", password).apply();
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putString("key", key).apply();
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putString("orginalemail", email_id).apply();
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putInt("type", signUpType).apply();
                    if (resultJson.has("msg")) {
                        msg = resultJson.getInt("msg");
                        if(msg == 1) {
                            Toast.makeText(getApplicationContext(),"SignUp Successful Email verification link sent to your inbox. Please verify your account." , Toast.LENGTH_LONG).show();
                            start_mci_reg_activity();
                        }else if(msg == 2){
                            Toast.makeText(getApplicationContext(),"Login Successful" , Toast.LENGTH_LONG).show();
                            JSONObject user = resultJson.getJSONObject("users");
                            status = user.getString("status");
                            if(status.matches("inactive")){
                                inactive = true;
                            }else if(status.matches("active")){
                                inactive = false;
                            }
                            fraud = user.getBoolean("mstatus");
                            start_details_activity();
                        }
                    }
                } else {
                    if (resultJson.has("msg")) {
                        msg = resultJson.getInt("msg");
                        if(msg == 3) {
                            final Dialog dialog = new Dialog(this);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.signin_dialog);
                            dialog.show();
                            Button okbutton = (Button) dialog.findViewById(R.id.okbutton);
                            okbutton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    signUpApiCAll("0");
                                    dialog.dismiss();
                                }
                            });
                        }else if(msg == 0){
                            final Dialog dialog = new Dialog(this);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.signup_dialog);
                            dialog.show();
                            Button okbutton = (Button) dialog.findViewById(R.id.okbutton);
                            okbutton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    signInApiCAll();
                                    dialog.dismiss();
                                }
                            });

                        }
                        else if(msg == 6){
                            Toast.makeText(getApplicationContext(),"Please check the password" , Toast.LENGTH_LONG).show();
                        }else if(msg == 5){
                            if(signUpType == 2){
                                //googleFbSignInApiCall("2");
                            }else if(signUpType == 1){
                                //googleFbSignInApiCall("1");
                            }
                        }
                    }else if (resultJson.has("res")){
                        msg = resultJson.getInt("res");
                        if(msg == 3) {
                            final Dialog dialog = new Dialog(this);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.signin_dialog);
                            dialog.show();
                            Button okbutton = (Button) dialog.findViewById(R.id.okbutton);
                            okbutton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    signUpApiCAll("0");
                                    dialog.dismiss();
                                }
                            });                        }
                    }
                }
            }else {
                final Dialog dialog = new Dialog(this);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.no_internet_dialog);
                dialog.show();
                Button databutton = (Button) dialog.findViewById(R.id.data);
                Button wifibutton = (Button) dialog.findViewById(R.id.wifi);
                databutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setComponent(new ComponentName("com.android.settings",
                                "com.android.settings.Settings$DataUsageSummaryActivity"));
                        dialog.dismiss();
                        startActivity(intent);
                    }
                });
                wifibutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(Settings.ACTION_WIFI_SETTINGS);
                        dialog.dismiss();
                        startActivity(intent);

                    }
                });
            }
        }catch (JSONException e)
        {   pd.dismiss();
            e.printStackTrace();
        }
    }
    void start_mci_reg_activity(){
        Intent in1 = new Intent(this, MciRegActivity.class);
        in1.putExtra("email_id", email_id);
        in1.putExtra("name", username);
        in1.putExtra("image_url",profimageUrl);
        in1.putExtra("flag",0);
        startActivity(in1);
    }
    void start_details_activity(){
        Intent in1 = new Intent(this, DetailsActivity.class);
        in1.putExtra("inactive",inactive);
        in1.putExtra("fraud",fraud);
        startActivity(in1);
    }
    public void login_mode(){
        System.out.println("mode login");
        if(loginFlag == false) {
            loginFlag = true;
            loginSelector.setText("Did you Signup first?Click here to SignUp");
            normalSignUp.setVisibility(View.GONE);
            normalLogin.setVisibility(View.VISIBLE);
            checkboxLayout.setVisibility(View.INVISIBLE);
        }else {
            loginFlag = false;
            loginSelector.setText("Already have an account?Click here to Login");
            normalSignUp.setVisibility(View.VISIBLE);
            normalLogin.setVisibility(View.GONE);
            checkboxLayout.setVisibility(View.VISIBLE);
        }
    }
    public void forgetpassword(View v){
        final Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.forgotpassword_dialog);
        dialog.show();
        Button okbutton = (Button) dialog.findViewById(R.id.okbutton);
        final EditText email_forgot = (EditText) dialog.findViewById(R.id.email_forgot);
        email_id = email_idView.getText().toString();
        if(!(email_id.matches(""))){
            email_forgot.setText(email_id, TextView.BufferType.EDITABLE);
        }
        okbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_forgot_str = email_forgot.getText().toString();
                if(email_forgot_str.equals("")){
                    Toast.makeText(getApplicationContext(), "Enter your Email id", Toast.LENGTH_LONG).show();
                }else {
                    forgotPasswordApiCall(email_forgot_str);
                    dialog.dismiss();

                }
            }
        });
    }
    void forgotPasswordApiCall(String email){
        try {
            String baseUrl1 = Constants.BASE_URL+"/users/forgotpassword?email="+email;
            URL url = new URL(baseUrl1);
            GetJsonAsyncTask task2 = new GetJsonAsyncTask(this,url);
            pd = ProgressDialog.show(LoginActivity.this, "", "Please wait");
            task2.execute("");
        }catch (Exception e){
        }
    }
    @Override
    public void GetOnTaskCompleted(String response) {
        pd.dismiss();
        System.out.println(response);
        if(response != null) {
            try {
                System.out.println(response);
                final JSONObject responseJson = new JSONObject(response);
                Boolean success = responseJson.getBoolean("success");
                if (success) {
                    Toast.makeText(LoginActivity.this,"Please check your inbox", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            this.finishAffinity();
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click back again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

}
