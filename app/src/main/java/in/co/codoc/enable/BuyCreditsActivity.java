package in.co.codoc.enable;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

public class BuyCreditsActivity extends AppCompatActivity implements OnTaskCompleted,GetOnTaskCompleted,PaymentResultListener {
    IconTextView no_reportsView;
    EditText amountView;
    VariableListener total;
    boolean packageFlag;
    IconTextView totalView;
    int amount_total = 0;
    int package_total = 0;
    String user_id;
    String key;
    ProgressDialog pd;
    int no_reports;
    Boolean paymentFlag = false;
    ListView packageListview;
    Button pay;
    int totalpay = 0;
    String totalPayStr = null;
    String APP_CODE = "RH";
    String email;
    int type;
    UsersAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_credits);
       // ViewGroup head = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.listview_header_layout,null);
        View head=View.inflate(getApplicationContext(),R.layout.listview_header_layout,null);
        no_reportsView = (IconTextView) findViewById(R.id.report_balance);
        user_id = (PreferenceManager.getDefaultSharedPreferences(this).getString("user_id", null));
        key = (PreferenceManager.getDefaultSharedPreferences(this).getString("key", null));
        email = (PreferenceManager.getDefaultSharedPreferences(this).getString("orginalemail",null));
        type = (PreferenceManager.getDefaultSharedPreferences(this).getInt("type",9));
        amountView = (EditText) head.findViewById(R.id.amount);
        totalView = (IconTextView) findViewById(R.id.total);
        packageListview = (ListView) findViewById(R.id.list_view1);
        packageListview.addHeaderView(head,null,false);
        packageListview.setAdapter(null);
        pay = (Button) findViewById(R.id.payButton);
        total = new VariableListener();
        amountView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
                //System.out.println("before" + s);
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                packageFlag  = false;
                if(s.toString().equals("")) {
                    amount_total=0;
                    total.setTotal(amount_total + package_total);
                }else {
                    amount_total = Integer.parseInt(s.toString());
                    total.setTotal(amount_total + package_total);
                }
            }});
        total.setListener(new VariableListener.ChangeListener() {
            @Override
            public void onChange() {
                System.out.println("variable change"+total.getTotal());
                totalView.setText(String.valueOf(total.getTotal()));
            }
        });
        pay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                payment();
            }
        });
        packageListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                System.out.println("Fucked");
                Package selectedPackage = (Package) packageListview.getItemAtPosition(position);
                if (selectedPackage.isClicked) {
                    selectedPackage.isClicked = false;
                    totalpay = selectedPackage.rate;
                    int temp = total.getTotal();
                    changeAmount(String.valueOf(temp-totalpay));
                    total.setTotal(temp-totalpay);

                } else {
                    selectedPackage.isClicked = true;
                    totalpay = selectedPackage.rate;
                    int temp = total.getTotal();
                    changeAmount(String.valueOf(totalpay+temp));
                    total.setTotal(totalpay+temp);
                }
                adapter.notifyDataSetChanged();

            }
        });
        getWalletAmountApiCall();
    }
    void changeAmount(String amt){
        amountView.setText(amt, TextView.BufferType.EDITABLE);
    }
    public void back(View v){
        finish();
    }
    void getWalletAmountApiCall() {
        try {
            JSONObject prof = new JSONObject();
            prof.put("user_id",user_id);
            URL url = new URL(Constants.BASE_URL+"/users/getWalletAmt");
            JsonAsyncTask task = new JsonAsyncTask(this,url,key,true);
            pd = ProgressDialog.show(BuyCreditsActivity.this, "", "Loading");
            task.execute(prof.toString());
        }catch (Exception e){
            System.out.println("cant send");
        }
    }
    @Override
    public void onTaskCompleted(String response) {
        // Just showing the response in a Toast message
        pd.dismiss();
        if(response != null) {
            System.out.println("task completed");
            try {
                JSONObject resultJson = new JSONObject(response);
                //System.out.println("result"+resultJson.get("success"));
                System.out.println("response" + response);
                boolean success = resultJson.getBoolean("success");
                if (success) {
                    if(paymentFlag){
                        if((resultJson.getInt("msg"))==7){
                            Intent in1 = new Intent(this, PaymentSuccesfullScreen.class);
                            in1.putExtra("status",true);
                            startActivity(in1);
                        } else if((resultJson.getInt("msg"))==7){
                            Intent in1 = new Intent(this, PaymentSuccesfullScreen.class);
                            in1.putExtra("status",false);
                            startActivity(in1);
                        }
                    }else {
                        getPackages();
                        no_reports = resultJson.getInt("result");
                        PreferenceManager.getDefaultSharedPreferences(BuyCreditsActivity.this).edit()
                                .putInt("wallet_amt", no_reports).apply();
                        no_reportsView.setText("" + no_reports);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();

            }
        }else {
            if(paymentFlag){
                Intent in1 = new Intent(this, PaymentSuccesfullScreen.class);
                in1.putExtra("status",false);
                startActivity(in1);
            }
            Toast.makeText(getApplicationContext(),"Oops!.." , Toast.LENGTH_SHORT).show();
        }
    }
    void getPackages(){
        try {
            String baseUrl1 = Constants.BASE_URL+"/users/getpackages?user_id="+Constants.EXPERT_USER_ID;
            URL url = new URL(baseUrl1);
            GetJsonAsyncTask task2 = new GetJsonAsyncTask(this,url);
            task2.execute("");
        }catch (Exception e){
        }
    }
    @Override
    public void GetOnTaskCompleted(String response) {
        if(response != null) {
            try {
                System.out.println(response);
                final JSONObject responseJson = new JSONObject(response);
                Boolean success = responseJson.getBoolean("success");
                if (success) {
                    final JSONArray packagesJsonArray = responseJson.getJSONArray("result");
                    ArrayList<Package> packages = Package.fromJson(packagesJsonArray);
                    adapter = new UsersAdapter(this, packages);
                    packageListview.setAdapter(adapter);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    void payment(){
        totalpay = total.getTotal();
        if ((totalpay >= 50) && (totalpay <= 10000)) {
            if (totalpay % 50 == 0) {
                totalPayStr = String.valueOf(totalpay * 100);
                final Dialog dialog = new Dialog(this);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.payment_dialog);
                dialog.show();
                TextView textView = (TextView) dialog.findViewById(R.id.textView);
                textView.setText("Pay INR "+totalpay);
                Button okbutton = (Button) dialog.findViewById(R.id.okbutton);
                okbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPayment(totalPayStr);
                        dialog.dismiss();
                    }
                });
            }else {
                Toast.makeText(getApplicationContext(),"Enter amount which is multiple of 50" , Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(getApplicationContext(),"Enter amount between 50 and 10000" , Toast.LENGTH_LONG).show();
        }
    }
    public void startPayment(String totalPayStr) {
        Checkout checkout = new Checkout();
        checkout.setImage(R.drawable.app_logo);
        final Activity activity = this;
        try {
            JSONObject options = new JSONObject();
            options.put("name", "Dr Niraj Yadav");
            String orderId = createOrderId();
            options.put("description", orderId);
            options.put("currency", "INR");
            /**
             * Amount is always passed in PAISE
             * Eg: "500" = Rs 5.00
             */
            options.put("amount", totalPayStr);
            if(type == 0) {
                JSONObject prefill = new JSONObject();
                if (email != null) {
                    prefill.put("email", email);
                }
                options.put("prefill", prefill);
            }
            checkout.open(activity, options);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        System.out.println("payment ssuccesfull");
        System.out.println("pay id "+razorpayPaymentID);
        paymentFlag = true;
        paymentSuccessApi(razorpayPaymentID);
        /**
         * Add your logic here for a successfull payment response
         */
    }

    @Override
    public void onPaymentError(int code, String response) {
        System.out.println(response);
        Intent in1 = new Intent(this, PaymentSuccesfullScreen.class);
        in1.putExtra("status",false);
        startActivity(in1);
        /**
         * Add your logic here for a failed payment response
         */
    }
    void paymentSuccessApi(String id){
        try {

            JSONObject prof = new JSONObject();
            prof.put("user_id", user_id);
            prof.put("rate",String.valueOf(totalpay));
            prof.put("trans_id",id);
            prof.put("transaction_type","razorpay");
            prof.put("transaction_mode","NetBanking");
            prof.put("expert_id", Constants.EXPERT_USER_ID);
            URL url = new URL(Constants.BASE_URL+"/users/UpdateWallet");
            JsonAsyncTask task = new JsonAsyncTask(this, url, key, true);
            pd = ProgressDialog.show(BuyCreditsActivity.this, "", "Loading");
            task.execute(prof.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    String createOrderId(){
        Long time= System.currentTimeMillis();
        String timehex = Long.toHexString(time);
        String orderId = APP_CODE+timehex;
        return orderId;
    }


    public class UsersAdapter extends ArrayAdapter<Package> {
        public UsersAdapter(Context context, ArrayList<Package> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            final Package aPackage = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.package_list_item, parent, false);
            }
            RelativeLayout btn = (RelativeLayout) convertView.findViewById(R.id.btn);
            TextView tvRate = (TextView) convertView.findViewById(R.id.rate);
            TextView tvNoOfReports = (TextView) convertView.findViewById(R.id.no_reports);
            TextView tvRsLogo = (TextView) convertView.findViewById(R.id.rs);
            // Populate the data into the template view using the data object
            tvRate.setText(""+aPackage.rate);
            tvNoOfReports.setText(""+aPackage.no_reports+" Reports");
            if (aPackage.isClicked){
                btn.setBackgroundResource(R.color.colorPrimary);
                tvRate.setTextColor(getResources().getColor(R.color.white));
                tvNoOfReports.setTextColor(getResources().getColor(R.color.white));
                tvRsLogo.setTextColor(getResources().getColor(R.color.white));
            }else {
                btn.setBackgroundResource(R.color.white);
                tvRate.setTextColor(getResources().getColor(R.color.textcolor));
                tvNoOfReports.setTextColor(getResources().getColor(R.color.textcolor));
                tvRsLogo.setTextColor(getResources().getColor(R.color.textcolor));
            }
            return convertView;
        }

    }


}
