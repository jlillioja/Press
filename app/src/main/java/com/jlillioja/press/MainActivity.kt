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
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.Spreadsheet

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

    private val PREF_ACCOUNT_NAME = "accountName"
    private val SCOPES = mutableListOf(SheetsScopes.SPREADSHEETS)

    private lateinit var mCallApiButton: Button
    lateinit var mOutputText: TextView

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
                text = "Button"
                onClick {
                    setUpSheetsPermissions()
                }
            }
            scrollView {
                mOutputText = textView {
                    text = "Click the button to link an account and create a sheet."
                    textSize = 18f
                }
            }
        }
    }

    private fun setUpSheetsPermissions() {
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

    private fun isGooglePlayServicesAvailable(): Boolean {
        var apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
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

    private fun getPermissions(credential: GoogleAccountCredential) {
        MakeRequestTask(mCredential, this).execute()
        Toast.makeText(this, "Fetching whatever", LENGTH_SHORT).show()
    }


    @AfterPermissionGranted(1003)
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = getString(PREF_ACCOUNT_NAME)
            if (accountName != null) {
                mCredential.selectedAccountName = accountName
                setUpSheetsPermissions()
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
                setUpSheetsPermissions()
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
                    setUpSheetsPermissions()
                }
            }
            REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
                setUpSheetsPermissions()
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

    internal class MakeRequestTask(credential: GoogleAccountCredential,
                                   val activity: MainActivity) : AsyncTask<Void, Void, List<String>>() {

        private val SPREADSHEET_KEY = "SPREADSHEET_KEY"
        var spreadsheetId: String? = activity.defaultSharedPreferences.getString(SPREADSHEET_KEY, null)
        set(value) {
            activity.defaultSharedPreferences.edit().putString(SPREADSHEET_KEY, value).apply()
        }

        private var mService: Sheets = Sheets.Builder(AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("com.jlillioja.Press")
                .build()

        private fun output(text: String) {
            activity.runOnUiThread {
                activity.mOutputText.text = text
            }
        }

        override fun doInBackground(vararg params: Void): List<String>? {
            try {
                if (spreadsheetId == null) {
                    spreadsheetId = createSheet().spreadsheetId
                }
                return dataFromApi
            } catch (e: UserRecoverableAuthIOException) {
                activity.startActivityForResult(e.intent, 0)
                return dataFromApi
            } catch (e: Exception) {
                output("fucked")
                Log.d("IO", e.getStackTraceString())
                return null
            }
        }

        private fun createSheet(): Spreadsheet {
            val spreadsheet = Spreadsheet().apply {
                sheets = listOf(Sheet())
            }
            return mService.spreadsheets()
                    .create(spreadsheet)
                    .execute()
        }

        private val dataFromApi: List<String>
            @Throws(IOException::class)
            get() {
                val range = "Sheet1"
                val results = ArrayList<String>()
                val response = this.mService.spreadsheets().values()
                        .get(spreadsheetId, range)
                        .execute()
                val values = response.getValues()
                if (values != null) {
                    for (row in values) {
                        results.add(row[0].toString() + ", " + row[1])
                    }
                }
                return results
            }

        override fun onPreExecute() {
            output("about to get stuff")
        }

        override fun onPostExecute(result: List<String>?) {
            if (result == null || result.isEmpty()) {
                output("No results returned.")
            } else {
                output(result.fold("These are the results\n") { old, new -> old + "\n" + new })
            }
        }
    }
}

fun Activity.getString(key: String) : String? {
    return this.getPreferences(Context.MODE_PRIVATE).getString(key, null)
}
