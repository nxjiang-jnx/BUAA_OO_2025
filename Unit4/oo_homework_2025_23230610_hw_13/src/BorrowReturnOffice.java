import com.oocourse.library1.LibraryBookId;

import java.util.HashMap;

public class BorrowReturnOffice {
    private final HashMap<LibraryBookId, Integer> borrowReturnBooks;

    public BorrowReturnOffice() {
        borrowReturnBooks = new HashMap<>();
    }

    public HashMap<LibraryBookId, Integer> getBorrowReturnBooks() {
        return borrowReturnBooks;
    }

    public void addBorrowReturnBook(LibraryBookId bookId) {
        if (borrowReturnBooks.containsKey(bookId)) {
            borrowReturnBooks.put(bookId, borrowReturnBooks.get(bookId) + 1);
        } else {
            borrowReturnBooks.put(bookId, 1);
        }
    }
}
