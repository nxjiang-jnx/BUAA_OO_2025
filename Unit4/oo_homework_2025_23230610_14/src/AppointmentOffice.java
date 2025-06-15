import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;

import java.util.HashMap;
import java.util.HashSet;

public class AppointmentOffice {
    private final HashMap<String, HashSet<LibraryBookIsbn>> appointments;
    private final HashMap<String, HashSet<Book>> reservedBooks;
    private final HashMap<LibraryBookId, Integer> overdueBooks;

    public AppointmentOffice() {
        appointments = new HashMap<>();
        reservedBooks = new HashMap<>();
        overdueBooks = new HashMap<>();
    }

    public void addAppointment(String studentId, LibraryBookIsbn bookIsbn) {
        if (appointments.containsKey(studentId)) {
            appointments.get(studentId).add(bookIsbn);
        } else {
            HashSet<LibraryBookIsbn> set = new HashSet<>();
            set.add(bookIsbn);
            appointments.put(studentId, set);
        }
    }

    public void addOverdueBook(LibraryBookId bookId) {
        if (overdueBooks.containsKey(bookId)) {
            overdueBooks.put(bookId, overdueBooks.get(bookId) + 1);
        } else {
            overdueBooks.put(bookId, 1);
        }
    }

    public HashMap<String, HashSet<LibraryBookIsbn>> getAppointments() {
        return appointments;
    }

    public HashMap<String, HashSet<Book>> getReservedBooks() {
        return reservedBooks;
    }

    public HashMap<LibraryBookId, Integer> getOverdueBooks() {
        return overdueBooks;
    }

    public void addReservedBook(String studentId, Book book) {
        if (reservedBooks.containsKey(studentId)) {
            reservedBooks.get(studentId).add(book);
        } else {
            HashSet<Book> set = new HashSet<>();
            set.add(book);
            reservedBooks.put(studentId, set);
        }
    }
}
