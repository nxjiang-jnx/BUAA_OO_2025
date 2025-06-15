import com.oocourse.library2.LibraryBookId;

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
        borrowReturnBooks.put(bookId, 1);
    }
}
