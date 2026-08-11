package com.company.sprintreporter.controller;

import com.company.sprintreporter.config.StripeProperties;
import com.company.sprintreporter.service.subscription.SubscriptionService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks/stripe")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "stripe.enabled", havingValue = "true")
public class StripeWebhookController {

    private final StripeProperties stripeProperties;
    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeProperties.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature");
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutCompleted(event);
            case "invoice.paid" -> handleInvoicePaid(event);
            case "invoice.payment_failed" -> handlePaymentFailed(event);
            case "customer.subscription.updated" -> handleSubscriptionUpdated(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            default -> log.debug("Unhandled Stripe event type: {}", event.getType());
        }

        return ResponseEntity.ok("OK");
    }

    private void handleCheckoutCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (session == null) {
            log.error("Could not deserialize checkout session from event");
            return;
        }

        String planCode = session.getMetadata().get("plan_code");
        String subscriptionId = session.getSubscription();

        if (planCode != null && subscriptionId != null) {
            subscriptionService.activateFromWebhook(subscriptionId, planCode);
            log.info("Activated subscription {} for plan {}", subscriptionId, planCode);
        }
    }

    private void handleInvoicePaid(Event event) {
        Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (invoice == null || invoice.getSubscription() == null) {
            return;
        }

        subscriptionService.renewFromWebhook(invoice.getSubscription());
        log.info("Renewed subscription from invoice: {}", invoice.getSubscription());
    }

    private void handlePaymentFailed(Event event) {
        Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (invoice == null || invoice.getSubscription() == null) {
            return;
        }

        subscriptionService.markPastDueFromWebhook(invoice.getSubscription());
        log.warn("Payment failed for subscription: {}", invoice.getSubscription());
    }

    private void handleSubscriptionUpdated(Event event) {
        Subscription subscription = (Subscription) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (subscription == null) {
            return;
        }

        subscriptionService.syncStatusFromWebhook(subscription.getId(), subscription.getStatus());
        log.info("Subscription updated: {} → {}", subscription.getId(), subscription.getStatus());
    }

    private void handleSubscriptionDeleted(Event event) {
        Subscription subscription = (Subscription) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (subscription == null) {
            return;
        }

        subscriptionService.cancelFromWebhook(subscription.getId());
        log.info("Subscription deleted/canceled: {}", subscription.getId());
    }
}
