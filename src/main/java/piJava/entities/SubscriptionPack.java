package piJava.entities;

import java.math.BigDecimal;
import piJava.utils.SubscriptionCurrency;

public class SubscriptionPack {

    private int id;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private int durationDays;
    private String features;
    private boolean active;
    private String icon;
    private String color;
    private boolean popular;

    public SubscriptionPack() {
    }

    public SubscriptionPack(int id, String name, String description, BigDecimal price, String currency,
                            int durationDays, String features, boolean active, String icon, String color,
                            boolean popular) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.currency = SubscriptionCurrency.normalize(currency);
        this.durationDays = durationDays;
        this.features = features;
        this.active = active;
        this.icon = icon;
        this.color = color;
        this.popular = popular;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = SubscriptionCurrency.normalize(currency);
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isPopular() {
        return popular;
    }

    public void setPopular(boolean popular) {
        this.popular = popular;
    }

    @Override
    public String toString() {
        return "SubscriptionPack{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", currency='" + currency + '\'' +
                ", durationDays=" + durationDays +
                ", active=" + active +
                ", popular=" + popular +
                '}';
    }
}

