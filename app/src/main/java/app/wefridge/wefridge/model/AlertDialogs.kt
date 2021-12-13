package app.wefridge.wefridge.model

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.provider.Settings
import androidx.annotation.StringRes
import app.wefridge.wefridge.R

fun alertDialogOnLocationPermissionDenied(ctx: Context): AlertDialog.Builder = buildAlert(R.string.item_share_location_permission_denied_title, R.string.item_share_location_permission_denied_text, ctx)
    .setNeutralButton(R.string.ad_btn_open_settings) { dialogInterface: DialogInterface, _ ->
        // this piece of code is based on https://stackoverflow.com/questions/19517417/opening-android-settings-programmatically
        dialogInterface.run { ctx.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
    }

fun alertDialogForLocationPermissionRationale(ctx: Context): AlertDialog.Builder = buildAlert(R.string.item_share_location_rationale_title, R.string.item_share_location_permission_rationale_text, ctx)
    .setNeutralButton(R.string.ad_btn_open_settings) { dialogInterface: DialogInterface, _ ->
        // this piece of code is based on https://stackoverflow.com/questions/19517417/opening-android-settings-programmatically
        dialogInterface.run { ctx.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
    }

fun alertDialogOnUnableToDetermineLocation(ctx: Context) = buildAlert(R.string.item_share_unable_to_determine_location_title, R.string.item_share_unable_to_determine_location_text, ctx)

fun alertDialogOnLocationNoInternetConnection(ctx: Context) = buildAlert(R.string.item_share_no_internet_connection_title, R.string.item_share_no_internet_connection_text, ctx)

fun alertDialogOnErrorParsingAddressString(ctx: Context) = buildAlert(R.string.item_share_location_problem_title, R.string.item_share_location_problem_text, ctx)

fun alertDialogOnErrorParsingAddressStringOnDestroy(ctx: Context) = buildAlert(R.string.item_share_location_problem_title, R.string.item_share_location_problem_on_destroy_text, ctx)

fun alertDialogOnItemNotSaved(ctx: Context) = buildAlert(R.string.item_share_item_not_saved_title, R.string.item_share_item_not_saved_text, ctx)

fun alertDialogOnContactEmailMissing(ctx: Context) = buildAlert(R.string.item_share_contact_email_missing_title, R.string.item_share_contact_email_missing_text, ctx)

fun alertDialogOnContactEmailMissingOnDestroy(ctx: Context) = buildAlert(R.string.item_share_contact_email_missing_on_destroy_title, R.string.item_share_contact_email_missing_on_destroy_text, ctx)

fun alertDialogOnInvalidAddress(ctx: Context) = buildAlert(R.string.item_share_invalid_address_title, R.string.item_share_invalid_address_text, ctx)

fun alertDialogOnUserLoggedOut(ctx: Context) = buildAlert(R.string.user_logged_out_title, R.string.user_logged_out_text, ctx)

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
