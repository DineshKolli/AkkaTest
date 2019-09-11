package cluster.sharding;

import java.io.Serializable;
import java.util.Objects;

class CallAsEntity implements Serializable {
    final CallId id;
    Value value;

    CallAsEntity(CallId id, Value value) {
        this.id = id;
        this.value = value;
    }

    CallAsEntity(String id, Object value) {
        this(new CallId(id), new Value(value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallAsEntity entity = (CallAsEntity) o;
        return Objects.equals(id, entity.id) &&
                Objects.equals(value, entity.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, value);
    }

    @Override
    public String toString() {
        return String.format("%s[%s, %s]", getClass().getSimpleName(), id, value);
    }

    static class CallId implements Serializable {
        final String id;

        CallId(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", getClass().getSimpleName(), id);
        }
    }

    static class Value implements Serializable {
        final Object value;

        Value(Object value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", getClass().getSimpleName(), value);
        }
    }
}