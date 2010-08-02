package com.arykow.applications.ugabe.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("ugabe")
public interface UGABEService extends RemoteService {
	public abstract List<Integer> loadCartridge(String fileName) throws Exception;
}
