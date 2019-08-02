package com.azuresample.msalandroidapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.AuthenticationResult;
import com.microsoft.identity.client.MsalClientException;
import com.microsoft.identity.client.MsalException;
import com.microsoft.identity.client.MsalServiceException;
import com.microsoft.identity.client.MsalUiRequiredException;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.UiBehavior;
import com.microsoft.identity.client.User;
//import org.apache.http.conn.
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.String;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.BasicHttpEntity;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class AuthenticatedActivity extends AppCompatActivity {

    /* UI & Debugging Variables */
    private static final String TAG = MainActivity.class.getSimpleName();
    Button apiButton;
    Button clearCacheButton;

    /* Azure AD variables */
    AppSubClass appState;
    private AuthenticationResult authResult;
    private PublicClientApplication sampleApp;
    String[] scopes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticated);

        apiButton = (Button) findViewById(R.id.edit);
        clearCacheButton = (Button) findViewById(R.id.clearCache);

        apiButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editProfile();
            }
        });

        clearCacheButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clearCache();
                finish();
            }
        });

        appState = AppSubClass.getInstance();
        sampleApp = appState.getPublicClient();
        authResult = appState.getAuthResult();
        scopes = Constants.SCOPES.split("\\s+");

        /* Write the token status (whether or not we received each token) */
        //this.updateTokenUI();

        /* Calls API, dump out response from UserInfo endpoint into UI */
        new callKonplanAPI().execute();
    }

    //
    // Core Identity methods used by MSAL
    // ==================================
    // onActivityResult() - Catches the redirect from the system browser
    // callAPI() - Calls our api with new access token
    // editProfile() - Calls b2c edit policy with this temporary authority
    // clearCache() - Clears token cache of this app
    //

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        sampleApp.handleInteractiveRequestRedirect(requestCode, resultCode, data);
    }

    /* Use Volley to request the /me endpoint from API
     *  Sets the UI to what we get back
     */
    private void callListAPI() {

        Log.d(TAG, "Starting volley request to API");

        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject parameters = new JSONObject();

        try {
            parameters.put("key", "value");
        } catch (Exception e) {
            Log.d(TAG, "Failed to put parameters: " + e.toString());
        }

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, Constants.API_ENDPOINT,
                null,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                /* Successfully called API */
                Log.d(TAG, "Response: " + response);
                try {
                    ((TextView) findViewById(R.id.welcome)).setText("Welcome, "
                            + response.toString());
                    Toast.makeText(getBaseContext(), "Response: " + response.toString(), Toast.LENGTH_SHORT)
                            .show();
                } catch (Exception e) {
                    Log.d(TAG, "JSONEXception Error: " + e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                Log.d(TAG, "Token: " + authResult.getAccessToken().toString());
                headers.put("Authorization", "Bearer " + authResult.getAccessToken());
                return headers;
            }
        };

        queue.add(request);
    }

    class callKonplanAPI extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected void onPreExecute() {
            //
        }

        protected String doInBackground(Void... urls) {

            // Do some validation here
            URL webAPIendPoint = null;

            OutputStream out = null;

            try {
                webAPIendPoint = new URL("https://fracturemonitor-webapi-dev.azurewebsites.net/api/v1/FlexMove/AddRaw");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            HttpClient httpclient = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet(Constants.API_ENDPOINT);


            try {

                HttpResponse response = httpclient.execute(getRequest);

                StatusLine statusLine = response.getStatusLine();

                Log.w("RESPONSE", statusLine.toString());

                // Get the response
                BufferedReader rd = new BufferedReader
                        (new InputStreamReader(
                                response.getEntity().getContent()));

                String line = "";
                while ((line = rd.readLine()) != null) {
                    Log.w("RESPONSE", line);
                }

                String string = "asdf";
                byte[] result = new byte[256];
                for (int i = 0; i<256; i++)
                {
                    result[i] = (byte) 0x4f;
                }
                //System.arraycopy(string.getBytes(), 0, result, 100 - string.length(), string.length());

                String base64String = Base64.encodeToString(result, Base64.DEFAULT);
                String dataToSend = "loggerDataRaw=" + Base64.encodeToString(result, Base64.DEFAULT);

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                //nameValuePairs.add(new BasicNameValuePair("loggerDataRaw",
                //        result.toString()));

                nameValuePairs.add(new BasicNameValuePair("loggerDataRaw",
                        "asdf"));


                HttpPost postRequest = new HttpPost("https://fracturemonitor-webapi-dev.azurewebsites.net/api/v1/FlexMove/AddRaw");

                /*
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addTextBody("loggerDataRaw", base64String);
                */
                //postRequest.addHeader("Authorization",authorization);
                postRequest.addHeader("Accept","application/json");
                postRequest.addHeader("Content-Type","application/json;charset=utf-8");
                //postRequest.addHeader("Transfer-Encoding", "chunked");

                //postRequest.setEntity(new StringEntity(dataToSend));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);

                //dos.writeBytes("loggerDataRaw=");
                //dos.write(result);
                ByteArrayInputStream content = new ByteArrayInputStream(baos.toByteArray());
                BasicHttpEntity entity = new BasicHttpEntity();
                entity.setChunked(true);
                entity.setContent(content);

                postRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                //postRequest.setEntity(entity);
                /*
                HttpEntity multipart = builder.build();
                postRequest.setEntity(multipart);
                */


                response = httpclient.execute(postRequest);
                rd = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent()));
                line = "";
                while ((line = rd.readLine()) != null) {
                    System.out.println(line);
                }
                /*
            }
                HttpsURLConnection urlConnection = new HttpsURLConnection(webAPIendPoint) {
                    @Override
                    public String getCipherSuite() {
                        return null;
                    }

                    @Override
                    public Certificate[] getLocalCertificates() {
                        return new Certificate[0];
                    }

                    @Override
                    public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
                        return new Certificate[0];
                    }

                    @Override
                    public void disconnect() {

                    }

                    @Override
                    public boolean usingProxy() {
                        return false;
                    }

                    @Override
                    public void connect() throws IOException {

                    }
                };
                */

                HttpsURLConnection urlConnection = (HttpsURLConnection) webAPIendPoint.openConnection();

                //OutputStream os = urlConnection.getOutputStream();

                urlConnection.setUseCaches(false);//set true to enable Cache for the req
                urlConnection.setDoOutput(true);//enable to write data to output stream
                //urlConnection.setDoInput(false);
                urlConnection.setConnectTimeout(30000);
                urlConnection.setReadTimeout(30000);

                String basicAuth = "Hallo Welt";
                //urlConnection.setRequestProperty ("Authorization", basicAuth);

                //int responseCode = urlConnection.getResponseCode();

                //if(responseCode == HttpURLConnection.HTTP_OK){
                //    Log.v("CatalogClient", "HTTP_OK");
                //}

            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }

            return null;
        }

        protected void onPostExecute(String response) {
            if (response == null) {
                response = "THERE WAS AN ERROR";
            }

            Log.i("INFO", response);

            // TODO: check this.exception
            // TODO: do something with the feed

//            try {
//                JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
//                String requestID = object.getString("requestId");
//                int likelihood = object.getInt("likelihood");
//                JSONArray photos = object.getJSONArray("photos");
//                .
//                .
//                .
//                .
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
        }
    }


    /* Use Volley to request the /me endpoint from API
     *  Sets the UI to what we get back
     */
    private void editProfile() {
        Log.d(TAG, "Starting volley request to API");
        try {
            String authority = String.format(Constants.AUTHORITY,
                    Constants.TENANT,
                    Constants.EDIT_PROFILE_POLICY);

            User currentUser = Helpers.getUserByPolicy(
                    sampleApp.getUsers(),
                    Constants.EDIT_PROFILE_POLICY);

            sampleApp.acquireToken(
                    this,
                    Constants.SCOPES.split("\\s+"),
                    currentUser,
                    UiBehavior.SELECT_ACCOUNT,
                    null,
                    null,
                    authority,
                    getEditPolicyCallback());
        } catch(MsalClientException e) {
            /* No User */
            Log.d(TAG, "MSAL Exception Generated while getting users: " + e.toString());
        }

    }

    /* Clears a user's tokens from the cache.
     * Logically similar to "signOut" but only signs out of this app.
     */
    private void clearCache() {
        List<User> users = null;
        try {
            Log.d(TAG, "Clearing app cache");
            users = sampleApp.getUsers();

            if (users == null) {
                /* We have no users */

                Log.d(TAG, "Faield to Sign out/clear cache, no user");
            } else if (users.size() == 1) {
                /* We have 1 user */

                /* Remove from token cache */
                sampleApp.remove(users.get(0));

                Log.d(TAG, "Signed out/cleared cache");

            }
            else {
                /* We have multiple users */

                for (int i = 0; i < users.size(); i++) {
                    sampleApp.remove(users.get(i));
                }

                Log.d(TAG, "Signed out/cleared cache for multiple users");
            }

            Toast.makeText(getBaseContext(), "Signed Out!", Toast.LENGTH_SHORT)
                    .show();

        } catch (MsalClientException e) {
            /* No token in cache, proceed with normal unauthenticated app experience */
            Log.d(TAG, "MSAL Exception Generated while getting users: " + e.toString());

        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "User at this position does not exist: " + e.toString());
        }
    }

    //
    // UI & Helper methods
    // ==================================
    // Everything below is some kind of helper to update app UI or do non-essential identity tasks
    // UpdateTokenUI() - Updates UI with token in cache status
    // hasRefreshToken() - Checks if we have a refresh token in our cache
    // UpdateRefreshTokenUI() - Updates UI with RT in cache status
    // getAuthSilentCallback() -
    //

    /* Write the token status (whether or not we received each token) */
    private void updateTokenUI() {
        if (authResult != null) {
            TextView it = (TextView) findViewById(R.id.itStatus);
            TextView at = (TextView) findViewById(R.id.atStatus);

            if(authResult.getIdToken() != null) {
                it.setText(it.getText() + " " + getString(R.string.tokenPresent));
            } else {
                it.setText(it.getText() + " " + getString(R.string.noToken));
            }

            if (authResult.getAccessToken() != null) {
                at.setText(at.getText() + " " + getString(R.string.tokenPresent));
            } else {
                at.setText(at.getText() + " " + getString(R.string.noToken));
            }

            /* Only way to check if we have a refresh token is to actually refresh our tokens */
            hasRefreshToken();
        } else {
            Log.d(TAG, "No authResult, something went wrong.");
        }
    }

    /* Checks if there's a refresh token in the cache.
     * Only way to check is to refresh the tokens and catch Exception.
     * Also is used to refresh the token.
     */
    private void hasRefreshToken() {

        /* Attempt to get a user and acquireTokenSilently
         * If this fails we will do an interactive request
         */
        List<User> users = null;
        try {
            User currentUser = Helpers.getUserByPolicy(sampleApp.getUsers(), Constants.SISU_POLICY);

            if (currentUser != null) {
            /* We have 1 user */
                boolean forceRefresh = true;
                sampleApp.acquireTokenSilentAsync(
                        scopes,
                        currentUser,
                        String.format(Constants.AUTHORITY, Constants.TENANT, Constants.SISU_POLICY),
                        forceRefresh,
                        getAuthSilentCallback());
            } else {
                /* We have no user for this policy*/
                updateRefreshTokenUI(false);
            }
        } catch (MsalClientException e) {
            /* No token in cache, proceed with normal unauthenticated app experience */
            Log.d(TAG, "MSAL Exception Generated while getting users: " + e.toString());

        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "User at this position does not exist: " + e.toString());
        }
    }

    /* Write the token status (whether or not we received each token) */
    private void updateRefreshTokenUI(boolean status) {

        TextView rt = (TextView) findViewById(R.id.rtStatus);

        if (rt.getText().toString().contains(getString(R.string.noToken))
                || rt.getText().toString().contains(getString(R.string.tokenPresent))) {
            rt.setText(R.string.RT);
        }
        if (status) {
            rt.setText(rt.getText() + " " + getString(R.string.tokenPresent));
        } else {
            rt.setText(rt.getText() + " " + getString(R.string.noToken) + " or Invalid");
        }
    }

    /* Callback used in for silent acquireToken calls.
     * Used in here solely to test whether or not we have a refresh token in the cache
     */
    private AuthenticationCallback getAuthSilentCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                /* Successfully got a token */
                updateRefreshTokenUI(true);

                /* If the token is refreshed we should refresh our data */
                //callAPI();
            }

            @Override
            public void onError(MsalException exception) {
                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.toString());
                updateRefreshTokenUI(false);
                if (exception instanceof MsalClientException) {
                    /* Exception inside MSAL, more info inside MsalError.java */
                    assert true;

                } else if (exception instanceof MsalServiceException) {
                    /* Exception when communicating with the STS, likely config issue */
                    assert true;

                } else if (exception instanceof MsalUiRequiredException) {
                    /* Tokens expired or no session, retry with interactive */
                    assert true;
                }
            }

            @Override
            public void onCancel() {
                /* User canceled the authentication */
                Log.d(TAG, "User cancelled login.");
                updateRefreshTokenUI(true);
            }
        };
    }

    /* Callback used in for silent acquireToken calls.
     * Used in here solely to test whether or not we have a refresh token in the cache
     */
    private AuthenticationCallback getEditPolicyCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                /* Successfully got a token */

                /* Use this method to refresh our token with new claims */
                Log.d(TAG, "Edit Profile: " + authenticationResult.getAccessToken());
                hasRefreshToken();
            }

            @Override
            public void onError(MsalException exception) {
                /* Failed to acquireToken */
                Log.d(TAG, "Edit Profile failed: " + exception.toString());

                if (exception instanceof MsalClientException) {
                    /* Exception inside MSAL, more info inside MsalError.java */
                    assert true;

                } else if (exception instanceof MsalServiceException) {
                    /* Exception when communicating with the STS, likely config issue */
                    assert true;

                } else if (exception instanceof MsalUiRequiredException) {
                    /* Tokens expired or no session, retry with interactive */
                    assert true;
                }
            }

            @Override
            public void onCancel() {
                /* User canceled the authentication */
                Log.d(TAG, "User cancelled Edit Profile.");
                Toast.makeText(getBaseContext(), getString(R.string.editFailure), Toast.LENGTH_SHORT)
                        .show();
            }
        };
    }
}
