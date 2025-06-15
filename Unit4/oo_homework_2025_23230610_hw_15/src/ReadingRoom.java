import com.oocourse.library3.LibraryBookId;

import java.util.HashMap;

public class ReadingRoom {
    private final HashMap<LibraryBookId, Integer> readingBooks;

    public ReadingRoom() {
        readingBooks = new HashMap<>();
    }

    public HashMap<LibraryBookId, Integer> getReadingBooks() {
        return readingBooks;
    }

    public void addReadingBook(LibraryBookId bookId) {
        readingBooks.put(bookId, 1);
    }

    public void removeReadingBook(LibraryBookId bookId) {
        readingBooks.remove(bookId);
    }
}
