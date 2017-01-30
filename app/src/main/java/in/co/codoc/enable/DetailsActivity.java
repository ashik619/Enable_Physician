package in.co.codoc.enable;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetailsActivity extends AppCompatActivity implements OnTaskCompleted{
    private FragmentTabHost mTabHost;
    DrawerLayout mDrawer;
    NavigationView nvDrawer;
    CircleImageView ppropic;
    IconTextView phyname;
    ImageButton menuButton;
    RelativeLayout navHeader;
    FloatingActionMenu fab;
    FloatingActionButton gallery, camera;
    private static final int RESULT_LOAD_IMG = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    File myDir;
    String capturedImageFileName = null;
    File capturedImageFile = null;
    String selectedImage = null;
    String user_id;
    String key;
    String profilename;
    int walletAmount;
    String profilImageUrl;
    IconTextView walletAmountView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        user_id = (PreferenceManager.getDefaultSharedPreferences(this).getString("user_id", null));
        key = (PreferenceManager.getDefaultSharedPreferences(this).getString("key", null));
        View referredView = LayoutInflater.from(DetailsActivity.this).inflate(R.layout.referred_tabhost_tabview,null);
        View pendingView = LayoutInflater.from(DetailsActivity.this).inflate(R.layout.pending_tabhost_tabview,null);
        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        mTabHost.addTab(mTabHost.newTabSpec("Referred").setIndicator(referredView),
                ReferredFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("Pending").setIndicator(pendingView),
                PendingFragment.class, null);
        mTabHost.setCurrentTab(1);
        mTabHost.getTabWidget().setDividerDrawable(R.drawable.tabwidget_divider);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        navHeader = (RelativeLayout) nvDrawer.getHeaderView(0).findViewById(R.id.navheader);
        ppropic = (CircleImageView ) nvDrawer.getHeaderView(0).findViewById(R.id.pprofilepicmenu );
        phyname = (IconTextView) nvDrawer.getHeaderView(0).findViewById(R.id.namemenu );
        walletAmountView = (IconTextView) nvDrawer.getHeaderView(0).findViewById(R.id.walletAmount );
        menuButton = (ImageButton) findViewById(R.id.menu);
        fab = (FloatingActionMenu) findViewById(R.id.fab);
        gallery = (FloatingActionButton) findViewById(R.id.gallery);
        camera = (FloatingActionButton) findViewById(R.id.camera);
        setupDrawerContent(nvDrawer);
        menuButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //setPhydata();
                mDrawer.openDrawer(GravityCompat.START);
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activeGallery();
                fab.close(true);
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activeTakePhoto();
                fab.close(true);
            }
        });
        navHeader.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MyProfileActivity.class);
                i.putExtra("user_id",user_id);
                startActivity(i);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        mDrawer.closeDrawers();
        getprofileApiCall();
    }
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }
    public void selectDrawerItem(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.about:

                break;
            case R.id.wallet:
                Intent in2 = new Intent(this, BuyCreditsActivity.class);
                startActivity(in2);

                break;
            case R.id.history:
                Intent in3 = new Intent(this, TransactionHistory.class);
                startActivity(in3);

                break;
            case R.id.signout:
                clseApplication();
                break;
            default:

        }


        //menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawer.closeDrawers();
    }

    private void activeTakePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    private void activeGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }
    @Override protected void onActivityResult(int requestCode, int resultCode,
                                              Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_LOAD_IMG:
                if (requestCode == RESULT_LOAD_IMG &&
                        resultCode == RESULT_OK && null != data) {
                    selectedImage = data.getData().toString();
                    startCrop(true);
                }
            case REQUEST_IMAGE_CAPTURE:
                if (requestCode == REQUEST_IMAGE_CAPTURE &&
                        resultCode == RESULT_OK) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    saveImgFile(photo);
                    startCrop(false);

                }
        }
    }
    void saveImgFile(Bitmap bm){
        myDir = new File(Constants.root + "/Rhythmia");
        myDir.mkdirs();
        capturedImageFileName = "ECG_image"+createRandomString()+".png";
        capturedImageFile = new File(myDir, capturedImageFileName);
        if (capturedImageFile.exists())
            capturedImageFile.delete();
        try {
            FileOutputStream out = new FileOutputStream(capturedImageFile);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            if(bm!=null)
            {
                bm.recycle();
                bm=null;
            }
            System.out.println(capturedImageFile.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    String createRandomString(){
        Long time= System.currentTimeMillis();
        String timehex = Long.toHexString(time);
        return timehex;
    }
    public void startCrop(boolean flag ){
        Intent i = new Intent(getApplicationContext(), CropActivity.class);
        if(!flag){
            i.putExtra("file",capturedImageFileName);
        }else {
            i.putExtra("file",selectedImage.toString());
        }
        i.putExtra("flag",flag);
        startActivity(i);

    }
    void getprofileApiCall() {
        try {
            JSONObject prof = new JSONObject();
            prof.put("user_id",user_id);
            URL url = new URL(Constants.BASE_URL+"/users/UpdatedProfile");
            JsonAsyncTask task = new JsonAsyncTask(this,url,key,true);
            task.execute(prof.toString());
        }catch (Exception e){
            System.out.println("cant send");
        }
    }
    @Override
    public void onTaskCompleted(String response) {
        // Just showing the response in a Toast message
        if(response != null) {
            try {
                JSONObject resultJson = new JSONObject(response);
                System.out.println("response" + response);
                boolean success = resultJson.getBoolean("success");
                if (success) {
                    JSONObject result = resultJson.getJSONObject("result");
                    if(result.has("name")) {
                        profilename = result.getString("name");
                        PreferenceManager.getDefaultSharedPreferences(this).edit()
                                .putString("name", profilename).apply();
                        if(profilename.matches("null")){
                            Intent intent = new Intent(this, UpdateProfileActivity.class);
                            intent.putExtra("flag",0);
                            startActivity(intent);
                        }else {

                        }
                    }else {
                        Intent intent = new Intent(this, UpdateProfileActivity.class);
                        intent.putExtra("flag",0);
                        startActivity(intent);
                    }
                    if(result.has("wallet_amt")) {
                        walletAmount = result.getInt("wallet_amt");
                        PreferenceManager.getDefaultSharedPreferences(DetailsActivity.this).edit()
                                .putInt("wallet_amt", walletAmount).apply();
                    }else {
                        walletAmount = 0;
                    }
                    profilImageUrl = result.getString("image_url");
                    setPhydata();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
           // loadSavedData();
        }
    }
    void setPhydata() {
        Picasso.with(this)
                .load(profilImageUrl)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(ppropic);

        if(profilename!= null) {
            phyname.setText("Dr. " + profilename);
        }
        if(walletAmount > 0) {
            walletAmountView.setText("" + walletAmount);
        }else {
            walletAmountView.setText("" + 0);
        }
    }
    private void clseApplication() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString("email",null).apply();
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString("password",null).apply();
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString("key",null).apply();
        this.finishAffinity();

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
