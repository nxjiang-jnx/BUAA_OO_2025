import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryCommand;
import com.oocourse.library1.LibraryOpenCmd;
import com.oocourse.library1.LibraryReqCmd;
import com.oocourse.library1.LibraryCloseCmd;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import static com.oocourse.library1.LibraryIO.PRINTER;
import static com.oocourse.library1.LibraryIO.SCANNER;

public class MainClass {
    public static void main(String[] args) {
        Map<LibraryBookIsbn, Integer> bookList = SCANNER.getInventory();
        Library library = new Library(bookList);
        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) {
                break;
            }
            LocalDate today = command.getDate();
            if (command instanceof LibraryOpenCmd) {
                library.sortBooks(today);
            } else if (command instanceof LibraryCloseCmd) {
                PRINTER.move(today,new ArrayList<>());
            } else {
                LibraryReqCmd req = (LibraryReqCmd) command;
                LibraryReqCmd.Type type = req.getType();

                switch (type) {
                    case QUERIED:
                        library.queryBook(req);
                        break;
                    case BORROWED:
                        library.borrowBook(req);
                        break;
                    case ORDERED:
                        library.appointmentBook(req);
                        break;
                    case RETURNED:
                        library.returnBook(req);
                        break;
                    case PICKED:
                        library.pickBook(req);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
