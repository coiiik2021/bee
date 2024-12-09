// // Thêm sự kiện khi bấm vào nút Add Nav Item
// document.getElementById('addNavItemBtn').addEventListener('click', function () {
//     // Tạo một phần tử <li> mới
//     const newNavItem = document.createElement('li');
//     newNavItem.className = 'nav-item';
//
//     // Tạo thẻ <a> liên kết bên trong phần tử mới
//     const newNavLink = document.createElement('a');
//     newNavLink.className = 'nav-link';
//     newNavLink.href = '#';
//     newNavLink.textContent = `Hoá đơn ${document.querySelectorAll('#navPills .nav-item').length + 1}`;
//
//     // Thêm sự kiện click để đổi màu cho thẻ mới
//     newNavLink.addEventListener('click', handleNavClick);
//
//     // Tạo nút X để xóa nav-item
//     const closeButton = document.createElement('button');
//     closeButton.className = 'btn-close';
//     closeButton.type = 'button';
//     closeButton.setAttribute('aria-label', 'Close');
//
//     // Thêm sự kiện xóa khi bấm vào nút X
//     closeButton.addEventListener('click', function (e) {
//         e.stopPropagation(); // Ngăn chặn sự kiện click vào thẻ a
//         newNavItem.remove(); // Xóa nav-item
//     });
//
//     // Thêm thẻ <a> và nút X vào trong phần tử <li>
//     newNavItem.appendChild(newNavLink);
//     newNavItem.appendChild(closeButton);
//
//     // Thêm phần tử mới vào danh sách nav-pills
//     document.getElementById('navPills').appendChild(newNavItem);
// });
//
// // Hàm xử lý sự kiện click đổi màu
// function handleNavClick(event) {
//     // Loại bỏ lớp 'active' khỏi tất cả các thẻ
//     document.querySelectorAll('#navPills .nav-link').forEach(function (navLink) {
//         navLink.classList.remove('active');
//     });
//
//     // Thêm lớp 'active' vào thẻ được nhấn
//     event.target.classList.add('active');
// }
//
// // Thêm sự kiện click cho các thẻ ban đầu
// document.querySelectorAll('#navPills .nav-link').forEach(function (navLink) {
//     navLink.addEventListener('click', handleNavClick);
// });
