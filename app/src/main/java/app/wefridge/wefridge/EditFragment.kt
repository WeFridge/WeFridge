package app.wefridge.wefridge

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import app.wefridge.wefridge.databinding.FragmentEditBinding
import app.wefridge.wefridge.model.*
import app.wefridge.wefridge.model.Unit
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.fragment_edit.*
import java.io.IOException

/**
 * A simple [Fragment] subclass.
 * Use the [EditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditFragment : Fragment() {
    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!
    private lateinit var model: Item
    private lateinit var locationController: LocationController
    private val ADD_ITEM_MODE: Boolean get() = model.firebaseId.isNullOrEmpty()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var unitDropdownMenu: PopupMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ownerReference = OwnerController.getCurrentUser()
        if (ownerReference == null) {
            requireActivity().onBackPressed()
            alertDialogOnAccountNotVerified(this).show()

        } else {
            model = arguments?.getParcelable(ARG_MODEL) ?: Item(ownerReference = ownerReference)

            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                model.firebaseId?.let { model.name } ?: getString(R.string.add_new_item)

            locationController = LocationController(this,
                callbackOnPermissionDenied = { alertDialogOnLocationPermissionDenied(this) },
                callbackForPermissionRationale = { alertDialogForLocationPermissionRationale(this) },
                callbackOnDeterminationFailed = { alertDialogForLocationDeterminationFailed(this) },
                callbackOnSuccess = { geoPoint ->
                    model.location = geoPoint
                    itemAddressTextInputLayout.editText?.requestFocus()
                    // the following code is based on https://stackoverflow.com/questions/9409195/how-to-get-complete-address-from-latitude-and-longitude
                    itemAddressTextInputLayout.editText?.setText(tryBuildAddressStringFrom(geoPoint))
                    itemAddressTextInputLayout.editText?.clearFocus()
                })
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
        setUpItemBestByDateTextInputLayout()
        setUpItemBestByDatePicker()
        setUpLocationPickerBox()
        setUpItemIsSharedSwitch()
        setUpItemIsSharedSwitchLabel()
        setUpItemAddressTextInputLayout()
        setUpLocateMeButton()
        setUpItemDescriptionTextInputLayout()
        setUpSaveButton()
    }

    // TODO: simplify the process of checking isShared + location and isShared and contactEmail
    override fun onDestroy() {
        super.onDestroy()
        if (!ADD_ITEM_MODE) {  // **new** Items should not be saved automatically, i. e. onDestroy
            // TODO: put the following condition into a separate function
            if (model.location == null && model.isShared) {
                model.location = null
                model.geohash = null
                model.isShared = false
                alertDialogOnInvalidAddress(this).show()
            } else {
                setModelGeohashAttribute()
            }
            setModelContactNameAttribute()
            setModelContactEmailAttribute()

            // TODO: put the following condition into a separate function
            if ((model.contactEmail == null || model.contactEmail == "") && model.isShared) {
                model.isShared = false
                alertDialogOnContactEmailMissing(this).show()
            }

            ItemController.saveItem(model, { /* do nothing on success */ }, { alertDialogOnSaveItemFailed(this).show() })
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
        unit_dropdown.editText?.setText(getString(model.unit.symbolId))
        unit_dropdown.editText?.setOnClickListener { unitDropdownMenu.show() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpItemBestByDateTextInputLayout() {
        itemBestByDateTextInputLayout.editText?.setText(buildDateStringFrom(model.bestByDate))
        itemBestByDateTextInputLayout.editText?.setOnClickListener {
            switchDatePickerVisibility()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpItemBestByDatePicker() {
        hideDatePicker()
        model.bestByDate?.let { setDatePickerDate(itemBestByDatePicker, it) }
        itemBestByDatePicker.setOnDateChangedListener { _, _, _, _ ->
            model.bestByDate = getDateFrom(itemBestByDatePicker)
            itemBestByDateTextInputLayout.editText?.setText(buildDateStringFrom(model.bestByDate))
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

    private fun setUpItemAddressTextInputLayout() {
        model.location?.let { itemAddressTextInputLayout.editText?.setText(tryBuildAddressStringFrom(it)) }
        itemAddressTextInputLayout.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) model.location = tryGetGeoPointFromAddressUserInput()
        }
    }

    private fun setUpLocateMeButton() {
        locateMeButton.setOnClickListener { locationController.getCurrentLocation() }
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

    // TODO: check for leading zeros
    private fun setModelQuantityAttribute() {
        val quantityString = itemQuantityTextInputLayout.editText?.text.toString()
        model.quantity = if (quantityString.isEmpty() || quantityString.isBlank()) 0 else quantityString.toLong()

    }

    private fun setModelUnitAttribute() {
        model.unit = tryGetUnitByString()
    }

    private fun setModelGeohashAttribute() {
        if (model.location != null)
            model.geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(model.location!!.latitude, model.location!!.longitude))
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
            model.bestByDate = getDateFrom(itemBestByDatePicker)
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
    // TODO: sample for contactEmail and isShared
    // TODO: consider, removing setModel*Attribute methods
    private fun saveNewItem() {

        // TODO: put the following condition into a separate function
        if (itemIsSharedSwitch.isChecked && model.location == null) {
            alertDialogOnInvalidAddress(this).show()
            itemAddressTextInputLayout.editText?.setText("")
        } else {

            // TODO: remove set* method calls
            //model = Item(ownerReference = ownerRef)
            setModelNameAttribute()
            setModelQuantityAttribute()
            setModelUnitAttribute()
            setModelBestByDateAttribute()
            setModelIsSharedAttribute()
            setModelGeohashAttribute()
            setModelDescriptionAttribute()
            setModelContactNameAttribute()
            setModelContactEmailAttribute()

            // TODO: put the following condition into a separate function
            if ((model.contactEmail == null || model.contactEmail == "") && model.isShared) {
                model.isShared = false
                alertDialogOnContactEmailMissing(this).show()
            } else {

                ItemController.saveItem(model, {
                    // saving was successful
                    Toast.makeText(requireContext(), "Item saved", Toast.LENGTH_SHORT).show()

                    // this line of code is based on https://www.codegrepper.com/code-examples/kotlin/android+go+back+to+previous+activity+programmatically
                    activity?.onBackPressed()
                },
                    {
                        // saving newItem failed
                        alertDialogOnSaveItemFailed(this).show()
                    })

            }
        }

    }

    private fun tryGetGeoPointFromAddressUserInput(): GeoPoint? {
        var matchedGeoPoint: GeoPoint? = null
        try {
            val userInputAddress = itemAddressTextInputLayout.editText?.text.toString()
            matchedGeoPoint = locationController.getGeoPointFrom(userInputAddress)
        } catch(exc: Exception) {
            if (itemIsSharedSwitch.isChecked) itemIsSharedSwitch.toggle()
            alertDialogOnErrorParsingAddressString(this).show()
        }

        return matchedGeoPoint
    }

    private fun tryBuildAddressStringFrom(geoPoint: GeoPoint): String? {
        var addressString: String? = null
        try {
            addressString = locationController.buildAddressStringFrom(geoPoint)
        } catch (exc: IOException){
            if (itemIsSharedSwitch.isChecked) itemIsSharedSwitch.toggle()
            alertDialogOnLocationNoNetwork(this).show()
        }

        return addressString
    }

    // TODO: consider moving this into calling function?
    private fun tryGetUnitByString(): Unit {
        val symbol = unit_dropdown.editText?.text.toString()
        return Unit.getByString(symbol, this) ?: Unit.PIECE
    }

    private fun switchDatePickerVisibility() {
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