package zi.aris.user

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import zi.aris.user.databinding.UserProfileFragmentBinding
import javax.inject.Inject

@AndroidEntryPoint
class UserFragment : Fragment(R.layout.user_profile_fragment) {

    @Inject
    lateinit var router: UserProfileRouter

    private val viewModel: UserProfileViewModel by viewModels()

    private lateinit var name: TextView
    private lateinit var lastName: TextView
    private lateinit var signOut: TextView
    private lateinit var email: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = UserProfileFragmentBinding.bind(view)
        name = binding.nameTv
        lastName = binding.lastNameTv
        signOut = binding.signOutBtn
        email = binding.emailTv

        registerStateSubscriber()
        setupViewListeners()
        userLandedOnProfileScreen()
    }

    private fun registerStateSubscriber() {
        lifecycleScope.launch { viewModel.state.collect { applyState(it) } }
    }

    private fun applyState(viewState: UserProfileContract.UserProfileState) {
        navigate(viewState.navigation)
        insertUserInfo(viewState.displayUserData)
    }

    private fun navigate(navigateToStep: UserProfileContract.UserNavigation) {
        when (navigateToStep) {
            is UserProfileContract.UserNavigation.NavigateToOnboarding -> {
                navigateToOnboarding()
            }
            is UserProfileContract.UserNavigation.ExitApp -> {
                exitApp()
            }
            else -> {}
        }
        viewModel.consumeEvent(UserProfileContract.UserProfileEvent.CleanNavigationEffect)
    }

    private fun insertUserInfo(userInfo: UserProfileContract.UserData) {
        if (userInfo is UserProfileContract.UserData.UserInfo) {
            name.text = Editable.Factory.getInstance().newEditable(userInfo.name)
            lastName.text = Editable.Factory.getInstance().newEditable(userInfo.lastName)
            email.text = Editable.Factory.getInstance().newEditable(userInfo.email)
        }
    }

    private fun exitApp() {

    }

    private fun navigateToOnboarding() {
        router.navigateFromProfileToOnBoarding()
        viewModel.consumeEvent(UserProfileContract.UserProfileEvent.CleanUserInfoEffect)
    }

    private fun setupViewListeners() {
        signOut.setOnClickListener {
            viewModel.consumeEvent(
                UserProfileContract.UserProfileEvent.UserClickedOnSignOut
            )
        }
    }


    private fun userLandedOnProfileScreen() {
        viewModel.consumeEvent(UserProfileContract.UserProfileEvent.UserLandedOnProfile)
    }

}
