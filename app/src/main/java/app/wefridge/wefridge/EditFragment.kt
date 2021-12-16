package app.wefridge.wefridge

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
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
import app.wefridge.wefridge.exceptions.InternetUnavailableException
import app.wefridge.wefridge.exceptions.ItemIsSharedWithoutContactEmailException
import app.wefridge.wefridge.exceptions.ItemIsSharedWithoutLocationException
import app.wefridge.wefridge.model.*
import app.wefridge.wefridge.model.Unit
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.GeoPoint
import java.io.IOException

const val ARG_OWNER = "owner"
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

        // this line of code was partially inspired by https://stackoverflow.com/questions/11741270/android-sharedpreferences-in-fragment
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        setModel()

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            model.firebaseId?.let { model.name } ?: getString(R.string.add_new_item)

        setUpLocationController()
    }

    private fun setUpLocationController() {
        locationController = LocationController(this,
            callbackOnPermissionDenied = { alertDialogOnLocationPermissionDenied(requireContext()) },
            callbackForPermissionRationale = { callback ->
                alertDialogForLocationPermissionRationale(requireContext()).setPositiveButton(
                    android.R.string.ok
                ) { _, _ ->
                    callback(true)
                    locationController.getCurrentLocation()
                }
            },
            callbackOnDeterminationFailed = { alertDialogOnUnableToDetermineLocation(requireContext()) },
            callbackOnSuccess = { geoPoint ->
                model.location = geoPoint
                model.geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(model.location!!.latitude, model.location!!.longitude))
                // the following code is based on https://stackoverflow.com/questions/9409195/how-to-get-complete-address-from-latitude-and-longitude
                binding.addressTextInputLayout.editText?.setText(tryBuildAddressStringFrom(geoPoint))
            })
    }

    private fun setModel() {
        val ownerReference = arguments?.getString(ARG_OWNER)?.let { UserController.getUserRef(it) }
            ?: UserController.getCurrentUserRef()
        model = arguments?.getParcelable(ARG_MODEL) ?: Item(ownerReference = ownerReference)

        model.contactName = UserController.getLocalName(sharedPreferences)
        model.contactEmail = UserController.getLocalEmail(sharedPreferences)
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

        setUpNameTextInputLayout()
        setUpItemQuantityTextInputLayout()
        setUpUnitDropdown()
        setUpBestByDateTextInputLayout()
        setUpBestByDatePicker()
        setUpLocationPickerBox()
        setUpIsSharedSwitch()
        setUpIsSharedSwitchLabel()
        setUpAddressTextInputLayout()
        setUpLocateMeButton()
        setUpDescriptionTextInputLayout()
        setUpSaveButton()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !internetAvailable(requireContext()))
            toastOnSaveItemWithInternetUnavailable(requireContext()).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!ADD_ITEM_MODE) {  // **new** Items shall not be saved automatically, i. e. onDestroy
            try {
                saveItem(
                    callbackOnFailure = { alertDialogOnItemNotSaved(requireContext()).show() }
                )
            } catch (exc: InternetUnavailableException) {
                Log.e("EditFragment", "Error before saving Item: ", exc)
                alertDialogOnItemNotSaved(requireContext()).show()

            } catch (exc: ItemIsSharedWithoutContactEmailException) {
                Log.e("EditFragment", "Error before saving Item: ", exc)
                alertDialogOnContactEmailMissingOnDestroy(requireContext()).show()

            } catch (exc: ItemIsSharedWithoutLocationException) {
                Log.e("EditFragment", "Error before saving Item: ", exc)
                alertDialogOnErrorParsingAddressStringOnDestroy(requireContext()).show()
            }
        }
    }

    private fun setUpNameTextInputLayout() {
        binding.nameTextInputLayout.editText?.addTextChangedListener {
            model.name = binding.nameTextInputLayout.editText?.text.toString()
        }

        binding.nameTextInputLayout.editText?.setText(model.name)

    }

    private fun setUpItemQuantityTextInputLayout() {
        binding.quantityTextInputLayout.editText?.addTextChangedListener {
            val quantityString = binding.quantityTextInputLayout.editText?.text.toString()
            model.quantity = if (quantityString.isBlank()) 0 else quantityString.toLong()
        }

        // the following code lines are partially inspired by https://stackoverflow.com/questions/69655474/android-material-text-input-layout-end-icon-not-visible-but-working
        val originalOnFocusChangeListener = binding.quantityTextInputLayout.editText?.onFocusChangeListener
        binding.quantityTextInputLayout.editText?.setOnFocusChangeListener { view: View, hasFocus: Boolean ->
            originalOnFocusChangeListener?.onFocusChange(view, hasFocus)
            if (!hasFocus) {
                binding.quantityTextInputLayout.editText?.setText(model.quantity.toString())
                binding.quantityTextInputLayout.isEndIconVisible = false
            }
            if (hasFocus && binding.quantityTextInputLayout.editText?.text?.isNotEmpty() == true) {
                binding.quantityTextInputLayout.isEndIconVisible = true
            }
        }

        binding.quantityTextInputLayout.editText?.setText(model.quantity.toString())
    }

    private fun setUpUnitDropdown() {
        unitDropdownMenu = PopupMenu(requireActivity(), binding.unitDropdown)
        for (unit in Unit.values()) unitDropdownMenu.menu.add(Menu.NONE, unit.value, Menu.NONE, unit._display)
        unitDropdownMenu.setOnMenuItemClickListener { menuItem ->
            binding.unitDropdown.editText?.setText(menuItem.title)
            model.unit = Unit.getByValue(menuItem.itemId) ?: Unit.PIECE
            true
        }

        binding.unitDropdown.editText?.setText(model.unit.display(requireContext()))
        binding.unitDropdown.editText?.setOnClickListener { unitDropdownMenu.show() }
        binding.unitDropdown.setEndIconOnClickListener { unitDropdownMenu.show() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpBestByDateTextInputLayout() {
        binding.bestByDateTextInputLayout.editText?.inputType = InputType.TYPE_NULL
        binding.bestByDateTextInputLayout.editText?.setOnClickListener { _: View ->
            if (binding.bestByDateTextInputLayout.editText?.hasFocus() == true)
                binding.bestByDateTextInputLayout.editText?.clearFocus()
        }

        val originalOnFocusChangeListener = binding.bestByDateTextInputLayout.editText?.onFocusChangeListener
        binding.bestByDateTextInputLayout.editText?.setOnFocusChangeListener { view: View, hasFocus: Boolean ->
            switchDatePickerVisibility()
            originalOnFocusChangeListener?.onFocusChange(view, hasFocus)
            if (hasFocus && binding.bestByDateTextInputLayout.editText?.text?.isNotEmpty() == true) {
                binding.bestByDateTextInputLayout.isEndIconVisible = true
            }
        }

        binding.bestByDateTextInputLayout.setEndIconOnClickListener {
            model.bestByDate = null
            binding.bestByDateTextInputLayout.editText?.setText("")
            binding.bestByDateTextInputLayout.editText?.clearFocus()
        }
        binding.bestByDateTextInputLayout.editText?.setText(buildDateStringFrom(model.bestByDate))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpBestByDatePicker() {
        hideDatePicker()
        model.bestByDate?.let { setDatePickerDate(binding.bestByDatePicker, it) }
        binding.bestByDatePicker.setOnDateChangedListener { _, _, _, _ ->
            model.bestByDate = getDateFrom(binding.bestByDatePicker)
            binding.bestByDateTextInputLayout.editText?.setText(buildDateStringFrom(model.bestByDate))

            if (binding.bestByDateTextInputLayout.editText?.text.toString() == "") model.bestByDate = null
            else model.bestByDate = getDateFrom(binding.bestByDatePicker)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpLocationPickerBox() {
        setLocationPickerActivation()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpIsSharedSwitch() {
        binding.isSharedSwitch.isChecked = model.isShared
        binding.isSharedSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && model.contactEmail == null) {
                binding.isSharedSwitch.toggle() // turn off again
                alertDialogOnContactEmailMissing(requireContext()).show()
            } else {
                model.isShared = isChecked
                setLocationPickerActivation()
            }
        }
    }

    private fun setUpIsSharedSwitchLabel() {
        binding.isSharedSwitchLabel.setOnClickListener {
            binding.isSharedSwitch.toggle()
        }
    }

    private fun setUpAddressTextInputLayout() {
        model.location?.let { binding.addressTextInputLayout.editText?.setText(tryBuildAddressStringFrom(it)) }

        val originalOnFocusChangeListener = binding.addressTextInputLayout.editText?.onFocusChangeListener
        binding.addressTextInputLayout.editText?.setOnFocusChangeListener { view, hasFocus ->
            originalOnFocusChangeListener?.onFocusChange(view, hasFocus)
            if (!hasFocus) {
                binding.addressTextInputLayout.isEndIconVisible = false
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
                    alertDialogOnErrorParsingAddressString(requireContext()).show()
                }
            }

            if (hasFocus && binding.addressTextInputLayout.editText?.text?.isNotEmpty() == true) {
                binding.addressTextInputLayout.isEndIconVisible = true
            }
        }
    }

    private fun setUpLocateMeButton() {
        binding.locateMeButton.setOnClickListener { locationController.getCurrentLocation() }
    }

    private fun setUpDescriptionTextInputLayout() {
        binding.descriptionTextInputLayout.editText?.setText(model.description)
        binding.descriptionTextInputLayout.editText?.addTextChangedListener {
            model.description = binding.descriptionTextInputLayout.editText?.text.toString()
        }
    }

    private fun setUpSaveButton() {
        if (!ADD_ITEM_MODE) return

        binding.saveButton.isVisible = true
        binding.saveButton.setOnClickListener {
            binding.addressTextInputLayout.clearFocus()

            try {
                saveItem(
                    callbackOnSuccess = {
                        Toast.makeText(
                            requireContext(),
                            R.string.new_item_saved_toast,
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    callbackOnFailure = { alertDialogOnItemNotSaved(requireContext()).show() }
                )

            } catch (exc: InternetUnavailableException) {
                Log.e("EditFragment", "Error before saving Item: ", exc)
                toastOnSaveItemWithInternetUnavailable(requireContext()).show()
            } catch (exc: ItemIsSharedWithoutContactEmailException) {
                Log.e("EditFragment", "Error before saving Item: ", exc)
                alertDialogOnContactEmailMissing(requireContext()).show()

            } catch (exc: ItemIsSharedWithoutLocationException) {
                Log.e("EditFragment", "Error before saving Item: ", exc)
                alertDialogOnErrorParsingAddressString(requireContext()).show()
            }
        }
    }

    private fun saveItem(callbackOnSuccess: (() -> kotlin.Unit)? = null, callbackOnFailure: ((Exception) -> kotlin.Unit)? = null) {
        // for devices running Android 7.0 or higher, check additionally if internet is available.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !internetAvailable(requireContext()))
            throw InternetUnavailableException("Failed to save Item: Internet unavailable.")

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
            val userInputAddress = binding.addressTextInputLayout.editText?.text.toString()
            matchedGeoPoint = LocationController.getGeoPointFrom(userInputAddress, requireContext())
        } catch(exc: Exception) {
            Log.e("EditFragment", "Error while parsing address from user input: ", exc)
        }

        return matchedGeoPoint
    }

    private fun tryBuildAddressStringFrom(geoPoint: GeoPoint): String? {
        var addressString: String? = null
        try {
            addressString = LocationController.buildAddressStringFrom(geoPoint, requireContext())
        } catch (exc: IOException){
            if (binding.isSharedSwitch.isChecked) binding.isSharedSwitch.toggle()
            alertDialogOnLocationNoInternetConnection(requireContext()).show()
        }

        return addressString
    }

    private fun switchDatePickerVisibility() {
        when (binding.bestByDatePicker.visibility) {
            View.GONE -> showDatePicker()
            View.INVISIBLE -> showDatePicker()
            View.VISIBLE -> hideDatePicker()
        }

    }

    private fun showDatePicker() {
        binding.bestByDatePicker.visibility = View.VISIBLE
    }

    private fun hideDatePicker() {
        binding.bestByDatePicker.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setLocationPickerActivation() {
        if (model.isShared) {
            enable(binding.locateMeButton)
            enable(binding.addressTextInputLayout, InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE)
            enable(binding.descriptionTextInputLayout, InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE)
        }
        else {
            disable(binding.locateMeButton)
            disable(binding.addressTextInputLayout)
            disable(binding.descriptionTextInputLayout)
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