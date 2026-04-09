package eu.dissco.backend.web;

import static eu.dissco.backend.utils.ProxyUtils.removeHandleProxy;

import eu.dissco.backend.client.HandleClient;
import eu.dissco.backend.component.FdoRecordComponent;
import eu.dissco.backend.exceptions.WebProcessingFailedException;
import eu.dissco.backend.schema.VirtualCollection;
import eu.dissco.backend.schema.VirtualCollectionRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HandleComponent {

	private static final String ERROR_MESSAGE = "Error occurred while creating PID: {}";

	private final HandleClient handleClient;

	private final FdoRecordComponent fdoRecordComponent;

	public String postHandleVirtualCollection(VirtualCollectionRequest virtualCollection)
			throws WebProcessingFailedException {
		var request = fdoRecordComponent.getPostRequestVirtualCollection(virtualCollection);
		var response = handleClient.postHandle(request);
		try {
			return response.get("data").get(0).get("id").asString();
		}
		catch (NullPointerException _) {
			log.error(ERROR_MESSAGE, response);
			throw new WebProcessingFailedException("Unexpected response from Handle API");
		}
	}

	public List<String> postHandleMjr(int n) throws WebProcessingFailedException {
		var request = Collections.nCopies(n, fdoRecordComponent.getPostRequestMjr());
		var response = handleClient.postHandles(request);
		try {
			var dataNode = response.get("data");
			var handles = new ArrayList<String>();
			for (var node : dataNode) {
				handles.add(node.get("id").asString());
			}
			return handles;
		}
		catch (NullPointerException _) {
			log.error(ERROR_MESSAGE, response);
			throw new WebProcessingFailedException("Unexpected response from Handle API");
		}
	}

	public void tombstoneHandle(String handle) throws WebProcessingFailedException {
		var request = fdoRecordComponent.getTombstoneRequest(handle);
		handleClient.tombstoneHandle(handle, request);
	}

	public void updateHandle(VirtualCollection virtualCollection) throws WebProcessingFailedException {
		var request = fdoRecordComponent.getPatchHandleRequest(virtualCollection);
		handleClient.updateHandle(removeHandleProxy(virtualCollection.getId()), request);
	}

	public void rollbackVirtualCollection(String id) throws WebProcessingFailedException {
		var request = fdoRecordComponent.getRollbackCreateRequest(id);
		handleClient.rollbackHandle(request);
	}

}
