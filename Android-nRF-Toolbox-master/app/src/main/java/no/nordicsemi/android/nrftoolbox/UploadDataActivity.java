package no.nordicsemi.android.nrftoolbox;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.achartengine.GraphicalView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.nordicsemi.android.nrftoolbox.nfc_ble_hybrid.LineGraphView;

public class UploadDataActivity extends AppCompatActivity {

    private double x, y, z, a, time;
    private int count;
    private LineGraphView mLineGraph = new LineGraphView();
    private GraphicalView mGraphView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_data);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mLineGraph = mLineGraph.getLineGraphView();
        showGraph();
    }


    private void showGraph() {
        mGraphView = mLineGraph.getView(this);
        ViewGroup layout = (ViewGroup) findViewById(R.id.graph_uploaded);
        layout.addView(mGraphView);
        mGraphView.repaint();
        mLineGraph.setZoomPan();
    }
    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    private static final int READ_REQUEST_CODE = 42;

    public void opentheDocument(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                //reads the text from the file and puts it into the string s, or throws an IO exception
                //when it does not work
                try {
                     readTextFromUri(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
    private void readTextFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        String line = reader.readLine();             //skips the first line
        String[] mValues;
        if (count != 0) {
            //reset the graph
            mLineGraph.clearGraph();
            mGraphView.repaint();
            count = 0;
        }
        while ((line = reader.readLine()) != null) {
            mValues = line.split("\t");
            x = Double.parseDouble(mValues[2]);
            y = Double.parseDouble(mValues[3]);
            z = Double.parseDouble(mValues[4]);
            a = Double.parseDouble(mValues[5]);
            //Take these values and update the graph similarly to in NFC_BLE_HYBRID_Activity
           updateGraph();
               //If any of the parse functions do not work, the file is not in the correct format,
               //and an error message should be presented saying, "incorrect file format"
        }
        inputStream.close();
    }
    private void updateGraph() {
        mLineGraph.addValue_x(count,x);
        mLineGraph.addValue_y(count,y);
        mLineGraph.addValue_z(count,z);
        mLineGraph.addValue_a(count,a);
        mGraphView.repaint();
        count++;
    }
    public void resetGraph(View view) {
        mLineGraph.clearGraph();
        mGraphView.repaint();
        count = 0;
    }
}
