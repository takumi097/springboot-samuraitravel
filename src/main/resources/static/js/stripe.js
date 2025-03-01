const stripe = Stripe('pk_test_51Qwzd7PDQPhxURP66e0K4PK17NpDSQq16U0Puk7IVJmJ13plVkq1J5ZwReULzGWAnQSC0ZMJnXJ48ERwYAMjozKz00V1lYk1pw');
const paymentButton = document.querySelector('#paymentButton');

paymentButton.addEventListener('click', () => {
 stripe.redirectToCheckout({
   sessionId: sessionId
 })
});