package com.arykow.applications.ugabe.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UGABEServiceAsync {
	public abstract void loadCartridge(String fileName, AsyncCallback<List<Integer>> callback);
}
