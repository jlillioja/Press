package com.jlillioja.press

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat.startActivity
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.content.ContextCompat
import android.text.Layout
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes

import com.jlillioja.press.database.DatabaseManager
import org.jetbrains.anko.*
import org.robolectric.annotation.Implements
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

import java.io.IOException
import javax.inject.Inject

@Implements
open class MainActivity : Activity(), EasyPermissions.PermissionCallbacks {

    @Inject
    open lateinit var databaseManager: DatabaseManager

    val REQUEST_ACCOUNT_PICKER = 1000

    val REQUEST_AUTHORIZATION = 1001
    val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
    private val BUTTON_TEXT = "Call Google Sheets API"

    private val PREF_ACCOUNT_NAME = "accountName"
    private val SCOPES = mutableListOf(SheetsScopes.SPREADSHEETS_READONLY)


    private lateinit var mCallApiButton: Button
    private lateinit var mOutputText: TextView

    val mCredential: GoogleAccountCredential by lazy {
        GoogleAccountCredential
                .usingOAuth2(applicationContext, SCOPES)
                .setBackOff(ExponentialBackOff())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PressApplication.graph.inject(this)

        verticalLayout {
            mCallApiButton = button {
                onClick { getResultsFromApi() }
            }
            mOutputText = textView {

            }
        }
    }

    private fun getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (mCredential.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            mOutputText.text = "No network connection available."
        } else {
            getPermissions(mCredential)
        }
    }

    private fun getPermissions(credential: GoogleAccountCredential) {
        MakeRequestTask(mCredential, this).execute()
        Toast.makeText(this, "Fetching whatever", LENGTH_SHORT).show()
    }

    private fun output(text: String) {
        mOutputText.text = text
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    fun showGooglePlayServicesAvailabilityErrorDialog(
            connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                this@MainActivity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }

    private fun isDeviceOnline(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }


    @AfterPermissionGranted(1003)
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null)
            if (accountName != null) {
                mCredential.selectedAccountName = accountName
                getResultsFromApi()
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER)
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS)
        }
    }

    override fun onActivityResult(
            requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                mOutputText.text = "This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app."
            } else {
                getResultsFromApi()
            }
            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null &&
                    data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    val settings = getPreferences(Context.MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString(PREF_ACCOUNT_NAME, accountName)
                    editor.apply()
                    mCredential.selectedAccountName = accountName
                    getResultsFromApi()
                }
            }
            REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
                getResultsFromApi()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>?) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>?) {

    }

    internal class MakeRequestTask(credential: GoogleAccountCredential, val context: Context) : AsyncTask<Void, Void, List<String>>() {
        private var mService: Sheets? = null
        private var mLastError: Exception? = null

        init {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            mService = Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("com.jlillioja.Press")
                    .build()
        }

        private fun output(text: String) {
            context.runOnUiThread {

                Toast.makeText(context, text, LENGTH_SHORT).show()
            }
        }

        /**
         * Background task to call Google Sheets API.
         * @param params no parameters needed for this task.
         */
        override fun doInBackground(vararg params: Void): List<String>? {
            try {
                return dataFromApi
            } catch (e: UserRecoverableAuthIOException) {
                ContextCompat.startActivity(context, e.intent, null)
                return dataFromApi
            } catch (e: Exception) {
                mLastError = e
                output("fucked")
                Log.d("IO", e.getStackTraceString())
                return null
            }
        }

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         * @return List of names and majors
         * *
         * @throws IOException
         */
        private val dataFromApi: List<String>
            @Throws(IOException::class)
            get() {
                val spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms"
                val range = "Class Data!A2:E"
                val results = ArrayList<String>()
                val response = this.mService!!.spreadsheets().values()
                        .get(spreadsheetId, range)
                        .execute()
                val values = response.getValues()
                if (values != null) {
                    results.add("Name, Major")
                    for (row in values) {
                        results.add(row[0].toString() + ", " + row[4])
                    }
                }
                return results
            }


        override fun onPreExecute() {
            output("about to get stuff")
        }

        override fun onPostExecute(output: List<String>?) {
            if (output == null || output.size === 0) {
                this.output( "No results returned.")
            } else {
                this.output(output.fold("These are the results"){ old, new -> old+new})
            }
        }
    }
}
