package com.mawen.learn.mybatis.domain.jpetstore;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/23
 */
public class Cart implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<String, CartItem> itemMap = Collections.synchronizedMap(new HashMap<>());
	private final List<CartItem> itemList = new ArrayList<>();

	public Iterator<CartItem> getCartItems() {
		return itemList.iterator();
	}

	public List<CartItem> getCartItemList() {
		return itemList;
	}

	public int getNumberOfItems() {
		return itemList.size();
	}

	public boolean containsItemId(String itemId) {
		return itemMap.containsKey(itemId);
	}

	public void addItem(Item item, boolean isInStock) {
		CartItem cartItem = itemMap.get(item.getItemId());
		if (cartItem == null) {
			cartItem = new CartItem();
			cartItem.setItem(item);
			cartItem.setQuantity(0);
			cartItem.setInStock(isInStock);
			itemMap.put(item.getItemId(), cartItem);
			itemList.add(cartItem);
		}
		cartItem.incrementQuantity();
	}

	public Item removeItemById(String itemId) {
		CartItem cartItem = itemMap.remove(itemId);
		if (cartItem == null) {
			return null;
		}
		itemList.remove(cartItem);
		return cartItem.getItem();
	}

	public void incrementQuantityByItemId(String itemId) {
		CartItem cartItem = itemMap.get(itemId);
		cartItem.incrementQuantity();
	}

	public void setQuantityByItemId(String itemId, int quantity) {
		CartItem cartItem = itemMap.get(itemId);
		cartItem.setQuantity(quantity);
	}

	public BigDecimal getSubTotal() {
		BigDecimal subTotal = new BigDecimal("0");
		Iterator<CartItem> items = getCartItems();
		while (items.hasNext()) {
			CartItem cartItem = items.next();
			Item item = cartItem.getItem();
			BigDecimal listPrice = item.getListPrice();
			BigDecimal quantity = new BigDecimal(String.valueOf(cartItem.getQuantity()));
			subTotal = subTotal.add(listPrice.multiply(quantity));
		}
		return subTotal;
	}

}
