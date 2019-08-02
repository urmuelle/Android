package com.azuresample.msalandroidapp;

/**
 * Created by dadaboli on 4/20/17.
 */

public class Constants {

    /* Azure AD b2c Configs */
    /*
    final static String AUTHORITY = "https://login.microsoftonline.com/tfp/%s/%s";
    final static String TENANT = "fabrikamb2c.onmicrosoft.com";
    final static String CLIENT_ID = "90c0fe63-bcf2-44d5-8fb7-b8bbc0b29dc6";
    final static String SCOPES = "https://fabrikamb2c.onmicrosoft.com/demoapi/demo.read";
    final static String API_URL = "https://fabrikamb2chello.azurewebsites.net/hello";

    final static String SISU_POLICY = "B2C_1_SUSI";
    final static String EDIT_PROFILE_POLICY = "B2C_1_edit_profile";
    final static String API_ENDPOINT = "https://fracturemonitor-webapi-test.azurewebsites.net/api/v2/FlexMove/Add";
    */

    public final static String AUTHORITY = "https://fracturemonitordev.b2clogin.com/tfp/%s/%s";
    public final static String TENANT = "fracturemonitordev.onmicrosoft.com";
    public final static String CLIENT_ID = "dcc8a591-05d2-4785-8163-30db6cfeda11";
    public final static String SCOPES = "email";

    public final static String SISU_POLICY = "B2C_1_SignInUI_v1";
    public final static String EDIT_PROFILE_POLICY = "b2c_1_edit_profile";
    public final static String API_ENDPOINT = "https://fracturemonitor-webapi-dev.azurewebsites.net/api/v1/FlexMove/List";
}
