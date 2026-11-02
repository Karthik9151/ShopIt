# Abstract — ShopIt

ShopIt is a full-stack e-commerce application for a convenient digital retail experience. Customers can register, authenticate, discover products, search and sort the catalogue, save wishlist items, maintain a persistent cart, submit orders, and review order history. An administrator can manage catalogue records, inspect customers and orders, and update fulfilment statuses.

The system uses React and Vite, a Java 21 / Spring Boot REST API, Spring Security with JWT, Spring Data JPA, Flyway migrations, and MySQL. Core commerce rules are enforced on the server: product prices are read from the database, inventory is locked and decremented during order creation, and order items retain product-name and unit-price snapshots.

The payment module currently exposes a clearly labelled simulated payment workflow. It does not collect card credentials or transfer money. A production payment provider must be integrated with signed callback/webhook verification before live payments are enabled.
