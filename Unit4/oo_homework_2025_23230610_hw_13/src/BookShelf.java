import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BookShelf {
    private final Map<LibraryBookIsbn, Integer> books;
    private final Map<LibraryBookIsbn, HashSet<Integer>> availableCopies;

    public BookShelf(Map<LibraryBookIsbn, Integer> books) {
        this.books = books;
        this.availableCopies = new HashMap<>();
        for (Map.Entry<LibraryBookIsbn, Integer> entry : books.entrySet()) {
            LibraryBookIsbn isbn = entry.getKey();
            int count = entry.getValue();
            HashSet<Integer> set = new HashSet<>();
            for (int i = 1; i <= count; i++) {
                set.add(i);
            }
            availableCopies.put(isbn, set);
        }
    }

    public void addBook(LibraryBookId bookId) {
        LibraryBookIsbn isbn = bookId.getBookIsbn();
        int copy = Integer.parseInt(bookId.getCopyId());
        availableCopies.get(isbn).add(copy);
        books.put(isbn, books.get(isbn) + 1);
    }

    public int queryBook(LibraryBookIsbn isbn) {
        return books.get(isbn);
    }

    public LibraryBookId removeBook(LibraryBookIsbn isbn) {
        HashSet<Integer> copies = availableCopies.get(isbn);
        if (copies == null || copies.isEmpty()) {
            return null;
        }
        // 简单选择最小编号
        int minCopy = Integer.MAX_VALUE;
        for (int c : copies) {
            if (c < minCopy) {
                minCopy = c;
            }
        }
        copies.remove(minCopy);
        books.put(isbn, books.get(isbn) - 1);
        return new LibraryBookId(isbn.getType(), isbn.getUid(), String.format("%02d", minCopy));
    }
}
