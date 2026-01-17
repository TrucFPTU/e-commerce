package com.groupproject.ecommerce.service;

import com.groupproject.ecommerce.entity.Entity.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {

    private List<Author> authors;
    private List<Publisher> publishers;
    private List<Supplier> suppliers;
    private List<Book> books;

    public BookService() {
        initializeData();
    }

    private void initializeData() {
        authors = new ArrayList<>();
        authors.add(new Author(1, "J.K. Rowling"));
        authors.add(new Author(2, "George R.R. Martin"));
        authors.add(new Author(3, "J.R.R. Tolkien"));
        authors.add(new Author(4, "Paulo Coelho"));
        authors.add(new Author(5, "Harper Lee"));
        authors.add(new Author(6, "Dan Brown"));
        authors.add(new Author(7, "Jane Austen"));
        authors.add(new Author(8, "Agatha Christie"));
        authors.add(new Author(9, "Stephen King"));
        authors.add(new Author(10, "Mark Twain"));
        authors.add(new Author(11, "Ernest Hemingway"));
        authors.add(new Author(12, "F. Scott Fitzgerald"));

        publishers = new ArrayList<>();
        publishers.add(new Publisher(1, "Bloomsbury Publishing"));
        publishers.add(new Publisher(2, "Bantam Books"));
        publishers.add(new Publisher(3, "HarperCollins"));
        publishers.add(new Publisher(4, "Doubleday"));
        publishers.add(new Publisher(5, "Penguin Books"));
        publishers.add(new Publisher(6, "Random House"));

        suppliers = new ArrayList<>();
        suppliers.add(new Supplier(1, "Book Distributors Co."));
        suppliers.add(new Supplier(2, "Global Literature Supply"));

        Author author1 = authors.get(0);
        Author author2 = authors.get(1);
        Author author3 = authors.get(2);
        Author author4 = authors.get(3);
        Author author5 = authors.get(4);
        Author author6 = authors.get(5);
        Author author7 = authors.get(6);
        Author author8 = authors.get(7);
        Author author9 = authors.get(8);
        Author author10 = authors.get(9);
        Author author11 = authors.get(10);
        Author author12 = authors.get(11);

        Publisher publisher1 = publishers.get(0);
        Publisher publisher2 = publishers.get(1);
        Publisher publisher3 = publishers.get(2);
        Publisher publisher4 = publishers.get(3);
        Publisher publisher5 = publishers.get(4);
        Publisher publisher6 = publishers.get(5);

        Supplier supplier1 = suppliers.get(0);
        Supplier supplier2 = suppliers.get(1);

        books = new ArrayList<>();
        books.add(new Book(1, "Harry Potter and the Philosopher's Stone",
            "A young wizard's journey begins at Hogwarts School of Witchcraft and Wizardry",
            "https://covers.openlibrary.org/b/olid/OL22856696M-L.jpg?default=false",
            299000, 1997, 150, "active", publisher1, supplier1, author1));
        books.add(new Book(2, "A Game of Thrones",
            "The first book in the epic fantasy series A Song of Ice and Fire",
            "https://covers.openlibrary.org/b/isbn/9780553103540-L.jpg?default=false",
            350000, 1996, 75, "active", publisher2, supplier1, author2));
        books.add(new Book(3, "The Lord of the Rings",
            "An epic high-fantasy novel set in Middle-earth",
            "https://covers.openlibrary.org/b/isbn/9780618260249-L.jpg?default=false",
            450000, 1954, 200, "active", publisher3, supplier2, author3));
        books.add(new Book(4, "The Alchemist",
            "A mystical story about following your dreams",
            "https://covers.openlibrary.org/b/olid/OL24214124M-L.jpg?default=false",
            199000, 1988, 300, "active", publisher3, supplier1, author4));
        books.add(new Book(5, "To Kill a Mockingbird",
            "A gripping tale of racial injustice and childhood innocence",
            "https://covers.openlibrary.org/b/isbn/9780060935467-L.jpg?default=false",
            250000, 1960, 120, "active", publisher3, supplier2, author5));
        books.add(new Book(6, "The Da Vinci Code",
            "A thrilling mystery involving secret societies and ancient mysteries",
            "https://covers.openlibrary.org/b/olid/OL3689912M-L.jpg?default=false",
            320000, 2003, 90, "active", publisher4, supplier1, author6));
        books.add(new Book(7, "Pride and Prejudice",
            "A romantic novel of manners set in Georgian England",
            "https://covers.openlibrary.org/b/isbn/9780141199078-L.jpg?default=false",
            180000, 1813, 180, "active", publisher1, supplier2, author7));
        books.add(new Book(8, "Murder on the Orient Express",
            "A classic detective mystery featuring Hercule Poirot",
            "https://covers.openlibrary.org/b/isbn/9780062073501-L.jpg?default=false",
            220000, 1934, 140, "active", publisher3, supplier1, author8));
        books.add(new Book(9, "Harry Potter and the Chamber of Secrets",
            "Harry's second year at Hogwarts brings new dangers",
            "https://covers.openlibrary.org/b/isbn/0439064864-L.jpg?default=false",
            299000, 1998, 5, "active", publisher1, supplier1, author1));
        books.add(new Book(10, "The Hobbit",
            "Bilbo Baggins' unexpected journey to reclaim dwarf treasure",
            "https://covers.openlibrary.org/b/olid/OL33891794M-L.jpg?default=false",
            280000, 1937, 0, "inactive", publisher3, supplier2, author3));
        books.add(new Book(11, "Harry Potter and the Prisoner of Azkaban",
            "Harry's third year brings the escape of Sirius Black",
            "https://covers.openlibrary.org/b/isbn/9780439136358-L.jpg?default=false",
            299000, 1999, 85, "active", publisher1, supplier1, author1));
        books.add(new Book(12, "Harry Potter and the Goblet of Fire",
            "The Triwizard Tournament comes to Hogwarts",
            "https://covers.openlibrary.org/b/isbn/9780439139595-L.jpg?default=false",
            349000, 2000, 60, "active", publisher1, supplier1, author1));
        books.add(new Book(13, "A Clash of Kings",
            "The second book in A Song of Ice and Fire series",
            "https://covers.openlibrary.org/b/olid/OL9760330M-L.jpg?default=false",
            350000, 1998, 55, "active", publisher2, supplier1, author2));
        books.add(new Book(14, "A Storm of Swords",
            "The third book in A Song of Ice and Fire series",
            "https://covers.openlibrary.org/b/olid/OL9369418M-L.jpg?default=false",
            380000, 2000, 45, "active", publisher2, supplier1, author2));
        books.add(new Book(15, "The Two Towers",
            "The second volume of The Lord of the Rings",
            "https://covers.openlibrary.org/b/isbn/9780547928203-L.jpg?default=false",
            420000, 1954, 110, "active", publisher3, supplier2, author3));
        books.add(new Book(16, "The Return of the King",
            "The final volume of The Lord of the Rings",
            "https://covers.openlibrary.org/b/isbn/9780547928197-L.jpg?default=false",
            420000, 1955, 95, "active", publisher3, supplier2, author3));
        books.add(new Book(17, "The Shining",
            "A family's terrifying stay at an isolated hotel",
            "https://covers.openlibrary.org/b/isbn/9780743424424-L.jpg?default=false",
            280000, 1977, 70, "active", publisher4, supplier1, author9));
        books.add(new Book(18, "It",
            "A group of children face an ancient evil in Derry, Maine",
            "https://covers.openlibrary.org/b/isbn/9781501142970-L.jpg?default=false",
            350000, 1986, 40, "active", publisher5, supplier2, author9));
        books.add(new Book(19, "The Adventures of Tom Sawyer",
            "The mischievous adventures of a young boy in Missouri",
            "https://covers.openlibrary.org/b/isbn/9780486400778-L.jpg?default=false",
            150000, 1876, 200, "active", publisher5, supplier1, author10));
        books.add(new Book(20, "Adventures of Huckleberry Finn",
            "A runaway boy and escaped slave journey down the Mississippi",
            "https://covers.openlibrary.org/b/isbn/9780142437179-L.jpg?default=false",
            160000, 1884, 180, "active", publisher5, supplier1, author10));
        books.add(new Book(21, "The Old Man and the Sea",
            "An aging fisherman's epic battle with a giant marlin",
            "https://covers.openlibrary.org/b/olid/OL358629M-L.jpg?default=false",
            180000, 1952, 150, "active", publisher6, supplier2, author11));
        books.add(new Book(22, "A Farewell to Arms",
            "A tragic love story set during World War I",
            "https://covers.openlibrary.org/b/isbn/9780684801469-L.jpg?default=false",
            200000, 1929, 90, "active", publisher6, supplier2, author11));
        books.add(new Book(23, "The Great Gatsby",
            "The decadence and disillusionment of the Jazz Age",
            "https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg?default=false",
            180000, 1925, 250, "active", publisher6, supplier1, author12));
        books.add(new Book(24, "Tender Is the Night",
            "A psychiatrist's descent into madness on the French Riviera",
            "https://covers.openlibrary.org/b/isbn/9780684801544-L.jpg?default=false",
            190000, 1934, 65, "active", publisher6, supplier1, author12));
        books.add(new Book(25, "And Then There Were None",
            "Ten strangers are lured to an island with deadly consequences",
            "https://covers.openlibrary.org/b/isbn/9780062073488-L.jpg?default=false",
            230000, 1939, 130, "active", publisher3, supplier1, author8));
        books.add(new Book(26, "Death on the Nile",
            "Hercule Poirot investigates a murder on an Egyptian cruise",
            "https://covers.openlibrary.org/b/isbn/9780062073556-L.jpg?default=false",
            220000, 1937, 100, "active", publisher3, supplier1, author8));
        books.add(new Book(27, "Emma",
            "A young woman's matchmaking attempts lead to romantic mishaps",
            "https://covers.openlibrary.org/b/isbn/9780141439587-L.jpg?default=false",
            175000, 1815, 85, "active", publisher1, supplier2, author7));
        books.add(new Book(28, "Sense and Sensibility",
            "Two sisters navigate love and heartbreak in Regency England",
            "https://covers.openlibrary.org/b/isbn/9780141439662-L.jpg?default=false",
            170000, 1811, 95, "active", publisher1, supplier2, author7));
        books.add(new Book(29, "Angels & Demons",
            "Robert Langdon faces the Illuminati in Vatican City",
            "https://covers.openlibrary.org/b/olid/OL3689920M-L.jpg?default=false",
            310000, 2000, 75, "active", publisher4, supplier1, author6));
        books.add(new Book(30, "Inferno",
            "Robert Langdon must solve puzzles based on Dante's Inferno",
            "https://covers.openlibrary.org/b/isbn/9780593078754-L.jpg?default=false",
            330000, 2013, 60, "active", publisher4, supplier1, author6));
        books.add(new Book(31, "Brida",
            "A young witch's journey to discover her destiny",
            "https://covers.openlibrary.org/b/isbn/9780061122415-L.jpg?default=false",
            185000, 1990, 110, "active", publisher3, supplier1, author4));
        books.add(new Book(32, "Eleven Minutes",
            "A Brazilian girl's journey of self-discovery in Geneva",
            "https://covers.openlibrary.org/b/isbn/9780062561565-L.jpg?default=false",
            195000, 2003, 80, "active", publisher3, supplier1, author4));
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<Book> getActiveBooks() {
        return books.stream()
            .filter(book -> "active".equals(book.getStatus()))
            .toList();
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public List<Publisher> getPublishers() {
        return publishers;
    }

    public List<Supplier> getSuppliers() {
        return suppliers;
    }

    public Book getBookById(int id) {
        return books.stream()
            .filter(b -> b.getId() == id)
            .findFirst()
            .orElse(null);
    }

    public Author getAuthorById(int id) {
        return authors.stream()
            .filter(a -> a.getId() == id)
            .findFirst()
            .orElse(null);
    }

    public Publisher getPublisherById(int id) {
        return publishers.stream()
            .filter(p -> p.getId() == id)
            .findFirst()
            .orElse(null);
    }

    public Supplier getSupplierById(int id) {
        return suppliers.stream()
            .filter(s -> s.getId() == id)
            .findFirst()
            .orElse(null);
    }

    public String getBooksForPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available books in our store:\n");
        for (Book book : getActiveBooks()) {
            sb.append("- \"").append(book.getName()).append("\" by ").append(book.getAuthor().getName());
            sb.append(" (").append(book.getDescription()).append(")");
            sb.append(" - Price: ₫").append(String.format("%,d", book.getPrice()));
            sb.append(", Stock: ").append(book.getStock()).append("\n");
        }
        return sb.toString();
    }
}
