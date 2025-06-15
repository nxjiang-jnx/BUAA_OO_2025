import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryCommand;
import com.oocourse.library3.LibraryOpenCmd;
import com.oocourse.library3.LibraryReqCmd;
import com.oocourse.library3.LibraryCloseCmd;
import com.oocourse.library3.LibraryQcsCmd;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import static com.oocourse.library3.LibraryIO.PRINTER;
import static com.oocourse.library3.LibraryIO.SCANNER;

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
            } else if (command instanceof LibraryQcsCmd) {
                library.queryCreditScore(command);
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
                        library.orderNewBook(req);
                        break;
                    case RETURNED:
                        library.returnBook(req);
                        break;
                    case PICKED:
                        library.getOrderedBook(req);
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
