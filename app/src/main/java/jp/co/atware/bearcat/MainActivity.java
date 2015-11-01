package jp.co.atware.bearcat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import jp.co.atware.bearcat.activity.MapActivity;
import jp.co.atware.bearcat.util.IntentName;
import jp.co.atware.bearcat.util.IntentResultCode;
import jp.co.atware.bearcat.util.PreferenceName;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.id)
    EditText mIdEditText;

    @Bind(R.id.password)
    EditText mPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String userId = preferences.getString(PreferenceName.USER_ID, null);
        if (!TextUtils.isEmpty(userId)) {
            startActivityForResult(new Intent(this, MapActivity.class), IntentResultCode.MAIN);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void login(View view) {
        String id = mIdEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        if (TextUtils.isEmpty(id)) {
            Toast.makeText(this, R.string.empty_id, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.empty_password, Toast.LENGTH_SHORT).show();
            return;
        }

        if ("miss".equals(password)) {
            Toast.makeText(this, R.string.miss_id_password, Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor chatApp = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
        chatApp.putString(PreferenceName.USER_ID, "12");
        chatApp.apply();

        startActivityForResult(new Intent(this, MapActivity.class), IntentResultCode.MAIN);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case IntentResultCode.MAIN:
                boolean isLogout = data.getBooleanExtra(IntentName.LOGOUT, true);
                if (!isLogout) {
                    this.finish();
                }
                break;
            default:
                break;
        }
    }

}
