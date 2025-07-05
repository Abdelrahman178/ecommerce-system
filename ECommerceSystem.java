import java.time.LocalDate;
import java.util.*;

interface Shippable {
    String getName();
    double getWeight();
}

class Product {
    String name;
    double price;
    int quantity;
    LocalDate expiryDate;
    boolean needsShipping;
    double weight;

    public Product(String name, double price, int quantity, LocalDate expiryDate, boolean needsShipping, double weight) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.needsShipping = needsShipping;
        this.weight = weight;
    }

    public boolean isExpired() {
        return expiryDate != null && LocalDate.now().isAfter(expiryDate);
    }
}

class CartItem {
    Product product;
    int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
}

class Customer {
    String name;
    double balance;

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }
}

class Cart {
    List<CartItem> items = new ArrayList<>();

    public void add(Product product, int quantity) {
        if (quantity > product.quantity) {
            throw new RuntimeException("Not enough stock for " + product.name);
        }
        if (product.isExpired()) {
            throw new RuntimeException(product.name + " is expired");
        }
        items.add(new CartItem(product, quantity));
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

class ShippingService {
    public void ship(List<Shippable> items) {
        System.out.println("** Shipment notice **");
        double totalWeight = 0;
        for (Shippable item : items) {
            System.out.printf("1x %-12s %dg%n", item.getName(), (int)(item.getWeight() * 1000));
            totalWeight += item.getWeight();
        }
        System.out.printf("Total package weight %.1fkg%n", totalWeight);
        System.out.println();
    }
}

// Main
public class ECommerceSystem {
    public static void checkout(Customer customer, Cart cart) {
        if (cart.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        double subtotal = 0;
        for (CartItem item : cart.items) {
            subtotal += item.product.price * item.quantity;
        }

        List<Shippable> shippableItems = new ArrayList<>();
        for (CartItem item : cart.items) {
            if (item.product.needsShipping) {
                for (int i = 0; i < item.quantity; i++) {
                    shippableItems.add(new Shippable() {
                        public String getName() { return item.product.name; }
                        public double getWeight() { return item.product.weight; }
                    });
                }
            }
        }

        double shippingFees = shippableItems.size() > 0 ?
                shippableItems.stream().mapToDouble(Shippable::getWeight).sum() * 30 : 0;

        double total = subtotal + shippingFees;

        if (customer.balance < total) {
            throw new RuntimeException("Insufficient balance");
        }

        customer.balance -= total;

        for (CartItem item : cart.items) {
            item.product.quantity -= item.quantity;
        }

        if (!shippableItems.isEmpty()) {
            new ShippingService().ship(shippableItems);
        }

        System.out.println("** Checkout receipt **");
        for (CartItem item : cart.items) {
            System.out.printf("%dx %-12s %d%n", item.quantity, item.product.name, (int)(item.product.price * item.quantity));
        }
        System.out.println("---------------------");
        System.out.printf("Subtotal         %d%n", (int)subtotal);
        System.out.printf("Shipping         %d%n", (int)shippingFees);
        System.out.printf("Amount           %d%n", (int)total);
        System.out.println();
    }

    public static void main(String[] args) {
        Product cheese = new Product("Cheese", 100, 10, LocalDate.now().plusDays(7), true, 0.4);
        Product tv = new Product("TV", 5000, 3, null, true, 15.0);
        Product scratchCard = new Product("ScratchCard", 50, 20, null, false, 0.0);

        Customer customer = new Customer("John", 10000);
        Cart cart = new Cart();

        cart.add(cheese, 2);
        cart.add(tv, 1);
        cart.add(scratchCard, 1);

        checkout(customer, cart);
    }
}