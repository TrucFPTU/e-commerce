package com.groupproject.ecommerce.entity;

public class Entity {

    public static class User {
        private int id;
        private String fullname;
        private String email;
        private String password;

        public User(int id, String fullname, String email, String password) {
            this.id = id;
            this.fullname = fullname;
            this.email = email;
            this.password = password;
        }

        public int getId() { return id; }
        public String getFullname() { return fullname; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
    }

    public static class Book {
        private int id;
        private String name;
        private String description;
        private String imageURL;
        private int price;
        private int publishYear;
        private int stock;
        private String status;
        private Publisher publisher;
        private Supplier supplier;
        private Author author;

        public Book(int id, String name, String description, String imageURL, int price,
                   int publishYear, int stock, String status, Publisher publisher,
                   Supplier supplier, Author author) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.imageURL = imageURL;
            this.price = price;
            this.publishYear = publishYear;
            this.stock = stock;
            this.status = status;
            this.publisher = publisher;
            this.supplier = supplier;
            this.author = author;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getImageURL() { return imageURL; }
        public int getPrice() { return price; }
        public int getPublishYear() { return publishYear; }
        public int getStock() { return stock; }
        public String getStatus() { return status; }
        public Publisher getPublisher() { return publisher; }
        public Supplier getSupplier() { return supplier; }
        public Author getAuthor() { return author; }
    }

    public static class Author {
        private int id;
        private String name;

        public Author(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }
    }

    public static class Publisher {
        private int id;
        private String name;

        public Publisher(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }
    }

    public static class Supplier {
        private int id;
        private String name;

        public Supplier(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }
    }
}
