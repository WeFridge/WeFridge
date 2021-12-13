package app.wefridge.wefridge

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
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
import app.wefridge.wefridge.model.Item
import app.wefridge.wefridge.model.ItemController
import app.wefridge.wefridge.exceptions.ItemIsSharedWithoutContactEmailException
import app.wefridge.wefridge.exceptions.ItemIsSharedWithoutLocationException
import app.wefridge.wefridge.model.*
import app.wefridge.wefridge.model.Unit
import app.wefridge.wefridge.model.UserController
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
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

        val ownerReference = UserController.getCurrentUserRef()

        // this line of code was partially inspired by https://stackoverflow.com/questions/11741270/android-sharedpreferences-in-fragment
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        setModel(ownerReference = ownerReference)

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            model.firebaseId?.let { model.name } ?: getString(R.string.add_new_item)

        setUpLocationController()
    }

    private fun setUpLocationController() {
        locationController = LocationController(this,
            callbackOnPermissionDenied = { alertDialogOnLocationPermissionDenied(this) },
            callbackForPermissionRationale = { alertDialogForLocationPermissionRationale(this) },
            callbackOnDeterminationFailed = { alertDialogForLocationDeterminationFailed(this) },
            callbackOnSuccess = { geoPoint ->
                model.location = geoPoint
                model.geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(model.location!!.latitude, model.location!!.longitude))
                binding.itemAddressTextInputLayout.editText?.requestFocus()
                // the following code is based on https://stackoverflow.com/questions/9409195/how-to-get-complete-address-from-latitude-and-longitude
                binding.itemAddressTextInputLayout.editText?.setText(tryBuildAddressStringFrom(geoPoint))
                binding.itemAddressTextInputLayout.editText?.clearFocus()
            })
    }

    private fun setModel(ownerReference: DocumentReference) {
        model = arguments?.getParcelable(ARG_MODEL) ?: Item(ownerReference = ownerReference)

        UserController.getCurrentUser().let {
            val userNameAsFallback = it?.displayName
            val userEmailAsFallback = it?.email
            model.contactName = sharedPreferences.getString(SETTINGS_NAME, userNameAsFallback)
            model.contactEmail = sharedPreferences.getString(SETTINGS_EMAIL, userEmailAsFallback)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

    override fun onDestroy() {
        super.onDestroy()
        if (!ADD_ITEM_MODE) {  // **new** Items shall not be saved automatically, i. e. onDestroy
            try {
                saveItem(
                    callbackOnFailure = { alertDialogOnSaveItemFailed(this).show() }
                )

            } catch (exc: ItemIsSharedWithoutContactEmailException) {
                Log.e("EditFragment", "Error while before saving Item: ", exc)
                alertDialogOnContactEmailMissingOnDestroy(this).show()

            } catch (exc: ItemIsSharedWithoutLocationException) {
                Log.e("EditFragment", "Error before saving Item: ", exc)
                alertDialogOnErrorParsingAddressStringOnDestroy(this).show()
            }
        }
    }

    private fun setUpItemNameTextInputLayout() {
        binding.itemNameTextInputLayout.editText?.addTextChangedListener {
            model.name = binding.itemNameTextInputLayout.editText?.text.toString()
        }

        binding.itemNameTextInputLayout.editText?.setText(model.name)

    }

    private fun setUpItemQuantityTextInputLayout() {
        binding.itemQuantityTextInputLayout.editText?.addTextChangedListener {
            val quantityString = binding.itemQuantityTextInputLayout.editText?.text.toString()
            model.quantity = if (quantityString.isBlank()) 0 else quantityString.toLong()
        }

        // the following code lines are partially inspired by https://stackoverflow.com/questions/69655474/android-material-text-input-layout-end-icon-not-visible-but-working
        val originalOnFocusChangeListener = binding.itemQuantityTextInputLayout.editText?.onFocusChangeListener
        binding.itemQuantityTextInputLayout.editText?.setOnFocusChangeListener { view: View, hasFocus: Boolean ->
            originalOnFocusChangeListener?.onFocusChange(view, hasFocus)
            if (!hasFocus) {
                binding.itemQuantityTextInputLayout.editText?.setText(model.quantity.toString())
                binding.itemQuantityTextInputLayout.isEndIconVisible = false
            }
            if (hasFocus && binding.itemQuantityTextInputLayout.editText?.text?.isNotEmpty() == true) {
                binding.itemQuantityTextInputLayout.isEndIconVisible = true
            }
        }

        binding.itemQuantityTextInputLayout.editText?.setText(model.quantity.toString())
    }

    private fun setUpUnitDropdown() {
        unitDropdownMenu = PopupMenu(requireActivity(), binding.unitDropdown)
        for (unit in Unit.values()) unitDropdownMenu.menu.add(unit._display)
        unitDropdownMenu.setOnMenuItemClickListener { menuItem ->
            binding.unitDropdown.editText?.setText(menuItem.title)
            model.unit = tryGetUnitByString()
            true
        }

        binding.unitDropdown.editText?.setText(model.unit.display(requireContext()))
        binding.unitDropdown.editText?.setOnClickListener { unitDropdownMenu.show() }
        binding.unitDropdown.setEndIconOnClickListener { unitDropdownMenu.show() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpItemBestByDateTextInputLayout() {
        binding.itemBestByDateTextInputLayout.editText?.inputType = InputType.TYPE_NULL
        binding.itemBestByDateTextInputLayout.editText?.setOnClickListener { _: View ->
            if (binding.itemBestByDateTextInputLayout.editText?.hasFocus() == true)
                binding.itemBestByDateTextInputLayout.editText?.clearFocus()
        }

        val originalOnFocusChangeListener = binding.itemBestByDateTextInputLayout.editText?.onFocusChangeListener
        binding.itemBestByDateTextInputLayout.editText?.setOnFocusChangeListener { view: View, hasFocus: Boolean ->
            switchDatePickerVisibility()
            originalOnFocusChangeListener?.onFocusChange(view, hasFocus)
            if (hasFocus && binding.itemBestByDateTextInputLayout.editText?.text?.isNotEmpty() == true) {
                binding.itemBestByDateTextInputLayout.isEndIconVisible = true
            }
        }

        binding.itemBestByDateTextInputLayout.setEndIconOnClickListener {
            model.bestByDate = null
            binding.itemBestByDateTextInputLayout.editText?.setText("")
            binding.itemBestByDateTextInputLayout.editText?.clearFocus()
        }
        binding.itemBestByDateTextInputLayout.editText?.setText(buildDateStringFrom(model.bestByDate))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpItemBestByDatePicker() {
        hideDatePicker()
        model.bestByDate?.let { setDatePickerDate(binding.itemBestByDatePicker, it) }
        binding.itemBestByDatePicker.setOnDateChangedListener { _, _, _, _ ->
            model.bestByDate = getDateFrom(binding.itemBestByDatePicker)
            binding.itemBestByDateTextInputLayout.editText?.setText(buildDateStringFrom(model.bestByDate))

            if (binding.itemBestByDateTextInputLayout.editText?.text.toString() == "") model.bestByDate = null
            else model.bestByDate = getDateFrom(binding.itemBestByDatePicker)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpLocationPickerBox() {
        setLocationPickerActivation()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpItemIsSharedSwitch() {
        binding.itemIsSharedSwitch.isChecked = model.isShared
        binding.itemIsSharedSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && model.contactEmail == null) {
                binding.itemIsSharedSwitch.toggle() // turn off again
                alertDialogOnContactEmailMissing(this).show()
            } else {
                model.isShared = isChecked
                setLocationPickerActivation()
            }
        }
    }

    private fun setUpItemIsSharedSwitchLabel() {
        binding.itemIsSharedSwitchLabel.setOnClickListener {
            binding.itemIsSharedSwitch.toggle()
        }
    }

    private fun setUpItemAddressTextInputLayout() {
        model.location?.let { binding.itemAddressTextInputLayout.editText?.setText(tryBuildAddressStringFrom(it)) }
        binding.itemAddressTextInputLayout.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                model.location = tryGetGeoPointFromAddressUserInput()
                if (model.location != null) {
                    model.geohash = GeoFireUtils.getGeoHashForLocation(
                        GeoLocation(
                            model.location!!.latitude,
                            model.location!!.longitude
                        )
                    )
                } else {
                    model.geohash = null
                    alertDialogOnErrorParsingAddressString(this).show()
                }
            }
        }
    }

    private fun setUpLocateMeButton() {
        binding.locateMeButton.setOnClickListener { locationController.getCurrentLocation() }
    }

    private fun setUpItemDescriptionTextInputLayout() {
        binding.itemDescriptionTextInputLayout.editText?.setText(model.description)
        binding.itemDescriptionTextInputLayout.editText?.addTextChangedListener {
            model.description = binding.itemDescriptionTextInputLayout.editText?.text.toString()
        }
    }

    private fun setUpSaveButton() {
        if (!ADD_ITEM_MODE) {
            binding.itemSaveButton.isVisible = false
        } else {
            binding.itemSaveButton.setOnClickListener {
                binding.itemAddressTextInputLayout.clearFocus()

                try {
                    saveItem(
                        callbackOnSuccess = {
                            Toast.makeText(
                                requireContext(),
                                R.string.toast_text_on_new_item_saved,
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        callbackOnFailure = { alertDialogOnSaveItemFailed(this).show() }
                    )

                } catch (exc: ItemIsSharedWithoutContactEmailException) {
                    Log.e("EditFragment", "Error while before saving Item: ", exc)
                    alertDialogOnContactEmailMissing(this).show()

                } catch (exc: ItemIsSharedWithoutLocationException) {
                    Log.e("EditFragment", "Error before saving Item: ", exc)
                    alertDialogOnErrorParsingAddressString(this).show()
                }
            }
        }
    }

    private fun saveItem(callbackOnSuccess: (() -> kotlin.Unit)? = null, callbackOnFailure: ((Exception) -> kotlin.Unit)? = null) {
        if ((model.isShared && model.location != null) || !model.isShared) {
            if ((model.isShared && model.contactEmail != null) || !model.isShared) {
                ItemController.saveItem(model, {
                    // saving was successful
                    if (callbackOnSuccess != null) callbackOnSuccess()

                    // this line of code is based on https://www.codegrepper.com/code-examples/kotlin/android+go+back+to+previous+activity+programmatically
                    activity?.onBackPressed() // finally, close the EditFragment
                },
                    { exception ->
                        // saving newItem failed
                        if (callbackOnFailure != null) callbackOnFailure(exception)
                        Log.e("EditFragment", "Error while saving Item: ", exception)
                    })
            } else {
                throw ItemIsSharedWithoutContactEmailException()
            }

        } else {
            throw ItemIsSharedWithoutLocationException()
        }
    }

    private fun tryGetGeoPointFromAddressUserInput(): GeoPoint? {
        var matchedGeoPoint: GeoPoint? = null
        try {
            val userInputAddress = binding.itemAddressTextInputLayout.editText?.text.toString()
            matchedGeoPoint = locationController.getGeoPointFrom(userInputAddress)
        } catch(exc: Exception) {
            Log.e("EditFragment", "Error while parsing address from user input: ", exc)
        }

        return matchedGeoPoint
    }

    private fun tryBuildAddressStringFrom(geoPoint: GeoPoint): String? {
        var addressString: String? = null
        try {
            addressString = locationController.buildAddressStringFrom(geoPoint)
        } catch (exc: IOException){
            if (binding.itemIsSharedSwitch.isChecked) binding.itemIsSharedSwitch.toggle()
            alertDialogOnLocationNoNetwork(this).show()
        }

        return addressString
    }

    private fun tryGetUnitByString(): Unit {
        val symbol = binding.unitDropdown.editText?.text.toString()
        return Unit.getByString(symbol, this) ?: Unit.PIECE
    }

    private fun switchDatePickerVisibility() {
        when (binding.itemBestByDatePicker.visibility) {
            View.GONE -> showDatePicker()
            View.INVISIBLE -> showDatePicker()
            View.VISIBLE -> hideDatePicker()
        }

    }

    private fun showDatePicker() {
        binding.itemBestByDatePicker.visibility = View.VISIBLE
    }

    private fun hideDatePicker() {
        binding.itemBestByDatePicker.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setLocationPickerActivation() {
        if (model.isShared) {
            enable(binding.locateMeButton)
            enable(binding.itemAddressTextInputLayout, InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE)
            enable(binding.itemDescriptionTextInputLayout, InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE)
        }
        else {
            disable(binding.locateMeButton)
            disable(binding.itemAddressTextInputLayout)
            disable(binding.itemDescriptionTextInputLayout)
        }

    }

    private fun enable(view: View) {
        view.isClickable = true
        view.alpha = 1f
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun enable(textInputLayout: TextInputLayout, inputType: Int) {
        textInputLayout.editText?.isEnabled = true
        textInputLayout.editText?.focusable = View.FOCUSABLE
        textInputLayout.editText?.isFocusableInTouchMode = true
        textInputLayout.editText?.inputType = inputType
        textInputLayout.alpha = 1f
    }

    private fun disable(view: View) {
        view.isClickable = false
        view.alpha = .5f
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun disable(textInputLayout: TextInputLayout) {
        textInputLayout.editText?.isEnabled = false
        textInputLayout.editText?.focusable = View.NOT_FOCUSABLE
        textInputLayout.editText?.isFocusableInTouchMode = false
        textInputLayout.editText?.inputType = InputType.TYPE_NULL
        textInputLayout.alpha = .5f
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