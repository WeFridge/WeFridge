package app.wefridge.wefridge.model

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import app.wefridge.wefridge.R

fun alertDialogOnLocationPermissionDenied(fragment: Fragment): AlertDialog.Builder = buildAlert(R.string.ad_title_location_permission_denied, R.string.ad_msg_location_permission_denied, fragment.requireContext())
    .setNeutralButton(R.string.ad_btn_open_settings) { dialogInterface: DialogInterface, _ ->
        // this piece of code is based on https://stackoverflow.com/questions/19517417/opening-android-settings-programmatically
        dialogInterface.run { fragment.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
    }

fun alertDialogForLocationPermissionRationale(fragment: Fragment): AlertDialog.Builder = buildAlert(R.string.ad_title_location_permission_rationale, R.string.ad_msg_location_permission_rationale, fragment.requireContext())
    .setNeutralButton(R.string.ad_btn_open_settings) { dialogInterface: DialogInterface, _ ->
        // this piece of code is based on https://stackoverflow.com/questions/19517417/opening-android-settings-programmatically
        dialogInterface.run { fragment.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
    }

fun alertDialogForLocationDeterminationFailed(fragment: Fragment) = buildAlert(R.string.ad_title_location_determination_failed, R.string.ad_msg_location_determination_failed, fragment.requireContext())

fun alertDialogOnLocationNoNetwork(fragment: Fragment) = buildAlert(R.string.ad_title_no_network, R.string.ad_msg_no_network, fragment.requireContext())

fun alertDialogOnErrorParsingAddressString(fragment: Fragment) = buildAlert(R.string.ad_title_error_parsing_address_string, R.string.ad_msg_error_parsing_address_string, fragment.requireContext())

fun alertDialogOnSaveItemFailed(fragment: Fragment) = buildAlert(R.string.ad_title_save_item_failed, R.string.ad_msg_save_item_failed, fragment.requireContext())

fun alertDialogOnContactEmailMissing(fragment: Fragment) = buildAlert(R.string.ad_title_contact_email_missing, R.string.ad_msg_contact_email_missing, fragment.requireContext())

fun alertDialogOnInvalidAddress(fragment: Fragment) = buildAlert(R.string.ad_title_invalid_address, R.string.ad_msg_invalid_address, fragment.requireContext())

fun alertDialogOnAccountNotVerified(fragment: Fragment) = buildAlert(R.string.ad_title_account_not_verified, R.string.ad_msg_account_not_verified, fragment.requireContext())

fun buildAlert(title: String, message: String, context: Context): AlertDialog.Builder {
    return AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, null)
}

fun buildAlert(@StringRes title: Int, @StringRes message: Int, context: Context): AlertDialog.Builder {
    // this piece of code is based on https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
    return AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, null)
}
