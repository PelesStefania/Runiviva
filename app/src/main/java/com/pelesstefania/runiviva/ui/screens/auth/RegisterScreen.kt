package com.pelesstefania.runiviva.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.pelesstefania.runiviva.data.AuthRepository
import com.pelesstefania.runiviva.data.UserRepository
import com.pelesstefania.runiviva.model.AppUser

@Composable
fun RegisterScreen(navController: NavHostController) {

    var username by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }

    var confirmPassword by remember { mutableStateOf("") }

    val lightBlue = Color(0xFFD9F0FF)

    val context = LocalContext.current

    val authRepository = remember {
        AuthRepository()
    }

    val userRepository = remember {
        UserRepository()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBlue)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
            },
            label = {
                Text("Username")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text("Email")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text("Password")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
            },
            label = {
                Text("Confirm Password")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {

                if (
                    username.isBlank() ||
                    email.isBlank() ||
                    password.isBlank() ||
                    confirmPassword.isBlank()
                ) {

                    Toast.makeText(
                        context,
                        "Please fill in all fields",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@Button
                }

                if (username.trim().length < 3) {

                    Toast.makeText(
                        context,
                        "Username too short",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@Button
                }

                if (password != confirmPassword) {

                    Toast.makeText(
                        context,
                        "Passwords do not match",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@Button
                }

                userRepository.isUsernameTaken(
                    username = username,
                    onResult = { isTaken ->

                        if (isTaken) {

                            Toast.makeText(
                                context,
                                "Username already taken",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@isUsernameTaken
                        }

                        authRepository.registerUser(
                            email = email.trim(),
                            password = password,

                            onSuccess = {

                                val firebaseUser =
                                    FirebaseAuth.getInstance().currentUser

                                if (firebaseUser == null) {

                                    Toast.makeText(
                                        context,
                                        "User created, but profile could not be loaded",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    return@registerUser
                                }

                                val appUser = AppUser(
                                    uid = firebaseUser.uid,

                                    username = username.trim(),

                                    usernameLowercase =
                                        username.trim().lowercase(),

                                    email = email.trim(),


                                    totalRuns = 0,

                                    totalDistanceKm = 0.0,

                                    lastRunDate = ""
                                )

                                userRepository.saveUser(
                                    user = appUser,

                                    onSuccess = {

                                        Toast.makeText(
                                            context,
                                            "Account created successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        navController.popBackStack()
                                    },

                                    onError = { errorMessage ->

                                        Toast.makeText(
                                            context,
                                            errorMessage,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                )
                            },

                            onError = { errorMessage ->

                                Toast.makeText(
                                    context,
                                    errorMessage,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    },

                    onError = { errorMessage ->

                        Toast.makeText(
                            context,
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            },

            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(16.dp)
        ) {

            Text("Create Account")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Back to Login",

            modifier = Modifier.clickable {
                navController.popBackStack()
            }
        )
    }
}