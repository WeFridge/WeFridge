package app.wefridge.wefridge

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import app.wefridge.wefridge.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode != RESULT_OK) {
            // Sign in failed.
            return authWall()
        }
        // Successfully signed in
        val user = FirebaseAuth.getInstance().currentUser ?: return authWall()

        if (response?.isNewUser == true) {
            // create firestore user document
            val db = Firebase.firestore
            // TODO: create user data model
            val userDoc = hashMapOf(
                "name" to user.displayName,
                "email" to user.email,
                "image" to user.photoUrl.toString()
            )
            db.collection(USERS_COLLECTION_NAME).document(user.uid)
                .set(userDoc)
                .addOnFailureListener { e ->
                    Log.w("Auth", "Error creating user document", e)
                    // on error, delete registration
                    AuthUI.getInstance()
                        .delete(this)
                        .addOnCompleteListener {
                            authWall()
                        }
                }
                .addOnSuccessListener {
                    Log.d("Auth", "User document ${user.uid} successfully created!")

                    Toast.makeText(
                        this,
                        getString(R.string.auth_success_welcome, user.displayName),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {

            Toast.makeText(
                this,
                getString(R.string.auth_success_welcome_back, user.displayName),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    internal fun authWall() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.GitHubBuilder().build()
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.mipmap.fridge_show_image_round)
            .setTheme(R.style.Theme_WeFridge)
            .build()
        signInLauncher.launch(signInIntent)
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            authWall()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        navController.addOnDestinationChangedListener { controller, destination, _ ->
            binding.toolbar.title = controller.graph.findNode(destination.id)?.label
        }

        binding.bottomNav.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Menu is currently unused,
        // uncomment the following line if the action bar menu should be used again.
        // menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}