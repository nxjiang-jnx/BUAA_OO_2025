import com.oocourse.library2.LibraryBookId;

import java.time.LocalDate;

public class Book {
    private final LibraryBookId bookId;
    private final LocalDate appointmentDate;

    public Book(LibraryBookId bookId, LocalDate appointmentDate) {
        this.bookId = bookId;
        this.appointmentDate = appointmentDate;
    }

    public LibraryBookId getBookId() {
        return bookId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }
}
