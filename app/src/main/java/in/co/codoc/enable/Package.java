package in.co.codoc.enable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ashik619 on 27-12-2016.
 */
public class Package {
    public int no_reports;
    public int rate;
    public boolean isClicked;
    public Package(JSONObject pack){
        try {
            this.no_reports = pack.getInt("no_of_reports");
            this.rate = pack.getInt("rate");
            this.isClicked = false;
        }catch (JSONException e){}
    }
    public static ArrayList<Package> fromJson(JSONArray jsonObjects) {
        ArrayList<Package> packages = new ArrayList<Package>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                packages.add(new Package(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return packages;
    }

}