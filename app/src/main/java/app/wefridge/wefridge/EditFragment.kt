package app.wefridge.wefridge

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import app.wefridge.wefridge.databinding.FragmentEditBinding
import app.wefridge.wefridge.model.*
import app.wefridge.wefridge.model.Unit
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.fragment_edit.*
import java.io.IOException
import java.text.DateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [EditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditFragment : Fragment() {
    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!
    private lateinit var model: Item
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val ADD_ITEM_MODE: Boolean get() = model.firebaseId.isNullOrEmpty()
    private var location: GeoPoint? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var unitDropdownMenu: PopupMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ownerReference = OwnerController.getCurrentUser()
        if (ownerReference == null) {
            requireActivity().onBackPressed()
            buildAlert(
                R.string.ad_title_account_not_verified,
                R.string.ad_msg_account_not_verified
            ).show()

        } else {
            model = arguments?.getParcelable(ARG_MODEL) ?: Item(ownerReference = ownerReference)

            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                model.firebaseId?.let { model.name } ?: getString(R.string.add_new_item)

            // TODO: move to separate file and call method from here
            // this piece of code is partially based on https://developer.android.com/training/permissions/requesting#kotlin
            requestPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                    if (isGranted) {
                        getCurrentLocation()
                    } else {
                        buildAlert(R.string.ad_title_location_permission_denied, R.string.ad_msg_location_permission_denied)
                            .setNeutralButton(R.string.ad_btn_open_settings) { dialogInterface: DialogInterface, _ ->
                                // this piece of code is based on https://stackoverflow.com/questions/19517417/opening-android-settings-programmatically
                                dialogInterface.run { startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                            }
                            .show()
                        Log.d("EditFragment", "Request for location access denied.")
                    }
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // this line of code was partially inspired by https://stackoverflow.com/questions/11741270/android-sharedpreferences-in-fragment
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        // Inflate the layout for this fragment
        _binding = FragmentEditBinding.inflate(inflater, container, false)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpItemNameTextInputLayout()
        setUpItemQuantityTextInputLayout()
        setUpUnitDropdown()
        setUpItemBestByDatePicker() // TODO: remove later
        setUpItemBestByDateTextInputLayout()
        // TODO: uncomment later
        //setUpItemBestByDatePicker()
        setUpLocationPickerBox()
        setUpItemIsSharedSwitch()
        setUpItemIsSharedSwitchLabel()
        setUpItemAddressTextInputLayout()
        setUpLocateMeButton()
        setUpItemDescriptionTextInputLayout()
        setUpSaveButton()

        location = model.location
    }

    // TODO: simplify the process of checking isShared + location and isShared and contactEmail
    override fun onDestroy() {
        super.onDestroy()
        if (!ADD_ITEM_MODE) {  // **new** Items should not be saved automatically, i. e. onDestroy
            // TODO: put the following condition into a separate function
            if (location == null && model.isShared) {
                model.location = null
                model.geohash = null
                model.isShared = false
                buildAlert(R.string.ad_title_invalid_address, R.string.ad_title_invalid_address).show()
            } else {
                setModelLocationAttribute()
                setModelGeohashAttribute()
            }
            setModelContactNameAttribute()
            setModelContactEmailAttribute()

            // TODO: put the following condition into a separate function
            if ((model.contactEmail == null || model.contactEmail == "") && model.isShared) {
                model.isShared = false
                buildAlert(R.string.ad_title_contact_email_missing, R.string.ad_msg_contact_email_missing).show()
            }

            ItemController.saveItem(model, { /* do nothing on success */ }, { buildAlert(R.string.ad_title_error_save_item, R.string.ad_msg_error_save_item).show() })
        }
    }

    private fun setUpItemNameTextInputLayout() {
        itemNameTextInputLayout.editText?.addTextChangedListener { setModelNameAttribute() }
        itemNameTextInputLayout.editText?.setText(model.name)

    }

    private fun setUpItemQuantityTextInputLayout() {
        itemQuantityTextInputLayout.editText?.addTextChangedListener { setModelQuantityAttribute() }
        itemQuantityTextInputLayout.editText?.setText(model.quantity.toString())
    }

    private fun setUpUnitDropdown() {
        unitDropdownMenu = PopupMenu(requireActivity(), unit_dropdown)
        unitDropdownMenu.inflate(R.menu.unit_dropdown)
        unitDropdownMenu.setOnMenuItemClickListener { menuItem ->
            unit_dropdown.editText?.setText(menuItem.title)
            setModelUnitAttribute()
            true
        }
        unit_dropdown.editText?.setText(matchUnitValueToUnitDropdownSelection())
        unit_dropdown.editText?.setOnClickListener { unitDropdownMenu.show() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpItemBestByDateTextInputLayout() {
        // TODO: build date string from model
        if (model.bestByDate != null) itemBestByDateTextInputLayout.editText?.setText(buildDateStringFromDatePicker())
        itemBestByDateTextInputLayout.editText?.setOnClickListener {
            setDatePickerVisibility()
            model.bestByDate?.let { setDatePickerDateTo(it) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpItemBestByDatePicker() {
        hideDatePicker()
        model.bestByDate?.let { setDatePickerDateTo(it) }
        itemBestByDatePicker.setOnDateChangedListener { _, _, _, _ ->
            itemBestByDateTextInputLayout.editText?.setText(buildDateStringFromDatePicker())
            setModelBestByDateAttribute()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpLocationPickerBox() {
        setLocationPickerActivation()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpItemIsSharedSwitch() {
        itemIsSharedSwitch.isChecked = model.isShared
        itemIsSharedSwitch.setOnCheckedChangeListener { _, _ ->
            setModelIsSharedAttribute()
            setLocationPickerActivation()
        }
    }

    private fun setUpItemIsSharedSwitchLabel() {
        itemIsSharedSwitchLabel.setOnClickListener { itemIsSharedSwitch.toggle() }
    }

    // TODO: refactor, so that the location from "locate me" btn can be inserted without calling getGeoPointFromAddressUserInput
    private fun setUpItemAddressTextInputLayout() {
        model.location?.let { itemAddressTextInputLayout.editText?.setText(buildAddressString(it)) }
        itemAddressTextInputLayout.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) location = getGeoPointFromAddressUserInput()
        }
    }

    private fun setUpLocateMeButton() {
        locateMeButton.setOnClickListener { getCurrentLocation() }
    }

    private fun setUpItemDescriptionTextInputLayout() {
        itemDescriptionTextInputLayout.editText?.setText(model.description)
        itemDescriptionTextInputLayout.editText?.addTextChangedListener { setModelDescriptionAttribute() }
    }

    private fun setUpSaveButton() {
        if (!ADD_ITEM_MODE) itemSaveButton?.isVisible = false
        itemSaveButton.setOnClickListener { itemAddressTextInputLayout.clearFocus(); saveNewItem() }

    }

    // TODO: consider removing this methods and instead call code directly
    private fun setModelNameAttribute() {
        model.name = itemNameTextInputLayout.editText?.text.toString()
    }

    private fun setModelQuantityAttribute() {
        val quantityString = itemQuantityTextInputLayout.editText?.text.toString()
        model.quantity = if (quantityString.isEmpty() || quantityString.isBlank()) 0 else quantityString.toLong()

    }

    private fun setModelUnitAttribute() {
        model.unit = matchUnitDropdownSelectionToUnit()
    }

    private fun setModelLocationAttribute() {
        model.location = location
    }

    private fun setModelGeohashAttribute() {
        if (location != null)
            model.geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(location!!.latitude, location!!.longitude))
    }

    private fun setModelContactNameAttribute() {
        val userNameAsFallbackValue = FirebaseAuth.getInstance().currentUser?.displayName
        model.contactName = sharedPreferences.getString(SETTINGS_NAME, userNameAsFallbackValue)
    }

    private fun setModelContactEmailAttribute() {
        val userEmailAsFallbackValue = FirebaseAuth.getInstance().currentUser?.email
        model.contactEmail = sharedPreferences.getString(SETTINGS_EMAIL, userEmailAsFallbackValue)
    }

    private fun setModelBestByDateAttribute() {
        if (itemBestByDateTextInputLayout.editText?.text.toString() != "")
            model.bestByDate = getDateFromDatePicker()
        else
            model.bestByDate = null
    }

    private fun setModelIsSharedAttribute() {
        model.isShared = itemIsSharedSwitch.isChecked
    }


    private fun setModelDescriptionAttribute() {
        model.description = itemDescriptionTextInputLayout.editText?.text.toString()
    }

    // TODO: simplify the process of checking, if isCheck == true and location ==null
    // TODO: samle for contactEmail and isShared
    private fun saveNewItem() {

        // TODO: put the following condition into a separate function
        if (itemIsSharedSwitch.isChecked && location == null) {
            buildAlert(R.string.ad_title_invalid_address, R.string.ad_title_invalid_address).show()
            itemAddressTextInputLayout.editText?.setText("")
        } else {

            // TODO: remove set* method calls
            //model = Item(ownerReference = ownerRef)
            setModelNameAttribute()
            setModelQuantityAttribute()
            setModelUnitAttribute()
            setModelBestByDateAttribute()
            setModelIsSharedAttribute()
            setModelLocationAttribute()
            setModelGeohashAttribute()
            setModelDescriptionAttribute()
            setModelContactNameAttribute()
            setModelContactEmailAttribute()

            // TODO: put the following condition into a separate function
            if ((model.contactEmail == null || model.contactEmail == "") && model.isShared) {
                model.isShared = false
                buildAlert(R.string.ad_title_contact_email_missing, R.string.ad_msg_contact_email_missing).show()
            } else {

                ItemController.saveItem(model, {
                    // saving was successful
                    Toast.makeText(requireContext(), "Item saved", Toast.LENGTH_SHORT).show()

                    // this line of code is based on https://www.codegrepper.com/code-examples/kotlin/android+go+back+to+previous+activity+programmatically
                    activity?.onBackPressed()
                },
                    {
                        // saving newItem failed
                        buildAlert(R.string.ad_title_error_save_item, R.string.ad_msg_error_save_item).show()
                    })

            }
        }

    }

    // TODO: move into separate file
    private fun getCurrentLocation() {

        // this piece of code is partially based on https://developer.android.com/training/permissions/requesting#kotlin
        when {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // called when permission was granted
                fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(requireContext())
                fusedLocationClient.getCurrentLocation(
                    PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                )
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            itemAddressTextInputLayout.editText?.requestFocus()
                            setAddressStringToItemAddressTextEdit(GeoPoint(location.latitude, location.longitude))
                            itemAddressTextInputLayout.editText?.clearFocus()
                        } else {
                            buildAlert(R.string.ad_title_location_determination_failed, R.string.ad_msg_location_determination_failed).show()
                        }
                    }

                    .addOnFailureListener { exception ->
                        buildAlert(R.string.ad_title_location_determination_failed, R.string.ad_msg_location_determination_failed).show()
                        Log.e("EditFragment", "Error after getCurrentLocation: ", exception)
                    }

            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // show Dialog which explains the reason for accessing the user's location
                // called, when permissions denied
                buildAlert(R.string.ad_title_location_permission_rationale, R.string.ad_msg_location_permission_rationale)
                    .setNeutralButton(R.string.ad_btn_open_settings) { dialogInterface: DialogInterface, _ ->
                        // this piece of code is based on https://stackoverflow.com/questions/19517417/opening-android-settings-programmatically
                        dialogInterface.run { startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                    }
                    .show()
            }
            else -> {
                // called when permission settings unspecified (like "ask every time")
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }

    }

    // TODO: move into related setUp method
    private fun setAddressStringToItemAddressTextEdit(location: GeoPoint) {
        // the following code is based on https://stackoverflow.com/questions/9409195/how-to-get-complete-address-from-latitude-and-longitude

        itemAddressTextInputLayout.editText?.setText(buildAddressString(location))
    }

    // TODO: refactor (accept string as input) and move to separate file
    private fun getGeoPointFromAddressUserInput(): GeoPoint? {
        // the following piece of code is inspired by https://stackoverflow.com/questions/3574644/how-can-i-find-the-latitude-and-longitude-from-address/27834110#27834110
        val userInputAddress = itemAddressTextInputLayout.editText?.text.toString()
        val geocoder = Geocoder(requireContext())
        var matchedGeoPoint: GeoPoint? = null

        if (userInputAddress == "" || userInputAddress.isBlank() || userInputAddress.isEmpty()) return null

        try {
            val matchedAddresses: List<Address> = geocoder.getFromLocationName(userInputAddress, 1)
            if (matchedAddresses.isEmpty()) return null

            val chosenAddress = matchedAddresses[0]
            matchedGeoPoint = GeoPoint(chosenAddress.latitude, chosenAddress.longitude)
        } catch (exc: IOException) {
            if (itemIsSharedSwitch.isChecked) itemIsSharedSwitch.toggle()
            buildAlert(R.string.ad_title_error_parsing_address_string, R.string.ad_msg_error_parsing_address_string).show()
            exc.message?.let { Log.e("EditFragment", it) }
        }

        return matchedGeoPoint
    }


    // TODO: move to separate file
    private fun buildAddressString(location: GeoPoint): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        var addressString = ""
        try {
            val address =
                geocoder.getFromLocation(location.latitude, location.longitude, 1)

            val addressLine = address[0].getAddressLine(0)
            val locality = address[0].locality
            val adminArea = address[0].adminArea
            val postalCode = address[0].postalCode

            addressString = "${addressLine}, $postalCode $locality, $adminArea"

        } catch (exc: IOException) {
            if (itemIsSharedSwitch.isChecked) itemIsSharedSwitch.toggle()
            buildAlert(R.string.ad_title_no_network, R.string.ad_msg_no_network).show()
            exc.message?.let { Log.e("EditFragment", it) }
        }

        return addressString

    }

    // TODO: move to separate file?
    private fun buildAlert(title: String, message: String): AlertDialog.Builder {
        return AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
    }

    private fun buildAlert(@StringRes title: Int, @StringRes message: Int): AlertDialog.Builder {
        // this piece of code is based on https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
        return AlertDialog.Builder(requireContext())
             .setTitle(title)
             .setMessage(message)
             .setPositiveButton(android.R.string.ok, null)
    }

    // TODO: remove method and add string attribute to Unit. Init strings with resource strings
    private fun matchUnitValueToUnitDropdownSelection(): String {
        return when (model.unit.value) {
           Unit.GRAM.value -> getString(R.string.itemUnitGramText)
            Unit.KILOGRAM.value -> getString(R.string.itemUnitKilogramText)
            Unit.LITER.value -> getString(R.string.itemUnitLiterText)
            Unit.MILLILITER.value -> getString(R.string.itemUnitMilliliterText)
            Unit.OUNCE.value -> getString(R.string.itemUnitOunceText)
            Unit.PIECE.value -> getString(R.string.itemUnitPieceText)
            else -> getString(R.string.itemUnitPieceText)
        }

    }

    // TODO: remove method and add in Unit: get_by_string
    private fun matchUnitDropdownSelectionToUnit(): Unit {
        return when (unit_dropdown.editText?.text.toString()) {
            getString(R.string.itemUnitGramText) -> Unit.GRAM
            getString(R.string.itemUnitKilogramText) -> Unit.KILOGRAM
            getString(R.string.itemUnitLiterText) -> Unit.LITER
            getString(R.string.itemUnitMilliliterText) -> Unit.MILLILITER
            getString(R.string.itemUnitOunceText) -> Unit.OUNCE
            getString(R.string.itemUnitPieceText) -> Unit.PIECE
            else -> Unit.PIECE
        }
    }

    // TODO: create extension for DatePicker (or create own class if that doesn't work)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDatePickerDateTo(date: Date) {
        val bestByDate = convertToLocalDate(date)
        itemBestByDatePicker.updateDate(bestByDate.year, bestByDate.monthValue - 1, bestByDate.dayOfMonth)

    }

    // TODO: move to Utils?
    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertToLocalDate(date: Date): LocalDate {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    // TODO: create extension with "toSting()"? for DatePicker (or create own class if that doesn't work)
    private fun buildDateStringFromDatePicker(): String {
        val day = itemBestByDatePicker.dayOfMonth
        val month = itemBestByDatePicker.month
        val year = itemBestByDatePicker.year
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)

        return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(calendar.time)
    }

    // TODO: create extension for DatePicker (or create own class if that doesn't work)
    private fun getDateFromDatePicker(): Date {
        val day = itemBestByDatePicker.dayOfMonth
        val month = itemBestByDatePicker.month
        val year = itemBestByDatePicker.year

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)

        return calendar.time
    }

    // TODO: move into appropriate setUp* method.
    private fun setDatePickerVisibility() {
        when (itemBestByDatePicker.visibility) {
            View.GONE -> showDatePicker()
            View.INVISIBLE -> showDatePicker()
            View.VISIBLE -> hideDatePicker()
        }

    }

    // TODO: remove?
    private fun showDatePicker() {
        itemBestByDatePicker.visibility = View.VISIBLE
    }

    // TODO: remove?
    private fun hideDatePicker() {
        itemBestByDatePicker.visibility = View.GONE
    }

    // TODO: create setUp* method for LocationPicker and move this code into it
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setLocationPickerActivation() {
        if (model.isShared) activateLocationPickerElements()
        else deactivateLocationPickerElements()

    }

    // TODO: refactor. One generic method for editTexts. Overload with different parameter (button)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun deactivateLocationPickerElements() {
        locateMeButton.isClickable = false
        locateMeButton.alpha = .5f

        itemAddressTextInputLayout.editText?.isEnabled = false
        itemAddressTextInputLayout.editText?.focusable = View.NOT_FOCUSABLE
        itemAddressTextInputLayout.editText?.isFocusableInTouchMode = false
        itemAddressTextInputLayout.editText?.inputType = InputType.TYPE_NULL
        itemAddressTextInputLayout.alpha = .5f


        itemDescriptionTextInputLayout.editText?.isEnabled = false
        itemDescriptionTextInputLayout.editText?.focusable = View.NOT_FOCUSABLE
        itemDescriptionTextInputLayout.editText?.isFocusableInTouchMode = false
        itemDescriptionTextInputLayout.editText?.inputType = InputType.TYPE_NULL
        itemDescriptionTextInputLayout.alpha = .5f

    }

    // TODO: refactor. One generic method for editTexts. Overload with different parameter (button)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun activateLocationPickerElements() {
        locateMeButton.isClickable = true
        locateMeButton.alpha = 1f

        itemAddressTextInputLayout.editText?.isEnabled = true
        itemAddressTextInputLayout.editText?.focusable = View.FOCUSABLE
        itemAddressTextInputLayout.editText?.isFocusableInTouchMode = true
        itemAddressTextInputLayout.editText?.inputType = InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE
        itemAddressTextInputLayout.alpha = 1f


        itemDescriptionTextInputLayout.editText?.isEnabled = true
        itemDescriptionTextInputLayout.editText?.focusable = View.FOCUSABLE
        itemDescriptionTextInputLayout.editText?.isFocusableInTouchMode = true
        itemDescriptionTextInputLayout.editText?.inputType =
            InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE
        itemDescriptionTextInputLayout.alpha = 1f
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment EditFragment.
         */
        @JvmStatic
        fun newInstance(model: Item) =
            EditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_MODEL, model)
                }
            }
    }
}