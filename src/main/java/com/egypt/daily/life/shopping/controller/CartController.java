package com.egypt.daily.life.shopping.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.egypt.daily.life.shopping.domain.Response;
import com.egypt.daily.life.shopping.model.Cart;
import com.egypt.daily.life.shopping.model.CartItem;
import com.egypt.daily.life.shopping.model.Product;
import com.egypt.daily.life.shopping.model.ShippingAddress;
import com.egypt.daily.life.shopping.model.User;
import com.egypt.daily.life.shopping.model.UserProducts;
import com.egypt.daily.life.shopping.repository.CartItemRepository;
import com.egypt.daily.life.shopping.repository.CartRepository;
import com.egypt.daily.life.shopping.repository.ShippingAddressRepository;
import com.egypt.daily.life.shopping.service.CartService;
import com.egypt.daily.life.shopping.service.ProductService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class CartController {

	@Autowired
	private CartService cartService;

	@Autowired
	private ProductService productService ;
	
	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private ShippingAddressRepository shippingAddressRepository;
	
	@Autowired
	private CartRepository cartRepository;

	@GetMapping("getAllCarts")
	public ResponseEntity<List<Cart>> getAllCarts() {
		return new ResponseEntity<List<Cart>>(cartService.findAll(), HttpStatus.OK);
	}

	@GetMapping("getCartByUserId/{id}")
	public ResponseEntity<List<Cart>> getCartByUserId(@PathVariable("id") Long id) {
		List<Cart> carts = cartService.findByUserId(id);
		return new ResponseEntity<List<Cart>>(carts, HttpStatus.OK);
	}
	
	@GetMapping("getUserOrderByUserId/{id}")
	public ResponseEntity<List<Cart>> getUserOrderByUserId(@PathVariable("id") Long id) {
		List<Cart> carts = cartService.findAllByIsOrderedAndUserId(true, id);
		return new ResponseEntity<List<Cart>>(carts, HttpStatus.OK);
	}
	
	@GetMapping("getShippingAddressUserId/{id}")
	public ResponseEntity<List<ShippingAddress>> getShippingAddressUserId(@PathVariable("id") Long id) {
		List<ShippingAddress> carts = shippingAddressRepository.findAllByUserId(id);
		return new ResponseEntity<List<ShippingAddress>>(carts, HttpStatus.OK);
	}
	
	@GetMapping("findAllOrders")
	public ResponseEntity<List<Cart>> getAllOrders() {
		List<Cart> carts = cartService.findAllByIsOrdered(true) ;
		return new ResponseEntity<List<Cart>>(carts, HttpStatus.OK);
	}

	
	@PostMapping("/cartByUser")
	public ResponseEntity<List<Cart>> getCartByUser(@RequestBody User user) {
		List<Cart> cartDB = cartService.findByUser(user);
		return new ResponseEntity<List<Cart>>(cartDB, HttpStatus.OK);
	}

	@PostMapping("/saveCart")
	public ResponseEntity<List<Cart>> saveCart(@RequestParam("product") String product,
			@RequestParam("user") String user) throws JsonParseException, JsonMappingException, IOException {
		// get user data from rest api
		User userData = new ObjectMapper().readValue(user, User.class);
		// get product data from rest api
		Product productData = new ObjectMapper().readValue(product, Product.class);
		List<CartItem> cartItems = new ArrayList<CartItem>();
		CartItem cartItem = new CartItem();
		cartItem.setProduct(productData);
		cartItem.setTotalPriceDouble(productData.getProductPrice() * cartItem.getQuantity());
		cartItems.add(cartItem);
		Cart cart = new Cart();
		cart.setUser(userData);
		cart.setCartItems(cartItems);
		cartItem.setCart(cart);
		cartService.save(cart);
		return new ResponseEntity<List<Cart>>(cartService.findAll(), HttpStatus.OK);
	}

	@PostMapping("/updateCart")
	public ResponseEntity<List<Cart>> updateCart(@RequestParam("cart") String cart,
			@RequestParam("quantity") String quantity) throws JsonParseException, JsonMappingException, IOException {
		// get product data from rest api
		Cart cartData = new ObjectMapper().readValue(cart, Cart.class);
		int quan = new ObjectMapper().readValue(quantity, Integer.class);
		Cart selectedcart = cartService.findById(cartData.getCartId());
		List<CartItem> cartItems = new ArrayList<CartItem>();
		for (CartItem cartItem : selectedcart.getCartItems()) {
			cartItem.setQuantity(quan);
			cartItem.setTotalPriceDouble(cartItem.getProduct().getProductPrice() * cartItem.getQuantity());
			cartItem.setCart(selectedcart);
			cartItems.add(cartItem);

			cartItemRepository.save(cartItem);
		}
		selectedcart.setCartItems(cartItems);
		cartRepository.save(selectedcart);
		List<Cart> carts = cartService.findByUserId(cartData.getUser().getId());
		return new ResponseEntity<List<Cart>>(carts, HttpStatus.OK);
	}
	
	@PostMapping("/setUserOrder")
	public ResponseEntity<Response> saveCart(@RequestParam("shippingAddress") String shippingAddress)
			throws JsonParseException, JsonMappingException, IOException {
		if (shippingAddress != null) {
			// get product data from rest api
			int quantity = 0 ;
			ShippingAddress shippingAddressData = new ObjectMapper().readValue(shippingAddress, ShippingAddress.class);
			shippingAddressData.setIsDefault(true);
			shippingAddressRepository.save(shippingAddressData);
			List<Cart> carts = cartService.findByUserId(shippingAddressData.getUser().getId());
			double total = 0;
			for (Cart cart : carts) {
				for (CartItem cartItem : cart.getCartItems()) {
					total += cartItem.getTotalPriceDouble() ;
					Product product = productService.getProductById(cartItem.getProduct().getProductId()) ;
					product.setQuantity(product.getQuantity() - 1);
					productService.save(product) ;
					quantity = product.getQuantity() ;
				}
				cart.setOrdered(true);
				cart.setGrandTotal(total);
				cartService.save(cart) ;
			}
			if (quantity >= 1) {
				return new ResponseEntity<Response>(new Response("order is set Successfully"), HttpStatus.OK);
			} else {
				return null ;
			}
		} else {
			return null;
		}
	}

	@PostMapping("/deleteFromCart")
	public ResponseEntity<List<Cart>> deleteFromCart(@RequestParam("cart") String cart,@RequestParam("user") String user)
			throws JsonParseException, JsonMappingException, IOException {
		// get product data from rest api
		Cart cartData = new ObjectMapper().readValue(cart, Cart.class);
		User userData = new ObjectMapper().readValue(user, User.class);
		Cart selectedcart = cartService.findById(cartData.getCartId());
		cartService.emptyCart(selectedcart);
		List<Cart> carts = cartService.findByUserId(userData.getId());
		return new ResponseEntity<List<Cart>>(carts, HttpStatus.OK);
	}

	@PostMapping("/saveUserProductsCart")
	public ResponseEntity<List<Cart>> saveUserProductsCart(@RequestParam("product") String product,
			@RequestParam("user") String user) throws JsonParseException, JsonMappingException, IOException {
		// get user data from rest api
		User userData = new ObjectMapper().readValue(user, User.class);
		// get product data from rest api
		UserProducts productData = new ObjectMapper().readValue(product, UserProducts.class);
		List<CartItem> cartItems = new ArrayList<CartItem>();
		CartItem cartItem = new CartItem();
		cartItem.setUserProducts(productData);
		cartItem.setTotalPriceDouble(productData.getProductPrice() * cartItem.getQuantity());
		cartItems.add(cartItem);
		Cart cart = new Cart();
		cart.setUser(userData);
		cart.setCartItems(cartItems);
		cartItem.setCart(cart);
		cartService.save(cart);
		return new ResponseEntity<List<Cart>>(cartService.findAll(), HttpStatus.OK);
	}
}
