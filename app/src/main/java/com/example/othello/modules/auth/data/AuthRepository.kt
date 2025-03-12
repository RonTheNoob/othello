package com.example.othello.modules.auth.data

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

@Suppress("DEPRECATION")
class AuthRepository : AuthRepositoryInterface {
    private val auth: FirebaseAuth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    override suspend fun signInWithGoogle(context: Context, onSignIn: (user: FirebaseUser?) -> Unit) {
        try {
            // Ensure we are using an Activity context
            if (context !is Activity) {
                Log.e("FIREBASE_LOGIN", "Invalid context: Must be an Activity context")
                return
            }

            // Clear previous sessions
            Firebase.auth.signOut()
            Log.d("GOOGLE_SIGN_IN", "User signed out before signing in again.")

            val signInClient = Identity.getSignInClient(context)
            signInClient.signOut().addOnCompleteListener {
                Log.d("GOOGLE_SIGN_IN", "Google Sign-In session cleared.")
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("949619083346-0qo6b8aa2cdqcvmrm2o4hlv8rk5g3c0h.apps.googleusercontent.com")
                .setAutoSelectEnabled(false)
                .setNonce(UUID.randomUUID().toString())
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(context)

            val credentialResponse = credentialManager.getCredential(
                request = request,
                context = context,
            )
            val credential = credentialResponse.credential

            if (credential is CustomCredential) {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                    val authResult = Firebase.auth.signInWithCredential(firebaseCredential).await()
                    val user = authResult.user

                    if (user != null) {
                        // Check if user document exists in Firestore
                        val userDocRef = db.collection("Users").document(user.uid)
                        val userDoc = userDocRef.get().await()

                        if (!userDoc.exists()) {
                            // Create new user document with Google info
                            val username = user.displayName
                                ?: user.email?.substringBefore("@")
                                ?: "user_${user.uid.take(6)}"

                            val userData = hashMapOf(
                                "username" to username,
                                "email" to user.email,
                                "userId" to user.uid,
                                "wins" to 0,
                                "losses" to 0,
                                "draws" to 0,
                                "lastLogin" to Date(),
                            )

                            userDocRef.set(userData).await()
                            Log.d("GOOGLE_SIGN_IN", "New user document created for ${user.email}")
                        }

                        Log.d("GOOGLE_SIGN_IN", "Sign-in successful: ${user.email}")
                        onSignIn(user)
                    } else {
                        onSignIn(null)
                    }
                } else {
                    Log.e("GOOGLE_CREDENTIAL", "Unexpected type of credential")
                    onSignIn(null)
                }
            } else {
                Log.e("GOOGLE_CREDENTIAL", "Unexpected type of credential")
                onSignIn(null)
            }
        } catch (e: ApiException) {
            Log.e("FIREBASE_LOGIN", "Google Sign-In failed: ${e.statusCode}", e)
            onSignIn(null)
        } catch (e: Exception) {
            Log.e("FIREBASE_LOGIN", "Google Sign-In failed: ${e.message}", e)
            onSignIn(null)
        }
    }

    override suspend fun registerUser(
        username: String,
        email: String,
        password: String,
        onResult: (String?) -> Unit
    ) {
        try {
            // Check if username is already taken
            val userQuery = db.collection("Users")
                .whereEqualTo("username", username)
                .get()
                .await()

            if (!userQuery.isEmpty) {
                onResult("Username is already taken")
                return
            }

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user

            // Creates/Registers user in Firestore
            user?.let {
                val userData = hashMapOf(
                    "username" to username,
                    "email" to email,
                    "userId" to it.uid,
                    "wins" to 0,
                    "losses" to 0,
                    "draws" to 0,
                    "lastLogin" to Date(),
                )
                db.collection("Users")
                    .document(it.uid)
                    .set(userData)
                    .await()

                // Sign out immediately after registering
                auth.signOut()

                onResult(null)
        } ?: onResult("Signup failed, please try again")

        } catch(e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            onResult("The email is already in use by another account.")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "An error has occurred. Please try again.")
        }
    }

    override suspend fun signInWithUsername(username: String, password: String): String? {
        return try {
            val userDocument = db.collection("Users")
                .whereEqualTo("username", username)
                .get()
                .await()
                .documents
                .firstOrNull()

            if (userDocument == null) {
                "Username does not exist"
            } else {
                val email = userDocument.getString("email")
                    ?: return "Login error"

                try {
                    val authResult = auth.signInWithEmailAndPassword(email, password).await()
                    if (authResult.user != null) null
                    else "Username and password do not match"
                } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                    "Username and password do not match"
                } catch (e: Exception) {
                    "Login failed: ${e.message}"
                }
            }
        } catch (e:Exception) {
        "An error occurred. Please try again."
        }
    }


    override fun logout() {
        Firebase.auth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? {
        return Firebase.auth.currentUser
    }

}