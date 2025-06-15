import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryCommand;
import com.oocourse.library2.LibraryOpenCmd;
import com.oocourse.library2.LibraryReqCmd;
import com.oocourse.library2.LibraryCloseCmd;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import static com.oocourse.library2.LibraryIO.PRINTER;
import static com.oocourse.library2.LibraryIO.SCANNER;

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
                    case READ:
                        library.readBook(req);
                        break;
                    case RESTORED:
                        library.restoreBook(req);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
