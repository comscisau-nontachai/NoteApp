package nontachai.becomedev.noteapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*

const val TAG = "logd"

class LoginActivity : AppCompatActivity() {
    @SuppressLint("CommitPrefEdits")

    private val RC_SIGN_IN = 9001
    lateinit var auth:FirebaseAuth

    lateinit var dialogLoginGoogle : SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        edt_password.transformationMethod = PasswordTransformationMethod()
        edt_reg_password.transformationMethod = PasswordTransformationMethod()

        txt_signup.setOnClickListener {
            constraintLayout_register.visibility = View.VISIBLE
            constraintLayout_login.visibility = View.GONE
        }
        btn_back_to_login.setOnClickListener {
            constraintLayout_register.visibility = View.GONE
            constraintLayout_login.visibility = View.VISIBLE
        }

        //load save user data
        val sharedPref = getSharedPreferences("login_setting", Context.MODE_PRIVATE)
        val email = sharedPref.getString("email", "")
        val pass = sharedPref.getString("password", "")
        edt_username.setText(email)
        edt_password.setText(pass)



        //event on click
        btn_login.setOnClickListener {
            val username = edt_username.text.toString().trim()
            val password = edt_password.text.toString().trim()

            when {
                username.isEmpty() -> {
                    edt_username.error = "username is empty."
                    edt_username.requestFocus()
                }
                password.isEmpty() -> {
                    edt_password.error = "password is empty."
                    edt_password.requestFocus()
                }
                else -> {

                    val sharedPreferences = getSharedPreferences("login_setting", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    if (chk_remember.isChecked) {
                        editor.putString("email", username)
                        editor.putString("password", password)
                        editor.apply()
                    } else {
                        editor.putString("email", "")
                        editor.putString("password", "")
                        editor.apply()
                    }

                    login(username, password)
                }
            }
        }

        btn_register.setOnClickListener {
            val name = edt_reg_name.text.toString()
            val emailReg = edt_reg_email.text.toString()
            val password = edt_reg_password.text.toString()

            when {
                name.isEmpty() -> {
                    edt_reg_name.error = "name is empty"
                    edt_reg_name.requestFocus()
                }
                emailReg.isEmpty() -> {
                    edt_reg_email.error = "email is empty"
                    edt_reg_email.requestFocus()
                }
                password.isEmpty() -> {
                    edt_reg_password.error = "password is empty"
                    edt_reg_password.requestFocus()
                }
                else -> {
                    register(name, emailReg, password)
                }
            }
        }

        dialogLoginGoogle = SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
        auth = FirebaseAuth.getInstance()
        btn_login_google.setOnClickListener {
            loginWithGoogle()
        }

        //auto
//        val userID = auth.currentUser?.uid
//        if(userID != "null" && userID != null){
//            Log.d(TAG, "data not null $userID")
//            login(email,pass)
//        }else{
//            Log.d(TAG, "data null $userID")
//        }
        

    }

    private fun register(name: String, email: String, password: String) {
        val fUserDatabase = FirebaseDatabase.getInstance().reference.child("Users")
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Create ac success: ")
                fUserDatabase.child(firebaseAuth.currentUser!!.uid).child("basic").child("name").setValue(name)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                //success register
                                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).apply {
                                    titleText = "Register Success!!"
                                    contentText = "back to login page,to login."
                                    show()
                                }
                            } else {
                                //err
                                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).apply {
                                    titleText = "Something wrong!!"
                                    contentText = "${task.exception!!.message}"
                                    show()
                                }
                            }
                        }
            } else {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).apply {
                    titleText = "Something wrong!!"
                    contentText = "${task.exception!!.message}"
                    show()
                }
            }
        }


    }

    private fun login(email: String, password: String) {
        val dialogLoading = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE).apply {
            contentText = "Loading..."
            setCancelable(false)
            show()
        }

        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                //login success
                Log.d(TAG, ":login success ")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                dialogLoading.dismissWithAnimation()
            } else {
                //login fails
                Log.d(TAG, ":login failed ")
                dialogLoading.dismissWithAnimation()
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE).apply {
                    titleText = "Username/Password Incorrect!!"
                    contentText = "can't login please check your username/password."
                    show()
                }
            }
        }

    }


    private fun loginWithGoogle() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)

    }
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        dialogLoginGoogle.apply {
            contentText = "Loading..."
            setCancelable(false)
            show()
        }

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        //val user = auth.currentUser

                        dialogLoginGoogle.dismissWithAnimation()
                        val intent = Intent(this, MainActivity::class.java)

                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                    }
                }
    }

}
