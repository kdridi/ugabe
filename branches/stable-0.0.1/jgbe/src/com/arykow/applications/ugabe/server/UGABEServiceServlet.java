package com.arykow.applications.ugabe.server;

import java.util.List;

import com.arykow.applications.ugabe.client.UGABEService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class UGABEServiceServlet extends RemoteServiceServlet implements UGABEService {
	private static final long serialVersionUID = 2780816170996352575L;

	private transient UGABEServiceController controller = new UGABEServiceController();
	public List<Integer> loadCartridge(String fileName) throws Exception {
		return controller.loadCartridge(getClass().getClassLoader().getResourceAsStream("sml.gb"));
	}
}
