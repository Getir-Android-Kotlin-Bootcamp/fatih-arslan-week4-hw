package com.getir.patika.network_operations

import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.getir.patika.network_operations.data.model.AuthState
import com.getir.patika.network_operations.data.model.MainEvent
import com.getir.patika.network_operations.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleInsets()
        binding.initializeViews()
    }

    private fun ActivityMainBinding.initializeViews() {
        etFullName.listenTyping { viewModel.onEvent(MainEvent.OnFullNameChanged(it)) }
        etEmail.listenTyping { viewModel.onEvent(MainEvent.OnEmailChanged(it)) }
        etPassword.listenTyping { viewModel.onEvent(MainEvent.OnPasswordChanged(it)) }
        scopeWithLifecycle {
            btnRegister.setOnClickListener { viewModel.onEvent(MainEvent.OnRegisterClicked) }
            btnLogin.setOnClickListener { viewModel.onEvent(MainEvent.OnLoginClicked) }
            btnGetProfile.setOnClickListener { viewModel.onEvent(MainEvent.GetProfileClicked) }
        }

        listenAuthChanges()
        listenGetProfileChanges()
    }

    private fun listenGetProfileChanges() {
        scopeWithLifecycle {
            viewModel.uiState.map { it.profileInfo }.distinctUntilChanged()
                .collectLatest { profileInfo ->
                    binding.tvProfileInfo.text = profileInfo
                }
        }
    }

    private fun ActivityMainBinding.listenAuthChanges() {
        scopeWithLifecycle {
            viewModel.uiState.map { it.authState }.distinctUntilChanged()
                .collectLatest { authState ->
                    tvStatus.text = when (authState) {
                        is AuthState.Error -> authState.message
                        AuthState.Idle -> getString(R.string.status_idle)
                        AuthState.Loading -> getString(R.string.status_loading)
                        AuthState.Registered -> getString(R.string.register)
                        AuthState.SignedIn -> getString(R.string.status_loggedIn)
                        AuthState.ProfileRetrieved -> getString(R.string.status_profile_retrieved)
                    }
                }
        }
    }

    private fun EditText.listenTyping(onTextChange: (String) -> Unit) {
        doOnTextChanged { text, _, _, _ ->
            onTextChange(text.toString())
        }
    }

    private fun scopeWithLifecycle(block: suspend CoroutineScope.() -> Unit) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED, block = block)
        }
    }

    private fun handleInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
