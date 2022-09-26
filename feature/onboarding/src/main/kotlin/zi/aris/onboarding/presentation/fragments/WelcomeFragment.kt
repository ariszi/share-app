package zi.aris.onboarding.presentation.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import zi.aris.onboarding.R
import zi.aris.onboarding.databinding.WelcomeFragmentBinding
import zi.aris.onboarding.presentation.state.OnboardingStateContract
import zi.aris.onboarding.presentation.viewmodels.OnboardingViewModel

@AndroidEntryPoint
class WelcomeFragment : Fragment(R.layout.welcome_fragment) {
    private val viewModel: OnboardingViewModel by viewModels()
    lateinit var next: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = WelcomeFragmentBinding.bind(view)
        next = binding.next

        next.setOnClickListener {
            viewModel.consumeEvent(OnboardingStateContract.OnboardingEvent.StepWelcomeCompleted)
        }
        registerStateSubscriber()
    }

    private fun registerStateSubscriber() {
        lifecycleScope.launch { viewModel.state.collect { applyState(it) } }
    }

    private fun applyState(viewState: OnboardingStateContract.OnboardingScreenState) {
        navigate(viewState.navigation)
//        insertUserInfo(viewState.displayUserInfo)
//        renderLoading(viewState.loading)
    }

    private fun navigate(navChooser: OnboardingStateContract.UserOnboardingNavigation) {
        if (navChooser != OnboardingStateContract.UserOnboardingNavigation.Idle) {
            val action = WelcomeFragmentDirections.actionWelcomeFragmentToTcFragment()
            this.findNavController().navigate(action)
        }

    }
}

