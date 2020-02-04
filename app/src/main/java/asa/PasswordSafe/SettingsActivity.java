package asa.PasswordSafe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Constants.color == 0) {
            setContentView(R.layout.settings_window_dark);
        }
        else {
            setContentView(R.layout.settings_window_light);
        }

        //setupList();

        Switch aSwitch = findViewById(R.id.darkThemeSwitch);

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                {
                    Constants.color = 0;
                    recreate();

                }
                else {
                    Constants.color = 1;
                    recreate();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.settingsMenuBack:
                myExit();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        myExit();
    }

    private void myExit()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onThemesPressed(View view){
        Intent intent = new Intent(this, ThemeActivity.class);
        startActivity(intent);
    }

    /*private void setupList()
    {
        String[] names = {getString(R.string.themes), getString(R.string.languages)};

        ListView listView = findViewById(R.id.settingList);
        List<HashMap<String, String>> list = new ArrayList<>();

        for(String i : names)
        {
            HashMap<String, String> map = new HashMap();
            map.put(Constants.firstLine, i);
            list.add(map);
        }

        listView.setAdapter(new SimpleAdapter(this, list, R.layout.item_on_list, new String[]{Constants.firstLine}, new int[]{R.id.text1}));
    }*/
}
