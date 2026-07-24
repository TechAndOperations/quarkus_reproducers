package org.acme.mock;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.acme.MyTypesafeClientAPI;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Query;

import org.acme.MyDTO;

import io.quarkus.logging.Log;

@GraphQLApi
public class MockMyGatewayApi {

    @Query("objects")
    public List<MyDTO> objects(
            @Name("objectRefs") @NonNull List<@NonNull String> objectRefs) {
        Log.infof("[mock] my object called with %s", objectRefs);
        return objectRefs.stream()
                .map(ref -> new MyDTO(ref, mockUid(ref)))
                .toList();
    }

    private static String mockUid(String ref) {
        // Deterministic uid so the mock is stable across calls.
        return UUID.nameUUIDFromBytes(ref.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
