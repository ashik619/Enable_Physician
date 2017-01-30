package in.co.codoc.enable;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class PaymentSuccesfullScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_succesfull_screen);ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Drawable success = getResources().getDrawable(R.drawable.payment_sucess);
        Drawable failure = getResources().getDrawable(R.drawable.payment_failure);
        Intent i = getIntent();
        boolean status = i.getBooleanExtra("status",false);
        if(status){
            imageView.setImageDrawable(success);
        }else {
            imageView.setImageDrawable(failure);
        }
    }
    @Override
    public void onBackPressed() {
        Intent in1 = new Intent(this, DetailsActivity.class);
        startActivity(in1);
    }
}
