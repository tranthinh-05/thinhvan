import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;

// Lớp Sinh viên
class Student {
    private String id;
    private String name;
    private boolean isPresent;

    public Student(String id, String name) {
        this.id = id;
        this.name = name;
        this.isPresent = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void markAttendance(boolean present) {
        this.isPresent = present;
    }

    @Override
    public String toString() {
        return id + "," + name + "," + (isPresent ? "Có mặt" : "Vắng mặt");
    }

    public static Student fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length == 3) {
            Student student = new Student(parts[0], parts[1]);
            student.markAttendance(parts[2].equalsIgnoreCase("Có mặt"));
            return student;
        }
        return null;
    }
}

// Lớp Giáo viên
class Teacher {
    private String username;
    private String hashedPassword;

    public Teacher(String username, String password) {
        this.username = username;
        this.hashedPassword = hashPassword(password);
    }

    public String getUsername() {
        return username;
    }

    public boolean authenticate(String inputPassword) {
        return this.hashedPassword.equals(hashPassword(inputPassword));
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Không thể băm mật khẩu.", e);
        }
    }
}

// Lớp Hệ thống điểm danh//
class AttendanceSystem {
    private ArrayList<Student> students = new ArrayList<>();
    private ArrayList<Teacher> teachers = new ArrayList<>();
    private Scanner scanner = new Scanner(System.in);
    private final String STUDENT_FILE = "students.csv";

    // Thêm sinh viên vào lớp
    public void addStudent() {
        System.out.print("Nhập ID sinh viên: ");
        String id = scanner.nextLine();

        // Kiểm tra ID trùng
        for (Student student : students) {
            if (student.getId().equals(id)) {
                System.out.println("ID sinh viên đã tồn tại! Vui lòng nhập lại.");
                return;
            }
        }

        System.out.print("Nhập tên sinh viên: ");
        String name = scanner.nextLine();
        students.add(new Student(id, name));
        System.out.println("Thêm sinh viên thành công!");
        saveToFile();
    }

    // Điểm danh (yêu cầu giáo viên đăng nhập trước)
    public void markAttendance() {
        Teacher teacher = authenticateTeacher();
        if (teacher == null) {
            System.out.println("Xác thực thất bại. Không thể thực hiện điểm danh.");
            return;
        }

        System.out.println("Chào giáo viên " + teacher.getUsername() + ", hãy thực hiện điểm danh.");
        int presentCount = 0;

        for (Student student : students) {
            System.out.print("Sinh viên " + student.getName() + " (có mặt? y/n): ");
            String input = scanner.nextLine().toLowerCase();
            student.markAttendance(input.equals("y"));
            if (input.equals("y")) {
                presentCount++;
            }
        }

        System.out.println("Điểm danh hoàn tất. Có " + presentCount + " sinh viên có mặt và " + (students.size() - presentCount) + " sinh viên vắng mặt.");
    }

    // Hiển thị danh sách sinh viên và trạng thái điểm danh
    public void displayAttendance() {
        System.out.println("\n===== Danh sách điểm danh =====");
        if (students.isEmpty()) {
            System.out.println("Chưa có sinh viên nào trong danh sách.");
            return;
        }

        for (Student student : students) {
            System.out.println("ID: " + student.getId() + ", Tên: " + student.getName() + ", Trạng thái: " + (student.isPresent() ? "Có mặt" : "Vắng mặt"));
        }
    }

    // Lưu danh sách ra file CSV
    public void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STUDENT_FILE))) {
            for (Student student : students) {
                writer.write(student.toString());
                writer.newLine();
            }
            System.out.println("Dữ liệu đã được lưu thành công!");
        } catch (IOException e) {
            System.out.println("Lỗi khi lưu file: " + e.getMessage());
        }
    }

    // Tải danh sách từ file CSV
    public void loadFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            students.clear();
            while ((line = reader.readLine()) != null) {
                Student student = Student.fromCSV(line);
                if (student != null) {
                    students.add(student);
                }
            }
            System.out.println("Tải danh sách từ file thành công!");
        } catch (FileNotFoundException e) {
            System.out.println("Không tìm thấy file dữ liệu. Danh sách trống.");
        } catch (IOException e) {
            System.out.println("Lỗi khi đọc file: " + e.getMessage());
        }
    }

    // Xác thực giáo viên
    private Teacher authenticateTeacher() {
        System.out.print("Nhập tên đăng nhập: ");
        String username = scanner.nextLine();
        System.out.print("Nhập mật khẩu: ");
        String password = scanner.nextLine();

        for (Teacher teacher : teachers) {
            if (teacher.getUsername().equals(username) && teacher.authenticate(password)) {
                return teacher;
            }
        }
        return null;
    }

    // Thêm giáo viên vào hệ thống
    public void addTeacher() {
        System.out.print("Nhập tên đăng nhập giáo viên: ");
        String username = scanner.nextLine();
        System.out.print("Nhập mật khẩu: ");
        String password = scanner.nextLine();
        teachers.add(new Teacher(username, password));
        System.out.println("Thêm giáo viên thành công!");
    }

    // Menu chính
    public void run() {
        loadFromFile();
        teachers.add(new Teacher("admin", "12345")); // Giáo viên mặc định

        while (true) {
            System.out.println("\n===== Hệ thống điểm danh =====");
            System.out.println("1. Thêm sinh viên");
            System.out.println("2. Điểm danh");
            System.out.println("3. Hiển thị danh sách điểm danh");
            System.out.println("4. Lưu danh sách ra file");
            System.out.println("5. Thêm giáo viên");
            System.out.println("6. Thoát");
            System.out.print("Chọn: ");
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        addStudent();
                        break;
                    case 2:
                        markAttendance();
                        break;
                    case 3:
                        displayAttendance();
                        break;
                    case 4:
                        saveToFile();
                        break;
                    case 5:
                        addTeacher();
                        break;
                    case 6:
                        saveToFile();
                        System.out.println("Đã lưu và thoát chương trình.");
                        return;
                    default:
                        System.out.println("Lựa chọn không hợp lệ!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Vui lòng nhập số hợp lệ!");
            }
        }
    }

    public static void main(String[] args) {
        AttendanceSystem system = new AttendanceSystem();
        system.run();
    }
}