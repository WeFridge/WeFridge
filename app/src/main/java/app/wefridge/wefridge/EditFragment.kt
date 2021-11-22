package app.wefridge.wefridge

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpOnClickListeners() {
        locateMeButton.setOnClickListener { getLastKnownLocation() }

        itemSaveButton.setOnClickListener { saveNewItem() }

        itemIsSharedSwitch.setOnClickListener { setLocationPickerActivation() }

        itemBestByDateTextInputLayout.editText?.setOnClickListener {
            setDatePickerVisibility()
            setDateStringToBestByDateEditText()
        }
    }

    private fun saveNewItem() {
        // TODO: replace PlaceholderItem with Item class from branch datamodel_item
        val newItem = PlaceholderContent.PlaceholderItem(
           id = (PlaceholderContent.ITEMS.size + 1).toString(),
           content = itemNameTextInputLayout.editText?.text.toString(),
           bestByDate = itemBestByDateTextInputLayout.editText?.text.toString(),
           details = itemDescriptionTextInputLayout.editText?.text.toString(),
           shared = itemIsSharedSwitch.isChecked)

        PlaceholderContent.ITEMS.add(newItem)
        Toast.makeText(requireContext(), "Item saved", Toast.LENGTH_SHORT).show()
        // this line of code is based on https://www.codegrepper.com/code-examples/kotlin/android+go+back+to+previous+activity+programmatically
        activity?.onBackPressed()
    }



    private fun getLastKnownLocation() {

        // this peace of code is partially based on https://developer.android.com/training/permissions/requesting#kotlin
        when {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // called when permission was granted
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            buildAddressString(location)
                        } else {
                            displayAlertDialogOnPermissionDenied()
                        }
                    }

                    .addOnFailureListener {
                        displayAlertDialogOnPermissionDenied()
                    }

            } shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
            // called when permission denied
                displayAlertDialogOnFailedLocationDetermination()
            }
            else -> {
                // called when permission settings unspecified (like "ask every time")
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }
        }

    }

    private fun buildAddressString(location: Location) {
        // the following code is based on https://stackoverflow.com/questions/9409195/how-to-get-complete-address-from-latitude-and-longitude
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val address =
            geocoder.getFromLocation(location.latitude, location.longitude, 1)
        val addressLine = address.get(0).getAddressLine(0)
        val city = address.get(0).locality
        val state = address.get(0).adminArea
        val postalCode = address.get(0).postalCode
        itemAddressTextInputLayout.editText?.setText("${addressLine}, ${postalCode} ${city}, ${state}")
    }

    private fun displayAlertDialogOnPermissionDenied() {
        AlertDialog.Builder(requireContext())
            .setTitle("Unable to determine location")
            .setMessage("Please try it another time.")
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun displayAlertDialogOnFailedLocationDetermination() {
        // TODO: outsource strings to strings file
        // this peace of code is based on https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
        AlertDialog.Builder(requireContext())
            .setTitle("Permission denied")
            .setMessage("We have no permission to access your location.\nIf you want to make use of the \"locate me\" functionality, please enable location access in settings.")
            .setPositiveButton(android.R.string.ok, null)
            .setNeutralButton("Open settings") { dialogInterface: DialogInterface, i: Int ->
                // this piece of code is based on https://stackoverflow.com/questions/19517417/opening-android-settings-programmatically
                dialogInterface.run { startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun adaptUIToModel() {
        hideItemSaveButtonOnExistingModelContent()
        fillFieldsWithModelContent()
    }

    private fun hideItemSaveButtonOnExistingModelContent() {
        if (model?.id != null) itemSaveButton?.isVisible = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fillFieldsWithModelContent() {
        itemNameTextInputLayout.editText?.setText(model?.content)
        itemQuantityTextInputLayout.editText?.setText(null) // TODO: support quantity (not yet provided by PlaceholderItem)
        itemUnitRadioGroup.check(radio_button_gram.id) // TODO: support unit (not yet provided by PlaceholderItem)
        itemBestByDateTextInputLayout.editText?.setText(model?.bestByDate) // TODO: use Date data type in future from Item data type
        itemIsSharedSwitch.isChecked = model?.shared ?: false
        itemAddressTextInputLayout.editText?.setText(null)
        itemDescriptionTextInputLayout.editText?.setText(model?.details)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpDatePicker() {
        itemBestByDatePicker.setOnDateChangedListener { datePicker, _, _, _ ->
           setDateStringToBestByDateEditText()
        }
    }

    private fun setDateStringToBestByDateEditText() {
        val day = itemBestByDatePicker.dayOfMonth
        val month = itemBestByDatePicker.month + 1
        val year = itemBestByDatePicker.year
        itemBestByDateTextInputLayout.editText?.setText("${day}. ${month} ${year}")
    }

    private fun setDatePickerVisibility() {
        when (itemBestByDatePicker.visibility) {
            View.GONE -> showDatePicker()
            View.INVISIBLE -> showDatePicker()
            View.VISIBLE -> hideDatePicker()
        }

    }

    private fun showDatePicker() {
        itemBestByDatePicker.visibility = View.VISIBLE
    }

    private fun hideDatePicker() {
        itemBestByDatePicker.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setLocationPickerActivation () {
        if (itemIsSharedSwitch.isChecked) {
            activateLocationPickerElements()
        } else {
            deactivateLocationPickerElements()
        }
    }

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
        itemDescriptionTextInputLayout.editText?.inputType = InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE
        itemDescriptionTextInputLayout.alpha = 1f
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EditFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}