import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;

public class Student {
    private String studentId;
    private LibraryBookId borrowBookB;
    private final HashSet<LibraryBookId> borrowBookC;
    private LibraryBookId readingBookToday;
    private int creditScore;
    private final HashMap<LibraryBookId, LocalDate> borrowDateMap;

    public Student(String studentId) {
        this.studentId = studentId;
        borrowBookB = null;
        borrowBookC = new HashSet<>();
        readingBookToday = null;
        creditScore = 100;
        borrowDateMap = new HashMap<>();
    }

    public boolean hasTypeB() {
        return borrowBookB != null;
    }

    public boolean hasOneTypeC(LibraryBookIsbn isbn) {
        for (LibraryBookIsbn bookId : borrowBookC) {
            if (bookId.getBookIsbn().equals(isbn)) {
                return true;
            }
        }
        return false;
    }

    public void borrowBook(LibraryBookId bookId, LocalDate today) {
        if (bookId.isTypeB()) {
            borrowBookB = bookId;
        } else {
            borrowBookC.add(bookId);
        }
        borrowDateMap.put(bookId, today);
    }

    public void returnBook(LibraryBookId bookId) {
        if (bookId.isTypeB()) {
            borrowBookB = null;
        } else {
            borrowBookC.remove(bookId);
        }
        borrowDateMap.remove(bookId);
    }

    public boolean canBorrowOrPickBook(LibraryBookIsbn bookIsbn) {
        if (bookIsbn.isTypeA()) {
            return false;
        }
        return bookIsbn.isTypeB() ? !hasTypeB() : !hasOneTypeC(bookIsbn);
    }

    public boolean canReadToday() {
        return readingBookToday == null;
    }

    public void addReadBook(LibraryBookId bookId) {
        readingBookToday = bookId;
    }

    public void restoreReadBook() {
        readingBookToday = null;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void addCreditScore(int score) {
        creditScore = Math.min(creditScore + score, 180);
    }

    public void reduceCreditScore(int score) {
        creditScore = Math.max(creditScore - score, 0);
    }

    public LocalDate getBorrowDate(LibraryBookId bookId) {
        return borrowDateMap.get(bookId);
    }

    public void dailyCheckCredit(LocalDate today, LocalDate lastSortDate) {
        if (borrowBookB != null) {
            LocalDate borrowDate = borrowDateMap.get(borrowBookB);
            LocalDate overdueDate = borrowDate.plusDays(30);
            long days = ChronoUnit.DAYS.between(borrowDate, today);
            if (days > 30) {
                LocalDate laterDate = lastSortDate.isAfter(overdueDate) ?
                    lastSortDate : overdueDate;
                reduceCreditScore((int) (5 * (ChronoUnit.DAYS.between(laterDate, today))));
            }
        }

        for (LibraryBookId bookC : borrowBookC) {
            LocalDate borrowDate = borrowDateMap.get(bookC);
            LocalDate overdueDate = borrowDate.plusDays(60);
            long days = ChronoUnit.DAYS.between(borrowDate, today);
            if (days > 60) {
                LocalDate laterDate = lastSortDate.isAfter(overdueDate) ?
                    lastSortDate : overdueDate;
                reduceCreditScore((int) (5 * (ChronoUnit.DAYS.between(laterDate, today))));
            }
        }
    }
}
