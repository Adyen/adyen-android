package com.adyen.checkout.example.ui.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.databinding.FragmentCardBinding
import com.adyen.checkout.example.repositories.paymentMethods.PaymentsRepository
import com.adyen.checkout.example.service.getPaymentMethodRequest
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class CardFragment : BottomSheetDialogFragment() {

    @Inject
    internal lateinit var checkoutConfigurationProvider: CheckoutConfigurationProvider

    @Inject
    internal lateinit var paymentsRepository: PaymentsRepository

    @Inject
    internal lateinit var keyValueStorage: KeyValueStorage

    private var _binding: FragmentCardBinding? = null
    private val binding: FragmentCardBinding get() = requireNotNull(_binding)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            val paymentMethod = paymentsRepository.getPaymentMethods(getPaymentMethodRequest(keyValueStorage))
                ?.paymentMethods
                ?.firstOrNull { it.type == "scheme" }

            paymentMethod ?: return@launch

            withContext(Dispatchers.Main) {
                val cardComponent = CardComponent.PROVIDER.get(
                    this@CardFragment,
                    paymentMethod,
                    checkoutConfigurationProvider.getCardConfiguration()
                )

                binding.cardView.attach(cardComponent, viewLifecycleOwner)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun show(fragmentManager: FragmentManager) = CardFragment().show(fragmentManager, null)
    }
}
