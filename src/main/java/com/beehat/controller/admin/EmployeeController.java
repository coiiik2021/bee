package com.beehat.controller.admin;

import com.beehat.entity.Customer;
import com.beehat.entity.Employee;
import com.beehat.repository.EmployeeRepo;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/employee")
public class EmployeeController {
    @Autowired
    EmployeeRepo employeeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @ModelAttribute("iconTitle")
    String iconTitle() {
        return "ph ph-user-list fs-3";
    }
    @ModelAttribute("pageTitle")
    String pageTitle() {
        return "Nhân viên";
    }
    @GetMapping
    public String employee(
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        model.addAttribute("employee", new Employee());
        Pageable pageable = PageRequest.of(page, size);
        List<Employee> employees = employeeRepository.findAll(pageable).getContent();

        model.addAttribute("employees", employees);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", employeeRepository.findAll(pageable).getTotalPages());

        return "admin/employee/employee";
    }
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        // Lấy thông tin người dùng hiện tại
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUsername = currentUser.getUsername();

        // phương thức tìm Employee theo username
        Employee currentEmployee = employeeRepository.findByUsername(currentUsername);

        // Kiểm tra nếu ID cần xóa là của người dùng hiện tại
        if (currentEmployee.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa tài khoản của chính mình.");
            return "redirect:/admin/employee";  // Điều hướng về trang danh sách hoặc trang khác phù hợp
        }

