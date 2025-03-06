// star-rating.js
document.addEventListener('DOMContentLoaded', function() {
  const stars = document.querySelectorAll('.star-rating span');
  const ratingValue = document.getElementById('star-rating-value');

  stars.forEach(star => {
    star.addEventListener('click', function() {
      stars.forEach(s => s.classList.remove('selected'));
      this.classList.add('selected');
      ratingValue.value = this.getAttribute('data-value');
    });

    star.addEventListener('mouseover', function() {
      stars.forEach(s => s.classList.remove('hover'));
      this.classList.add('hover');
    });

    star.addEventListener('mouseout', function() {
      stars.forEach(s => s.classList.remove('hover'));
    });
  });
});