package org.flowvisor.api.handlers.configuration;

import java.util.List;

import org.flowvisor.api.handlers.ApiHandler;
import org.flowvisor.config.ConfigError;
import org.flowvisor.config.SliceImpl;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class ListSlices implements ApiHandler<Object> {

	
	
	@Override
	public JSONRPC2Response process(Object params) {
		JSONRPC2Response resp = null;
		try {
			List<String> slices = SliceImpl.getProxy().getAllSliceNames();
			resp = new JSONRPC2Response(slices, 0);
		} catch (ConfigError e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(JSONRPC2Error.INTERNAL_ERROR.getCode(), 
					cmdName() + ": Unable to fetch slice list : " + e.getMessage()), 0);
		} 
		return resp;
		
	}

	@Override
	public JSONRPC2ParamsType getType() {
		return JSONRPC2ParamsType.NO_PARAMS;
	}

	@Override
	public String cmdName() {
		return "list-slices";
	}

}