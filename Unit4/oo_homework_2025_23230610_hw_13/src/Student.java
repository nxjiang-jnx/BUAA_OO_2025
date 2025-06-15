import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;

import java.util.HashSet;

public class Student {
    private String studentId;
    private LibraryBookId borrowBookB;
    private final HashSet<LibraryBookId> borrowBookC;

    public Student(String studentId) {
        this.studentId = studentId;
        borrowBookB = null;
        borrowBookC = new HashSet<>();
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

    public void borrowBook(LibraryBookId bookId) {
        if (bookId.isTypeB()) {
            borrowBookB = bookId;
        } else {
            borrowBookC.add(bookId);
        }
    }

    public void returnBook(LibraryBookId bookId) {
        if (bookId.isTypeB()) {
            borrowBookB = null;
        } else {
            borrowBookC.remove(bookId);
        }
    }

    public boolean canBorrowOrPickBook(LibraryBookIsbn bookIsbn) {
        if (bookIsbn.isTypeA()) {
            return false;
        }
        return bookIsbn.isTypeB() ? !hasTypeB() : !hasOneTypeC(bookIsbn);
    }
}
