package au.gov.australia.media.mrs_android.mediareleaseservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import au.gov.australia.media.mrs_android.mediareleaseservice.helper.JsonMediaReleaseRequestHelper;


public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JsonMediaReleaseRequestHelper helper = new JsonMediaReleaseRequestHelper(getApplicationContext());
        helper.execute(Constants.BASE_URL + "/api/mediareleases");
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }

}