        // Kiểm tra nếu tài khoản cần xóa là tài khoản admin
        Employee employeeToDelete = employeeRepository.findById(id).orElse(null);
        if (employeeToDelete != null && employeeToDelete.getRole()==1) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa tài khoản admin.");
            return "redirect:/admin/employee";  // Điều hướng về trang danh sách hoặc trang khác phù hợp
        }

        // Kiểm tra nếu tài khoản có liên kết với dữ liệu khác (ví dụ: hóa đơn)
        try {
            employeeRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa tài khoản này vì có ràng buộc dữ liệu khác.");
            return "redirect:/admin/employee";
        }

        // Nếu không có lỗi, chuyển hướng về trang danh sách nhân viên
        redirectAttributes.addFlashAttribute("success", "Xóa tài khoản thành công.");
        return "redirect:/admin/employee";
    }

    @GetMapping("/detail/{id}")
    public String editEmployee(@PathVariable("id") Integer id, Model model) {
        Employee employee = employeeRepository.findeById(id);
        if (employee!=null) {
            model.addAttribute("employee", employee);
            return "/admin/employee/employee_detail";
        }
        return "redirect:/admin/employee";
    }

    @PostMapping("/update")
    public String updateEmployee(@ModelAttribute Employee employee, BindingResult bindingResult, Principal principal) {
        Employee existingEmployee = employeeRepository.findById(employee.getId()).orElse(null);
        // Lấy tên người dùng đang đăng nhập
        String currentUsername = principal.getName();

        if (existingEmployee != null) {
            // Kiểm tra số điện thoại đã tồn tại, nhưng bỏ qua nếu không thay đổi
            if (!existingEmployee.getPhone().equals(employee.getPhone()) && employeeRepository.existsByPhone(employee.getPhone())) {
                bindingResult.rejectValue("phone", "error.employee", "Số điện thoại đã tồn tại");
            }

            // Kiểm tra email đã tồn tại, nhưng bỏ qua nếu không thay đổi
            if (!existingEmployee.getEmail().equals(employee.getEmail()) && employeeRepository.existsByEmail(employee.getEmail())) {
                bindingResult.rejectValue("email", "error.employee", "Email đã tồn tại");
            }
            // Kiểm tra nếu quản trị viên tự thay đổi trạng thái của chính mình
            if (existingEmployee.getUsername().equals(currentUsername) && !existingEmployee.getStatus().equals(employee.getStatus())) {
                bindingResult.rejectValue("status", "error.employee", "Bạn không thể tự thay đổi trạng thái của chính mình.");
            }
            // Kiểm tra nếu quản trị viên tự thay đổi role của chính mình
            if (existingEmployee.getUsername().equals(currentUsername) && !existingEmployee.getRole().equals(employee.getRole())) {
                bindingResult.rejectValue("role", "error.employee", "Bạn không thể tự thay đổi vai trò của chính mình.");
            }
        }

        // Kiểm tra có lỗi không
        if (bindingResult.hasErrors()) {
            return "admin/employee/employee_detail"; // Trả về trang cập nhật nếu có lỗi
        }
        employee.setUpdatedDate(LocalDateTime.now());
        employeeRepository.save(employee);
        return "redirect:/admin/employee";
    }
    @PostMapping("/add")
    public ModelAndView add(@ModelAttribute("employee") Employee employee, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView("admin/employee/employee"); // Trả về trang admin/employee nếu có lỗi

        // Kiểm tra lỗi và thêm thông báo lỗi vào bindingResult
        if (employeeRepository.existsByUsername(employee.getUsername())) {
            bindingResult.rejectValue("username", "error.employee", "Username đã tồn tại");
        }
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            bindingResult.rejectValue("email", "error.employee", "Email đã tồn tại");
        }
        if (employeeRepository.existsByPhone(employee.getPhone())) {
            bindingResult.rejectValue("phone", "error.employee", "Số điện thoại đã tồn tại");
        }

        // Kiểm tra nếu có lỗi, trả về trang với modal mở kèm thông báo lỗi
        if (bindingResult.hasErrors()) {
            modelAndView.addObject("employee", employee);
            modelAndView.addObject("showModal", true); // Thêm flag để JavaScript nhận biết cần hiển thị modal
            return modelAndView;
        }

        // Nếu không có lỗi, lưu employee và chuyển hướng về trang admin/employee
        employee.setCreatedDate(LocalDateTime.now());
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        employeeRepository.save(employee);
        return new ModelAndView("redirect:/admin/employee");
    }

    @GetMapping("/search")
    public String searchEmployees(@RequestParam(value = "searchValue", required = false) String searchValue,
                                  @RequestParam(value = "status", required = false) String status,
                                  @RequestParam(value = "role", required = false) String role,
                                  Model model) {
        List<Employee> employees;
        Byte statusValue = null;
        Byte roleValue = null;
        model.addAttribute("employee", new Employee());
        // Chuyển đổi status nếu không null hoặc rỗng
        if (status != null && !status.isEmpty()) {
            try {
                statusValue = Byte.parseByte(status);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        // Xử lý vai trò
        if (role != null && !role.isEmpty()) {
            roleValue = Byte.valueOf((byte) (role.equals("admin")?1:0));
        }
        // Nếu searchValue không được điền, tìm tất cả nhân viên
        if (searchValue == null || searchValue.isEmpty()) {
            employees = employeeRepository.findAllEmployees(statusValue, roleValue);
        } else {
            // Nếu có searchValue, tìm theo tên, username hoặc SĐT
            employees = employeeRepository.findEmployeesBySearchValue(searchValue, statusValue, roleValue);
        }

        // Gán danh sách nhân viên vào model
        model.addAttribute("employees", employees);
        return "/admin/employee/employee";
    }
    @GetMapping("/export")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response) throws IOException {
        // Đặt tên cho file Excel
        String fileName = "employees.xlsx";

        // Đặt kiểu nội dung cho phản hồi HTTP là Excel
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        // Khởi tạo workbook và sheet trong file Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employees");

        // Tạo dòng đầu tiên cho tiêu đề của bảng
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Username");
        headerRow.createCell(1).setCellValue("Password");
        headerRow.createCell(2).setCellValue("Fullname");
        headerRow.createCell(3).setCellValue("Email");
        headerRow.createCell(4).setCellValue("Phone");
        headerRow.createCell(5).setCellValue("Created Date");
        headerRow.createCell(6).setCellValue("Updated Date");
        headerRow.createCell(7).setCellValue("Role");
        headerRow.createCell(8).setCellValue("Status");

        // Lấy danh sách nhân viên từ cơ sở dữ liệu
        List<Employee> employees = employeeRepository.findAll();

        // Duyệt qua danh sách nhân viên và tạo các dòng cho từng nhân viên
        int rowNum = 1; // Bắt đầu từ dòng thứ hai vì dòng đầu tiên đã là tiêu đề
        for (Employee employee : employees) {
            Row row = sheet.createRow(rowNum++);

            // Điền dữ liệu của từng nhân viên vào các cột tương ứng
            row.createCell(0).setCellValue(employee.getUsername());
            row.createCell(1).setCellValue(employee.getPassword()); // Có thể cần mã hóa hoặc ẩn dữ liệu mật khẩu
            row.createCell(2).setCellValue(employee.getFullname());
            row.createCell(3).setCellValue(employee.getEmail());
            row.createCell(4).setCellValue(employee.getPhone());
            row.createCell(5).setCellValue(employee.getCreatedDate().toString());
            row.createCell(6).setCellValue(employee.getUpdatedDate() != null ? employee.getUpdatedDate().toString() : null);
            row.createCell(7).setCellValue(employee.getRole());
            row.createCell(8).setCellValue(employee.getStatus());
        }

        // Ghi workbook ra output stream của phản hồi HTTP
        workbook.write(response.getOutputStream());

        // Đóng workbook để giải phóng tài nguyên
        workbook.close();
    }
    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) {
        // Kiểm tra nếu file trống thì không thực hiện gì cả
        if (file.isEmpty()) {
            // Bạn có thể thêm xử lý để báo lỗi cho người dùng ở đây
            return "redirect:/admin/employee?error=file_empty";
        }

        List<Employee> list = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            // Lấy sheet đầu tiên trong workbook
            Sheet sheet = workbook.getSheetAt(0);

            // Duyệt qua từng hàng, bắt đầu từ hàng thứ 2 (index 1) để bỏ qua tiêu đề
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    // Đọc từng ô dữ liệu trong hàng và gán vào các thuộc tính của Employee
                    String username = row.getCell(0).getStringCellValue();
                    String password = row.getCell(1).getStringCellValue();
                    String fullname = row.getCell(2).getStringCellValue();
                    String email = row.getCell(3).getStringCellValue();
                    String phone = row.getCell(4).getStringCellValue();
                    Byte role = (byte) ((int) row.getCell(5).getNumericCellValue());
                    Byte status = (byte) ((int) row.getCell(6).getNumericCellValue());
                    String photo = row.getCell(7).getStringCellValue();

                    // Tạo đối tượng Employee mới và gán giá trị
                    Employee employee = new Employee(username, password, fullname, email, phone, photo, role, status);
                    employee.setCreatedDate(LocalDateTime.now());

                    // Mã hóa mật khẩu trước khi lưu
                    employee.setPassword(passwordEncoder.encode(employee.getPassword()));

                    // Thêm employee vào danh sách để lưu
                    list.add(employee);
                }
            }

            // Lưu tất cả các nhân viên đã tạo vào cơ sở dữ liệu
            employeeRepository.saveAll(list);

        } catch (IOException e) {
            e.printStackTrace();
            // Xử lý ngoại lệ có thể trả về thông báo lỗi hoặc log chi tiết hơn
            return "redirect:/admin/employee?error=import_failed";
        }

        // Chuyển hướng về trang danh sách nhân viên sau khi hoàn thành import
        return "redirect:/admin/employee";
    }
}