package com.gestion.gestion_bus.service.imp;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

	@Value("${stripe.api.key}")
	private String apiKey;

	@Value("${app.frontend-url:http://localhost:7357}")
	private String frontendUrl;

	@PostConstruct
	public void init() {
		Stripe.apiKey = apiKey;
	}

	public String createCheckoutSession(
			String priceId,
			Long userId,
			Long typeAbonnementId,
			String userEmail
	) throws StripeException {

		if (priceId == null || priceId.isBlank()) {
			throw new IllegalArgumentException("priceId Stripe manquant");
		}

		String successUrl = frontendUrl + "payment-success?session_id={CHECKOUT_SESSION_ID}";
		String cancelUrl  = frontendUrl + "payment-cancel";

		SessionCreateParams params = SessionCreateParams.builder()
				.setMode(SessionCreateParams.Mode.SUBSCRIPTION)
				.setSuccessUrl(successUrl)
				.setCancelUrl(cancelUrl)
				.putMetadata("userId", userId.toString())
				.putMetadata("typeAbonnementId", typeAbonnementId.toString())
				.setCustomerEmail(userEmail)
				.addLineItem(
						SessionCreateParams.LineItem.builder()
								.setQuantity(1L)
								.setPrice(priceId)
								.build()
				)
				.build();

		Session session = Session.create(params);
		return session.getUrl();
	}

	public boolean isSessionPaid(String sessionId) {
		try {
			SessionRetrieveParams params = SessionRetrieveParams.builder().build();
			Session session = Session.retrieve(sessionId, params, null);
			String paymentStatus = session.getPaymentStatus(); // "paid" | "unpaid" | "no_payment_required"
			String status = session.getStatus();               // "complete" | "expired" | "open"
			return "paid".equals(paymentStatus) || "complete".equals(status);
		} catch (StripeException e) {
			System.out.println("[StripeService] Erreur vérification session : " + e.getMessage());
			return false;
		}
	}

	public Session retrieveSession(String sessionId) throws StripeException {
		return Session.retrieve(sessionId);
	}
}
