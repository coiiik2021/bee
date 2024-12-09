const hamBurger = document.querySelector(".toggle-btn");

hamBurger.addEventListener("click", function () {
  document.querySelector("#sidebar").classList.toggle("expand");
});
document.addEventListener("DOMContentLoaded", function() {
  const userAvatar = document.getElementById("user-avatar");
  if (userAvatar) {
    userAvatar.onerror = function() {
      userAvatar.src = '/account.png'; // Ảnh mặc định
    };
  }
});
