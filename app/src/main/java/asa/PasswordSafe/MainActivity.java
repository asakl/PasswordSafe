package asa.PasswordSafe;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    EditText Password;
    DBWarpper db;
    TreeMap<String, Pair<String, String>> info;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_window);

        Password = findViewById(R.id.passInputOnMain);
        db = new DBWarpper(this);

        EditText filter = findViewById(R.id.searchOnMain);
        filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                showInfo(db.selectPart(charSequence.toString()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void OkButtonOnClick(View v){

        if (checkPassword()) {
            key = Password.getText().toString();
            showInfo();
            findViewById(R.id.infoListOnMain).setVisibility(View.VISIBLE);
            findViewById(R.id.addInfoButtonOnMain).setVisibility(View.VISIBLE);
            findViewById(R.id.errorMsgOnMain).setVisibility(View.INVISIBLE);
            findViewById(R.id.okButtonOnMain).setVisibility(View.GONE);
            findViewById(R.id.passInputOnMain).setVisibility(View.GONE);
            findViewById(R.id.exitButtonOnMain).setVisibility(View.VISIBLE);
            findViewById(R.id.searchOnMain).setVisibility(View.VISIBLE);
        }
        else {
            clearView();
            findViewById(R.id.errorMsgOnMain).setVisibility(View.VISIBLE);
        }
    }

    public void MyExitButtonOnClick(View v){
        clearView();
        ((EditText)findViewById(R.id.passInputOnMain)).setText("");
    }

    public void clearView()
    {
        findViewById(R.id.addInfoButtonOnMain).setVisibility(View.INVISIBLE);
        findViewById(R.id.infoListOnMain).setVisibility(View.INVISIBLE);
        findViewById(R.id.okButtonOnMain).setVisibility(View.VISIBLE);
        findViewById(R.id.exitButtonOnMain).setVisibility(View.GONE);
        findViewById(R.id.passInputOnMain).setVisibility(View.VISIBLE);
        findViewById(R.id.searchOnMain).setVisibility(View.GONE);
    }

    private boolean checkPassword() {
        String pass = Password.getText().toString();

        Cursor res = db.getData("This");


        if (res.getCount() == 0){
            db.insertData("This", "None", AES.encrypt(pass, pass));
            return true;
        }
        else{
            res.moveToNext();
            return pass.equals(AES.decrypt(res.getString(3), pass));
        }
    }

    private void showInfo(){
        showInfo(db.getAllDataMap());
    }

    private void showInfo(TreeMap<String, Pair<String, String>> m){
         info = m;


        ListView listView = findViewById(R.id.infoListOnMain);
        List<HashMap<String, String>> list = new ArrayList<>();

        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.item_on_list, new String[]{"First Line"}, new int[]{R.id.text1});

        Iterator it = info.entrySet().iterator();

        while (it.hasNext())
        {
            HashMap<String, String> resultsMap = new HashMap<>();

            resultsMap.put("First Line", ((Map.Entry<String, Pair<String, String>>)it.next()).getKey());

            list.add(resultsMap);
        }

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListOnClick(parent, view, position, id);
            }

        });

    }

    public void AddButtonOnClick(View v){
        View dialog = LayoutInflater.from(this).inflate(R.layout.add_item_popup, null);
        final AlertDialog mainDialog = new AlertDialog.Builder(this).create();

        mainDialog.setView(dialog);
        mainDialog.show();


        dialog.findViewById(R.id.okButtonOnAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String site = ((EditText)mainDialog.findViewById(R.id.siteNameOnAdd)).getText().toString();
                String name = ((EditText)mainDialog.findViewById(R.id.usernameOnAdd)).getText().toString();
                String pass = ((EditText)mainDialog.findViewById(R.id.passOnAdd)).getText().toString();

                if(site.equals("This")){
                    Toast.makeText(MainActivity.this, "YOU CAN'T ADD THIS SITE!", new Integer(0)).show();
                }
                else{
                    boolean in = db.insertData(site, name, AES.encrypt(pass, key));
                    if(in){
                        showInfo();
                    }
                }
                mainDialog.dismiss();
            }
        });
    }

    public void ListOnClick(AdapterView<?> adapter, View v, int position, long id){
        View dialog = LayoutInflater.from(this).inflate(R.layout.popup_list_on_click, null);
        final AlertDialog mainDialog = new AlertDialog.Builder(this).create();
        mainDialog.setView(dialog);
        mainDialog.show();


        Map.Entry pair = null;
        Iterator it = info.entrySet().iterator();
        for(int i = 0 ; i <= position ; i++) {
            pair = (Map.Entry)it.next();
        }

        final Map.Entry<String, Pair<String, String>> res = pair;

        ((TextView)dialog.findViewById(R.id.nameOnEdit)).setText((res.getValue()).first);
        ((TextView)dialog.findViewById(R.id.passOnEdit)).setText(AES.decrypt((res.getValue()).second, key));

        if(res.getKey().equals("This")){
            dialog.findViewById(R.id.deleteButtonOnEdit).setVisibility(View.INVISIBLE);
        }
        else {
            dialog.findViewById(R.id.deleteButtonOnEdit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    db.deleteData(res.getKey());
                    showInfo();
                    mainDialog.dismiss();
                }
            });
        }

        dialog.findViewById(R.id.editButtonOnEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit(view, res.getKey(), (res.getValue()).first, (res.getValue()).second);
                mainDialog.dismiss();
            }
        });

    }

    public void edit(View v,String site, String name, String pass){

        View dialog = LayoutInflater.from(this).inflate(R.layout.add_item_popup, null);
        final AlertDialog mainDialog = new AlertDialog.Builder(this).create();


        if(site.equals("This"))
        {
            ((EditText) dialog.findViewById(R.id.siteNameOnAdd)).setText(site);
            ((EditText) dialog.findViewById(R.id.siteNameOnAdd)).setEnabled(false);
            ((EditText) dialog.findViewById(R.id.usernameOnAdd)).setText(name);
            ((EditText) dialog.findViewById(R.id.usernameOnAdd)).setEnabled(false);
            ((EditText) dialog.findViewById(R.id.passOnAdd)).setText(AES.decrypt(pass, key));
        }
        else {
            ((EditText) dialog.findViewById(R.id.siteNameOnAdd)).setText(site);
            ((EditText) dialog.findViewById(R.id.usernameOnAdd)).setText(name);
            ((EditText) dialog.findViewById(R.id.passOnAdd)).setText(AES.decrypt(pass, key));
        }
        mainDialog.setView(dialog);
        mainDialog.show();

        final String str = site;

        dialog.findViewById(R.id.okButtonOnAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText name = mainDialog.findViewById(R.id.usernameOnAdd);
                EditText pass = mainDialog.findViewById(R.id.passOnAdd);
                EditText site = mainDialog.findViewById(R.id.siteNameOnAdd);

                if(str.equals("This")) {
                    db.changePassword(key, pass.getText().toString());
                    showInfo();
                }
                else if(site.getText().toString().equals("This")){
                    Toast.makeText(MainActivity.this, "YOU CAN'T CHANGE TO THIS SITE!", new Integer(0)).show();
                }
                else{
                    db.deleteData(str);
                    boolean in = db.insertData(site.getText().toString(), name.getText().toString(), AES.encrypt(pass.getText().toString(), key));
                    if(in){
                        showInfo();
                    }
                }
                mainDialog.dismiss();
            }
        });
    }

}
