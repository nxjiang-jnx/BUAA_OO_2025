import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryReqCmd;

import static com.oocourse.library1.LibraryIO.PRINTER;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class Library {
    private final BookShelf bookShelf;
    private final BorrowReturnOffice borrowReturnOffice;
    private final AppointmentOffice appointmentOffice;
    private final HashMap<String, Student> students;
    private final HashMap<LibraryBookId, ArrayList<String>> moveTrace;

    public Library(Map<LibraryBookIsbn, Integer> books) {
        bookShelf = new BookShelf(books);
        borrowReturnOffice = new BorrowReturnOffice();
        appointmentOffice = new AppointmentOffice();
        students = new HashMap<>();
        moveTrace = new HashMap<>();
    }

    public void sortBooks(LocalDate today) {
        ArrayList<LibraryMoveInfo> moveInfos = new ArrayList<>();
        bro2bs(moveInfos, today);
        // 更新预约处保留的图书时间
        appointmentOffice.getReservedBooks().forEach((key, books) ->
            books.removeIf(book -> {
                boolean isOverdue = ChronoUnit.DAYS.between(book.getAppointmentDate(), today) >= 5;
                if (isOverdue) {
                    appointmentOffice.addOverdueBook(book.getBookId());
                }
                return isOverdue;
            })
        );
        ao2bs(moveInfos, today);
        bs2ao(moveInfos, today);
        PRINTER.move(today, moveInfos);
    }

    public void queryBook(LibraryReqCmd req) {
        LibraryBookId bookId = req.getBookId();
        LocalDate today = req.getDate();

        ArrayList<String> trace = moveTrace.getOrDefault(bookId, new ArrayList<>());
        System.out.println("[" + today + "] " + bookId + " moving trace: " + trace.size());
        int idx = 1;
        for (String log : trace) {
            System.out.println(idx + " " + log);
            idx++;
        }
    }

    public void borrowBook(LibraryReqCmd req) {
        LibraryBookIsbn bookIsbn = req.getBookIsbn();
        String studentId = req.getStudentId();
        Student student = getStudent(studentId);

        if (bookShelf.queryBook(bookIsbn) == 0 || bookIsbn.isTypeA()) {
            PRINTER.reject(req);
            return;
        }
        if (student.canBorrowOrPickBook(bookIsbn)) {
            LibraryBookId bookId = bookShelf.removeBook(bookIsbn);
            student.borrowBook(bookId);
            recordTrace(bookId, req.getDate(), "bs", "user");
            PRINTER.accept(req, bookId);
        } else {
            PRINTER.reject(req);
        }
    }

    public void appointmentBook(LibraryReqCmd req) {
        LibraryBookIsbn bookIsbn = req.getBookIsbn();
        String studentId = req.getStudentId();
        Student student = getStudent(studentId);

        if (!student.canBorrowOrPickBook(bookIsbn)) {
            PRINTER.reject(req);
            return;
        }
        if (appointmentOffice.getReservedBooks().containsKey(studentId) &&
            !appointmentOffice.getReservedBooks().get(studentId).isEmpty())  {
            PRINTER.reject(req);
            return;
        }
        if (appointmentOffice.getAppointments().containsKey(studentId) &&
            !appointmentOffice.getAppointments().get(studentId).isEmpty()) {
            PRINTER.reject(req);
            return;
        }

        appointmentOffice.addAppointment(studentId, bookIsbn);
        PRINTER.accept(req);
    }

    public void returnBook(LibraryReqCmd req) {
        LibraryBookId bookId = req.getBookId();
        String studentId = req.getStudentId();
        Student student = getStudent(studentId);

        student.returnBook(bookId);
        borrowReturnOffice.addBorrowReturnBook(bookId);
        recordTrace(bookId, req.getDate(), "user", "bro");
        PRINTER.accept(req);
    }

    public void pickBook(LibraryReqCmd req) {
        LibraryBookIsbn bookIsbn = req.getBookIsbn();
        String studentId = req.getStudentId();
        Student student = getStudent(studentId);

        if (!appointmentOffice.getReservedBooks().containsKey(studentId)) {
            PRINTER.reject(req);
            return;
        }
        if (!student.canBorrowOrPickBook(bookIsbn)) {
            PRINTER.reject(req);
            return;
        }

        for (Book book : appointmentOffice.getReservedBooks().get(studentId)) {
            if (book.getBookId().getBookIsbn().equals(bookIsbn)) {
                appointmentOffice.getReservedBooks().get(studentId).remove(book);
                if (appointmentOffice.getReservedBooks().get(studentId).isEmpty()) {
                    appointmentOffice.getReservedBooks().remove(studentId);
                }
                student.borrowBook(book.getBookId());
                recordTrace(book.getBookId(), req.getDate(), "ao", "user");
                PRINTER.accept(req, book.getBookId());
                return;
            }
        }
        PRINTER.reject(req);
    }

    // 以下是辅助方法
    private void bro2bs(ArrayList<LibraryMoveInfo> moveInfos, LocalDate today) {
        // 从借还处到书架
        for (Map.Entry<LibraryBookId, Integer> entry :
            borrowReturnOffice.getBorrowReturnBooks().entrySet()) {
            LibraryBookId bookId = entry.getKey();
            int count = entry.getValue();
            for (int i = 0; i < count; i++) {
                moveInfos.add(new LibraryMoveInfo(bookId, "bro", "bs"));
                bookShelf.addBook(bookId);
                recordTrace(bookId, today, "bro", "bs");
            }
        }
        borrowReturnOffice.getBorrowReturnBooks().clear();
    }

    private void ao2bs(ArrayList<LibraryMoveInfo> moveInfos, LocalDate today) {
        // 从预约处到书架
        for (Map.Entry<LibraryBookId, Integer> entry :
            appointmentOffice.getOverdueBooks().entrySet()) {
            LibraryBookId bookId = entry.getKey();
            int count = entry.getValue();
            for (int i = 0; i < count; i++) {
                moveInfos.add(new LibraryMoveInfo(bookId, "ao", "bs"));
                bookShelf.addBook(bookId);
                recordTrace(bookId, today, "ao", "bs");
            }
        }
        appointmentOffice.getOverdueBooks().clear();
    }

    private void bs2ao(ArrayList<LibraryMoveInfo> moveInfos, LocalDate today) {
        // 从书架到预约处
        appointmentOffice.getAppointments().forEach((studentId, bookIsbns) -> bookIsbns.removeIf(
            bookIsbn -> {
                if (bookShelf.queryBook(bookIsbn) > 0) {
                    LibraryBookId bookId = bookShelf.removeBook(bookIsbn);
                    Book book = new Book(bookId, today);
                    appointmentOffice.addReservedBook(studentId, book);
                    moveInfos.add(new LibraryMoveInfo(bookId, "bs", "ao", studentId));
                    recordTrace(bookId, today, "bs", "ao");
                    return true;
                }
                return false;
            }));
    }

    private Student getStudent(String studentId) {
        if (!students.containsKey(studentId)) {
            students.put(studentId, new Student(studentId));
        }
        return students.get(studentId);
    }

    private void recordTrace(LibraryBookId bookId, LocalDate date, String from, String to) {
        String log = "[" + date.toString() + "] from " + from + " to " + to;
        moveTrace.computeIfAbsent(bookId, k -> new ArrayList<>()).add(log);
    }
}
