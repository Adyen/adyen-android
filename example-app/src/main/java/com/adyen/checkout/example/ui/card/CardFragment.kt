package com.adyen.checkout.example.ui.card

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.example.databinding.FragmentCardBinding
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.example.ui.main.NewIntentSubject
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.redirect.RedirectUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CardFragment : BottomSheetDialogFragment(), NewIntentSubject.Observer {

    @Inject
    internal lateinit var checkoutConfigurationProvider: CheckoutConfigurationProvider

    private var _binding: FragmentCardBinding? = null
    private val binding: FragmentCardBinding get() = requireNotNull(_binding)

    private val cardViewModel: CardViewModel by viewModels()

    private var redirectComponent: RedirectComponent? = null

    private var threeDS2Component: Adyen3DS2Component? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.payButton.setOnClickListener { cardViewModel.onPayClick() }

        lifecycleScope.launchWhenStarted {
            launch { cardViewModel.cardViewState.collect(::onCardViewState) }
            launch { cardViewModel.paymentResult.collect(::onPaymentResult) }
            launch { cardViewModel.additionalAction.collect(::onAdditionalAction) }
        }
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as? NewIntentSubject)?.registerObserver(this)
    }

    private fun onCardViewState(cardViewState: CardViewState) {
        when (cardViewState) {
            CardViewState.Loading -> {
                binding.progressIndicator.isVisible = true
                binding.cardContainer.isVisible = false
                binding.errorView.isVisible = false
            }
            is CardViewState.ShowComponent -> {
                binding.cardContainer.isVisible = true
                binding.progressIndicator.isVisible = false
                binding.errorView.isVisible = false

                setupCardView(cardViewState.paymentMethod)
            }
            CardViewState.Invalid -> {
                binding.cardView.highlightValidationErrors()
            }
            CardViewState.Error -> {
                binding.errorView.isVisible = true
                binding.progressIndicator.isVisible = false
                binding.cardContainer.isVisible = false
            }
        }
    }

    private fun setupCardView(paymentMethod: PaymentMethod) {
        val cardComponent = CardComponent.PROVIDER.get(
            this,
            paymentMethod,
            checkoutConfigurationProvider.getCardConfiguration()
        )

        binding.cardView.attach(cardComponent, viewLifecycleOwner)

        cardComponent.observe(viewLifecycleOwner, cardViewModel::onCardComponentState)
        cardComponent.observeErrors(viewLifecycleOwner, cardViewModel::onComponentError)
    }

    private fun onPaymentResult(result: String) {
        Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
        dismiss()
    }

    private fun onAdditionalAction(cardAction: CardAction) {
        when (cardAction) {
            is CardAction.Redirect -> setupRedirectComponent(cardAction.action)
            is CardAction.ThreeDS2 -> setupThreeDS2Component(cardAction.action)
            CardAction.Unsupported -> {
                Toast.makeText(requireContext(), "This action is not implemented", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRedirectComponent(action: Action) {
        redirectComponent = RedirectComponent.PROVIDER.get(
            this,
            requireActivity().application,
            checkoutConfigurationProvider.getRedirectConfiguration()
        )

        redirectComponent?.observe(viewLifecycleOwner, cardViewModel::onActionComponentData)
        redirectComponent?.observeErrors(viewLifecycleOwner, cardViewModel::onComponentError)

        redirectComponent?.handleAction(requireActivity(), action)
    }

    private fun setupThreeDS2Component(action: Action) {
        threeDS2Component = Adyen3DS2Component.PROVIDER.get(
            this,
            requireActivity().application,
            checkoutConfigurationProvider.get3DS2Configuration()
        )

        threeDS2Component?.observe(viewLifecycleOwner, cardViewModel::onActionComponentData)
        threeDS2Component?.observeErrors(viewLifecycleOwner, cardViewModel::onComponentError)

        threeDS2Component?.handleAction(requireActivity(), action)
    }

    override fun onNewIntent(intent: Intent) {
        val data = intent.data
        if (data != null && data.toString().startsWith(RedirectUtil.REDIRECT_RESULT_SCHEME)) {
            redirectComponent?.handleIntent(intent)
            threeDS2Component?.handleIntent(intent)
        }
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as? NewIntentSubject)?.unregisterObserver(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        redirectComponent = null
        _binding = null
    }

    companion object {
        fun show(fragmentManager: FragmentManager) = CardFragment().show(fragmentManager, null)
    }
}
