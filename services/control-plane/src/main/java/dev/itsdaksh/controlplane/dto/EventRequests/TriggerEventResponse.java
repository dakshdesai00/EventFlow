package dev.itsdaksh.controlplane.dto.EventRequests;

import java.util.List;
import java.util.Map;

public record TriggerEventResponse(

        Long eventId,

        String eventName,

        Map<String, Object> payload,

        List<TriggeredFunctionResponse> functions

) {
}