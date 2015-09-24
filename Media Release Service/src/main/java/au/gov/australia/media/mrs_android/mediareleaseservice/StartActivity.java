package au.gov.australia.media.mrs_android.mediareleaseservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import au.gov.australia.media.mrs_android.mediareleaseservice.helper.JsonRequestHelper;


public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JsonRequestHelper helper = new JsonRequestHelper(getApplicationContext());
        helper.execute("https://test.media.australia.gov.au/api/mediareleases");
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }

}
