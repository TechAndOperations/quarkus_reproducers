package org.acme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.logging.Log;

@ApplicationScoped
public class MyGraphQLClient {

    @Inject
    MyTypesafeClientAPI myGatewayTypesafeClientAPI;

    public List<MyDTO> fetchObjectsByRef(List<String> objectRefList) {
        Log.debugf("Fetching object(s) for the following objectRef(s) %s", objectRefList);
        List<MyDTO> simplifiedObjectDTOS = new ArrayList<>(objectRefList.size());

        for (int i = 0; i < objectRefList.size(); i += 500) {
            int end = Math.min(i + 500, objectRefList.size());
            simplifiedObjectDTOS.addAll(fetchByRef(objectRefList.subList(i, end)));
        }
        Log.debugf("Fetched %d object(s) by ref %s", simplifiedObjectDTOS.size(), objectRefList);
        return simplifiedObjectDTOS;
    }

    private List<MyDTO> fetchByRef(List<String> objectRefList) {
        try {
            List<MyDTO> response = myGatewayTypesafeClientAPI.fetchObjectByRef(objectRefList);
            if (response.isEmpty()) {
                Log.warnf("No object(s) found on My Gateway side for the given objectRef(s) %s", objectRefList);
                return Collections.emptyList();
            }
            return response;
        } catch (Exception e) {
            Log.warnf(e, "Error fetching objects by ref for the given objectRef(s) %s on My Gateway",
                    objectRefList);
            return Collections.emptyList();
        }
    }

}
