import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryMoveInfo;
import com.oocourse.library3.LibraryReqCmd;
import com.oocourse.library3.LibraryQcsCmd;
import com.oocourse.library3.LibraryCommand;
import com.oocourse.library3.annotation.Trigger;

import static com.oocourse.library3.LibraryIO.PRINTER;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;

public class Library {
    private final BookShelf bookShelf;
    private final BookShelf hotBookShelf;
    private final ReadingRoom readingRoom;
    private final BorrowReturnOffice borrowReturnOffice;
    private final AppointmentOffice appointmentOffice;
    private final HashMap<String, Student> students;
    private final HashMap<LibraryBookId, ArrayList<String>> moveTrace;
    private final HashSet<LibraryBookIsbn> hotIsbns;
    private LocalDate lastSortDate;

    public Library(Map<LibraryBookIsbn, Integer> books) {
        bookShelf = new BookShelf(books);
        hotBookShelf = new BookShelf(new HashMap<>());
        readingRoom = new ReadingRoom();
        borrowReturnOffice = new BorrowReturnOffice();
        appointmentOffice = new AppointmentOffice();
        students = new HashMap<>();
        moveTrace = new HashMap<>();
        hotIsbns = new HashSet<>();
        lastSortDate = LocalDate.now();
    }

    @Trigger(from = "InitState", to = "BOOKSHELF")
    public void sortBooks(LocalDate today) {
        ArrayList<LibraryMoveInfo> moveInfos = new ArrayList<>();
        bro2bs(moveInfos, today);
        // 更新预约处保留的图书时间
        appointmentOffice.getReservedBooks().forEach((key, books) ->
            books.removeIf(book -> {
                boolean isOverdue = ChronoUnit.DAYS.between(book.getAppointmentDate(), today) >= 5;
                if (isOverdue) {
                    appointmentOffice.addOverdueBook(book.getBookId());
                    getStudent(key).reduceCreditScore(15);
                }
                return isOverdue;
            })
        );
        ao2bs(moveInfos, today);
        rr2bs(moveInfos, today);
        hbs2bs(moveInfos, today);
        bs2ao(moveInfos, today);
        bs2hbs(moveInfos, today);
        // 清除所有学生的 readingBookToday
        students.forEach((key, student) -> {
            if (!student.canReadToday()) {
                student.reduceCreditScore(10);
            }
            student.restoreReadBook();
            student.dailyCheckCredit(today, lastSortDate);
        });
        PRINTER.move(today, moveInfos);
        lastSortDate = today;
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

    @Trigger(from = "BOOKSHELF", to = "USER")
    @Trigger(from = "HOT_BOOKSHELF", to = "USER")
    public void borrowBook(LibraryReqCmd req) {
        LibraryBookIsbn bookIsbn = req.getBookIsbn();
        String studentId = req.getStudentId();
        Student student = getStudent(studentId);
        LibraryBookId bookId;

        if (!student.canBorrowOrPickBook(bookIsbn)) {
            PRINTER.reject(req);
            return;
        }

        if (student.getCreditScore() < 60) {
            PRINTER.reject(req);
            return;
        }

        if (bookShelf.queryBook(bookIsbn) == 0) {
            if (hotBookShelf.queryBook(bookIsbn) == 0) {
                PRINTER.reject(req);
                return;
            }
            bookId = hotBookShelf.removeBook(bookIsbn);
            recordTrace(bookId, req.getDate(), "hbs", "user");
        } else {
            bookId = bookShelf.removeBook(bookIsbn);
            recordTrace(bookId, req.getDate(), "bs", "user");
        }
        student.borrowBook(bookId, req.getDate());
        hotIsbns.add(bookIsbn);
        PRINTER.accept(req, bookId);
    }

    public void orderNewBook(LibraryReqCmd req) {
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
        if (student.getCreditScore() < 100) {
            PRINTER.reject(req);
            return;
        }

        appointmentOffice.addAppointment(studentId, bookIsbn);
        PRINTER.accept(req);
    }

    @Trigger(from = "USER", to = "BORROW_RETURN_OFFICE")
    public void returnBook(LibraryReqCmd req) {
        LibraryBookId bookId = req.getBookId();
        String studentId = req.getStudentId();
        Student student = getStudent(studentId);

        LocalDate borrowDate = student.getBorrowDate(bookId);
        long borrowDays = ChronoUnit.DAYS.between(borrowDate, req.getDate());
        boolean notOverdue = (bookId.isTypeB() && borrowDays <= 30)
            || (bookId.isTypeC() && borrowDays <= 60);

        if (notOverdue) {
            student.addCreditScore(10);
            PRINTER.accept(req, "not overdue");
        } else {
            // 不再重复扣分
            PRINTER.accept(req, "overdue");
        }

        student.returnBook(bookId);
        borrowReturnOffice.addBorrowReturnBook(bookId);
        recordTrace(bookId, req.getDate(), "user", "bro");
    }

    @Trigger(from = "APPOINTMENT_OFFICE", to = "USER")
    public void getOrderedBook(LibraryReqCmd req) {
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
                student.borrowBook(book.getBookId(), req.getDate());
                recordTrace(book.getBookId(), req.getDate(), "ao", "user");
                PRINTER.accept(req, book.getBookId());
                return;
            }
        }
        PRINTER.reject(req);
    }

    @Trigger(from = "BOOKSHELF", to = "READING_ROOM")
    @Trigger(from = "HOT_BOOKSHELF", to = "READING_ROOM")
    public void readBook(LibraryReqCmd req) {
        LibraryBookIsbn isbn = req.getBookIsbn();
        String studentId = req.getStudentId();
        Student student = getStudent(studentId);

        if (!student.canReadToday()) {
            PRINTER.reject(req);
            return;
        }

        if (isbn.isTypeA() && student.getCreditScore() < 40) {
            PRINTER.reject(req);
            return;
        }

        if ((isbn.isTypeB() || isbn.isTypeC()) && student.getCreditScore() <= 0) {
            PRINTER.reject(req);
            return;
        }

        // 先尝试从普通书架取书，再尝试从热门书架取书
        LibraryBookId bookId = bookShelf.removeBook(isbn);
        if (bookId == null) {
            bookId = hotBookShelf.removeBook(isbn);
            if (bookId == null) {
                PRINTER.reject(req);
                return;
            }
            recordTrace(bookId, req.getDate(), "hbs", "rr");
        } else {
            recordTrace(bookId, req.getDate(), "bs", "rr");
        }

        student.addReadBook(bookId);
        readingRoom.addReadingBook(bookId);
        hotIsbns.add(isbn);
        PRINTER.accept(req, bookId);
    }

    @Trigger(from = "READING_ROOM", to = "BORROW_RETURN_OFFICE")
    public void restoreBook(LibraryReqCmd req) {
        String studentId = req.getStudentId();
        Student student = getStudent(studentId);
        LibraryBookId bookId = req.getBookId();

        student.restoreReadBook();
        student.addCreditScore(10);
        readingRoom.removeReadingBook(bookId);
        borrowReturnOffice.addBorrowReturnBook(bookId);
        recordTrace(bookId, req.getDate(), "rr", "bro");
        PRINTER.accept(req);
    }

    public void queryCreditScore(LibraryCommand command) {
        LibraryQcsCmd req = (LibraryQcsCmd) command;
        PRINTER.info(req, getStudent(req.getStudentId()).getCreditScore());
    }

    // 以下是辅助方法
    @Trigger(from = "BORROW_RETURN_OFFICE", to = "BOOKSHELF")
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

    @Trigger(from = "APPOINTMENT_OFFICE", to = "BOOKSHELF")
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

    @Trigger(from = "BOOKSHELF", to = "APPOINTMENT_OFFICE")
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

    @Trigger(from = "READING_ROOM", to = "BOOKSHELF")
    private void rr2bs(ArrayList<LibraryMoveInfo> moveInfos, LocalDate today) {
        // 从阅览室到书架
        for (Map.Entry<LibraryBookId, Integer> entry :
            readingRoom.getReadingBooks().entrySet()) {
            LibraryBookId bookId = entry.getKey();
            int count = entry.getValue();
            for (int i = 0; i < count; i++) {
                moveInfos.add(new LibraryMoveInfo(bookId, "rr", "bs"));
                bookShelf.addBook(bookId);
                recordTrace(bookId, today, "rr", "bs");
            }
        }
        readingRoom.getReadingBooks().clear();
    }

    @Trigger(from = "HOT_BOOK_SHELF", to = "BOOKSHELF")
    private void hbs2bs(ArrayList<LibraryMoveInfo> moveInfos, LocalDate today) {
        // 从热门书架到书架
        for (Map.Entry<LibraryBookIsbn, Integer> entry :
            hotBookShelf.getBooks().entrySet()) {
            LibraryBookIsbn isbn = entry.getKey();
            int count = entry.getValue();
            for (int i = 0; i < count; i++) {
                LibraryBookId bookId = hotBookShelf.removeBook(isbn);
                moveInfos.add(new LibraryMoveInfo(bookId, "hbs", "bs"));
                bookShelf.addBook(bookId);
                recordTrace(bookId, today, "hbs", "bs");
            }
        }
        hotBookShelf.clearBooks();
    }

    @Trigger(from = "BOOKSHELF", to = "HOT_BOOK_SHELF")
    private void bs2hbs(ArrayList<LibraryMoveInfo> moveInfos, LocalDate today) {
        // 从书架到热门书架
        for (LibraryBookIsbn isbn : hotIsbns) {
            int count = bookShelf.queryBook(isbn);
            for (int i = 0; i < count; i++) {
                LibraryBookId bookId = bookShelf.removeBook(isbn);
                moveInfos.add(new LibraryMoveInfo(bookId, "bs", "hbs"));
                hotBookShelf.addBook(bookId);
                recordTrace(bookId, today, "bs", "hbs");
            }
        }
        hotIsbns.clear();
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
