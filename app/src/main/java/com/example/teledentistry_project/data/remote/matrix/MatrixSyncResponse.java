package com.example.teledentistry_project.data.remote.matrix;

import com.google.gson.annotations.SerializedName;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;

public class MatrixSyncResponse {

    @SerializedName("next_batch")
    private String nextBatch;

    @SerializedName("rooms")
    private Rooms rooms;

    public String getNextBatch() {
        return nextBatch;
    }

    public Rooms getRooms() {
        return rooms;
    }

    public static class Rooms {
        @SerializedName("join")
        private Map<String, JoinedRoom> join;

        public Map<String, JoinedRoom> getJoin() {
            return join;
        }
    }

    public static class JoinedRoom {
        @SerializedName("timeline")
        private Timeline timeline;

        @SerializedName("state")
        private State state;

        public Timeline getTimeline() {
            return timeline;
        }

        public State getState() {
            return state;
        }
    }

    public static class Timeline {
        @SerializedName("events")
        private List<Event> events;

        public List<Event> getEvents() {
            return events;
        }
    }

    public static class State {
        @SerializedName("events")
        private List<Event> events;

        public List<Event> getEvents() {
            return events;
        }
    }

    public static class Event {
        @SerializedName("type")
        private String type;

        @SerializedName("sender")
        private String sender;

        @SerializedName("event_id")
        private String eventId;

        @SerializedName("origin_server_ts")
        private long originServerTs;

        @SerializedName("content")
        private JsonObject content;

        @SerializedName("state_key")
        private String stateKey;

        @SerializedName("unsigned")
        private JsonObject unsigned;

        public String getStateKey() {
            return stateKey;
        }

        public JsonObject getUnsigned() {
            return unsigned;
        }

        /** Transaction id of an event this device sent (only present on own events). */
        public String getTransactionId() {
            if (unsigned != null && unsigned.has("transaction_id") && unsigned.get("transaction_id").isJsonPrimitive()) {
                return unsigned.get("transaction_id").getAsString();
            }
            return null;
        }

        public String getType() {
            return type;
        }

        public String getSender() {
            return sender;
        }

        public String getEventId() {
            return eventId;
        }

        public long getOriginServerTs() {
            return originServerTs;
        }

        public JsonObject getContent() {
            return content;
        }
    }
}
