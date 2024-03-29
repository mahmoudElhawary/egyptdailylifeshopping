package com.egypt.daily.life.shopping.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long cartItemId;

    @ManyToOne
    @JoinColumn(name = "cartId")
    @JsonIgnore
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "productId")
    private Product product;
    
    @ManyToOne
    @JoinColumn(name = "userProductsId")
    private UserProducts userProducts;
    
    private double totalPriceDouble;

    @Column(name = "quantity", nullable = false, columnDefinition = "bigint(20) default 1")
    private int quantity = 1;

    public Long getCartItemId() {
		return cartItemId;
	}

	public void setCartItemId(Long cartItemId) {
		this.cartItemId = cartItemId;
	}

	public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

	public double getTotalPriceDouble() {
		return totalPriceDouble;
	}

	public void setTotalPriceDouble(double totalPriceDouble) {
		this.totalPriceDouble = totalPriceDouble;
	}

	public UserProducts getUserProducts() {
		return userProducts;
	}

	public void setUserProducts(UserProducts userProducts) {
		this.userProducts = userProducts;
	}
	
}
