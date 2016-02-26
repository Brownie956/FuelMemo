/*Author: Chris Brown
* Date: 26/02/2016
* Description: Login activity class. Displays login screen
* User can login or navigate to the sign up screen*/
package com.cfbrownweb.fuelmemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "cfbrownweb";

    private final String loginUrl = "http://cfbrownweb.ngrok.io/fuel/login.php";

    private EditText userInput;
    private EditText passInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userInput = (EditText) findViewById(R.id.login_username_input);
        passInput = (EditText) findViewById(R.id.login_password_input);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //only letters and numbers in the username field - Max Length = 20
        userInput.setFilters(new InputFilter[]{new LettersNumberInputFilter(false), new InputFilter.LengthFilter(20)});

        //no spaces allowed in the password field - Max Length = 20
        passInput.setFilters(new InputFilter[]{new NoWhitespaceInputFilter(), new InputFilter.LengthFilter(20)});
    }

    public void loginReq(View view){
        //Get all the inputs
        final String user = userInput.getText().toString();
        final String pass = passInput.getText().toString();

        //check user and pass inputs
        if(user.isEmpty()){
            Toast.makeText(this, getString(R.string.login_empty_user), Toast.LENGTH_LONG).show();
        }
        else if(user.length() < 5){
            Toast.makeText(this, getString(R.string.login_min_user), Toast.LENGTH_LONG).show();
        }
        else if(pass.isEmpty()){
            Toast.makeText(this, getString(R.string.login_empty_pass), Toast.LENGTH_LONG).show();
        }
        else if(pass.length() < 5){
            Toast.makeText(this, getString(R.string.login_min_pass), Toast.LENGTH_LONG).show();
        }
        else {
            RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, loginUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                        /*Outcomes:
                        * 1 = Success*/
                            if(response.equals("0")){
                                //Failed login
                                displayLoginError();
                            }
                            else if(response.equals("1")){
                                //login
                                //Store user in config
                                Configuration.getConfig().setUser(new User(user));

                                //Navigate to vehicles page
                                Intent vehiclesIntent = new Intent(LoginActivity.this, VehiclesActivity.class);
                                startActivity(vehiclesIntent);

                                //Close the login
                                finish();
                            }
                            else {
                                //Something went wrong
                                Utils.defaultErrorToast(LoginActivity.this);
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(error.networkResponse == null){
                        //Network error
                        Utils.netErrorToast(LoginActivity.this);
                    }
                    else {
                        //A different error
                        Utils.serverErrorToast(LoginActivity.this);
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("user",user);
                    params.put("pass",pass);

                    return params;
                }
            };
            queue.add(stringRequest);

        }
    }

    private void displayLoginError() {
        TextView loginError = (TextView) findViewById(R.id.login_error);
        loginError.setVisibility(View.VISIBLE);
    }

    public void goToSignUp(View view){
        Intent signUpIntent = new Intent(this, SignUpActivity.class);
        startActivity(signUpIntent);
    }
}
