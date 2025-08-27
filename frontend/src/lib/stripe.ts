import { loadStripe, type Stripe } from '@stripe/stripe-js';

// Direct Stripe publishable key (will be obfuscated later)
const STRIPE_PUBLISHABLE_KEY =
	'pk_test_51S0UCkFCuMbaQ3eotp2Da6H8FP4dOgtEERfVZOkF5WqS7JvjWyu4FHrvFrhncOnLm40z0a2oVPKvizWOXQ31FaW100gZtDyUbf';

// Debug logging
console.log('Stripe configuration:', {
	hasKey: !!STRIPE_PUBLISHABLE_KEY,
	keyLength: STRIPE_PUBLISHABLE_KEY?.length,
	keyPrefix: STRIPE_PUBLISHABLE_KEY?.substring(0, 10) + '...'
});

if (!STRIPE_PUBLISHABLE_KEY) {
	console.warn('Stripe publishable key not found.');
}

// Lazy load Stripe to prevent errors during module initialization
let stripeInstance: Stripe | null = null;

export async function getStripe(): Promise<Stripe> {
	if (!STRIPE_PUBLISHABLE_KEY) {
		throw new Error('Stripe publishable key not found.');
	}

	if (!stripeInstance) {
		console.log('Creating new Stripe instance');
		const loadedStripe = await loadStripe(STRIPE_PUBLISHABLE_KEY);
		if (!loadedStripe) {
			throw new Error('Failed to load Stripe');
		}
		stripeInstance = loadedStripe;
		console.log('Stripe instance created successfully');
	} else {
		console.log('Returning existing Stripe instance');
	}

	return stripeInstance;
}

// For backward compatibility, export a promise that resolves to the stripe instance
export const stripe = getStripe();
